package com.zszg.ai;

import com.zszg.question.Question;
import com.zszg.question.QuestionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI试卷生成服务
 * 
 * 功能：
 * 1. 智能组卷（从题库选题 + AI生成题）
 * 2. 知识点覆盖度分析
 * 3. 难度分布控制
 * 4. 自动生成答案和解析
 * 5. 导出Word/PDF
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGeneratorService {
    
    private final QuestionRepository questionRepository;
    private final GLMService glmService;
    
    /**
     * 生成试卷
     */
    public ExamPaper generateExam(ExamConfig config) {
        log.info("开始生成试卷, 科目={}, 范围={}", config.getSubject(), config.getScope());
        
        ExamPaper paper = new ExamPaper();
        paper.setExamId("exam_" + System.currentTimeMillis());
        paper.setSubject(config.getSubject());
        paper.setGrade(config.getGrade());
        paper.setTotalScore(config.getTotalScore());
        paper.setDuration(config.getDuration());
        paper.setGenerateTime(LocalDateTime.now());
        
        try {
            List<ExamQuestion> allQuestions = new ArrayList<>();
            
            // 1. 生成选择题
            if (config.getChoiceConfig() != null) {
                List<ExamQuestion> choices = generateQuestions(
                    config, 
                    config.getChoiceConfig(), 
                    "choice"
                );
                allQuestions.addAll(choices);
            }
            
            // 2. 生成填空题
            if (config.getFillBlankConfig() != null) {
                List<ExamQuestion> fillBlanks = generateQuestions(
                    config, 
                    config.getFillBlankConfig(), 
                    "fill_blank"
                );
                allQuestions.addAll(fillBlanks);
            }
            
            // 3. 生成解答题
            if (config.getSolveConfig() != null) {
                List<ExamQuestion> solves = generateSolveQuestions(config);
                allQuestions.addAll(solves);
            }
            
            paper.setQuestions(allQuestions);
            
            // 4. 生成元数据
            paper.setMetadata(generateMetadata(allQuestions, config));
            
            log.info("试卷生成完成, 共{}题", allQuestions.size());
            
        } catch (Exception e) {
            log.error("试卷生成失败", e);
            throw new RuntimeException("试卷生成失败: " + e.getMessage());
        }
        
        return paper;
    }
    
    /**
     * 生成选择题或填空题
     */
    private List<ExamQuestion> generateQuestions(
            ExamConfig examConfig, 
            QuestionConfig config, 
            String type) {
        
        List<ExamQuestion> questions = new ArrayList<>();
        
        // 从题库中检索题目
        List<Question> dbQuestions = questionRepository.findBySubject(examConfig.getSubject());
        
        // 过滤符合条件的题目
        List<Question> candidates = dbQuestions.stream()
                .filter(q -> matchesConfig(q, examConfig, config))
                .collect(Collectors.toList());
        
        // 计算从题库取多少，AI生成多少
        int fromDb = (int) (config.getCount() * 0.7);  // 70%从题库
        int fromAI = config.getCount() - fromDb;        // 30%AI生成
        
        // 从题库随机选择
        Collections.shuffle(candidates);
        List<Question> selected = candidates.stream()
                .limit(fromDb)
                .collect(Collectors.toList());
        
        // 转换为ExamQuestion
        int questionNumber = 1;
        for (Question q : selected) {
            ExamQuestion eq = convertToExamQuestion(q, questionNumber++, config.getScorePerItem());
            questions.add(eq);
        }
        
        // AI生成剩余题目
        for (int i = 0; i < fromAI; i++) {
            ExamQuestion aiQuestion = generateAIQuestion(
                examConfig, 
                config, 
                type, 
                questionNumber++
            );
            questions.add(aiQuestion);
        }
        
        return questions;
    }
    
    /**
     * 生成解答题
     */
    private List<ExamQuestion> generateSolveQuestions(ExamConfig examConfig) {
        List<ExamQuestion> questions = new ArrayList<>();
        QuestionConfig config = examConfig.getSolveConfig();
        
        int questionNumber = 1;
        
        // 解答题通常每题分值不同，需要递进
        List<Integer> scores = config.getScores();
        if (scores == null || scores.isEmpty()) {
            scores = generateDefaultScores(config.getCount());
        }
        
        for (int i = 0; i < config.getCount(); i++) {
            int score = scores.get(i);
            
            // 根据分值判断难度
            String difficulty;
            if (score <= 6) {
                difficulty = "简单";
            } else if (score <= 10) {
                difficulty = "中等";
            } else {
                difficulty = "困难";
            }
            
            // AI生成解答题
            ExamQuestion question = generateAISolveQuestion(
                examConfig, 
                difficulty, 
                score, 
                questionNumber++
            );
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * AI生成题目
     */
    private ExamQuestion generateAIQuestion(
            ExamConfig examConfig, 
            QuestionConfig config, 
            String type, 
            int number) {
        
        String prompt = buildGeneratePrompt(examConfig, config, type);
        String response = glmService.answerQuestion(examConfig.getSubject(), prompt, null);
        
        return parseAIQuestion(response, type, number, config.getScorePerItem());
    }
    
    /**
     * AI生成解答题
     */
    private ExamQuestion generateAISolveQuestion(
            ExamConfig examConfig, 
            String difficulty, 
            int score, 
            int number) {
        
        // 随机选择一个知识点
        String knowledgePoint = examConfig.getScope().isEmpty() 
            ? examConfig.getSubject() 
            : examConfig.getScope().get(new Random().nextInt(examConfig.getScope().size()));
        
        String prompt = String.format(
            "请生成一道%s学科的%s难度解答题，考查知识点：%s。\n\n" +
            "要求：\n" +
            "1. 题目要完整、清晰\n" +
            "2. 分值%d分，难度适中\n" +
            "3. 给出详细的解答过程\n" +
            "4. 标注关键步骤的分值\n\n" +
            "输出格式：\n" +
            "【题目】\n题目内容\n\n" +
            "【答案】\n解答过程\n\n" +
            "【评分标准】\n评分细则\n\n" +
            "【解析】\n解题思路",
            examConfig.getSubject(), difficulty, knowledgePoint, score
        );
        
        String response = glmService.answerQuestion(examConfig.getSubject(), prompt, null);
        
        return parseAISolveQuestion(response, number, score, knowledgePoint);
    }
    
    /**
     * 构建生成题目的提示词
     */
    private String buildGeneratePrompt(ExamConfig examConfig, QuestionConfig config, String type) {
        String typeName = type.equals("choice") ? "选择题" : "填空题";
        
        String prompt = String.format(
            "请生成一道%s学科的%s，难度：%s。\n\n",
            examConfig.getSubject(), typeName, config.getDifficulty()
        );
        
        if (!examConfig.getScope().isEmpty()) {
            prompt += "考查知识点范围：" + String.join("、", examConfig.getScope()) + "\n\n";
        }
        
        if (type.equals("choice")) {
            prompt += "输出格式：\n" +
                     "【题目】题目内容\n" +
                     "【选项】\n" +
                     "A. 选项A\n" +
                     "B. 选项B\n" +
                     "C. 选项C\n" +
                     "D. 选项D\n" +
                     "【答案】B\n" +
                     "【解析】解题思路";
        } else {
            prompt += "输出格式：\n" +
                     "【题目】题目内容\n" +
                     "【答案】答案\n" +
                     "【解析】解题思路";
        }
        
        return prompt;
    }
    
    /**
     * 解析AI生成的题目
     */
    private ExamQuestion parseAIQuestion(String response, String type, int number, int score) {
        ExamQuestion question = new ExamQuestion();
        question.setId("q" + number);
        question.setNumber(number);
        question.setType(type);
        question.setScore(score);
        
        // 简单解析（实际应该用更robust的解析方法）
        String[] parts = response.split("【");
        
        for (String part : parts) {
            if (part.startsWith("题目】")) {
                question.setContent(part.substring(3).split("\n")[0].trim());
            } else if (part.startsWith("选项】")) {
                String optionsText = part.substring(3);
                List<String> options = new ArrayList<>();
                for (String line : optionsText.split("\n")) {
                    if (line.matches("^[A-D]\\..*")) {
                        options.add(line);
                    }
                }
                question.setOptions(options);
            } else if (part.startsWith("答案】")) {
                question.setAnswer(part.substring(3).split("\n")[0].trim());
            } else if (part.startsWith("解析】")) {
                question.setAnalysis(part.substring(3).trim());
            }
        }
        
        return question;
    }
    
    /**
     * 解析AI生成的解答题
     */
    private ExamQuestion parseAISolveQuestion(String response, int number, int score, String knowledgePoint) {
        ExamQuestion question = new ExamQuestion();
        question.setId("q" + number);
        question.setNumber(number);
        question.setType("solve");
        question.setScore(score);
        
        String[] parts = response.split("【");
        
        for (String part : parts) {
            if (part.startsWith("题目】")) {
                question.setContent(part.substring(3).split("【")[0].trim());
            } else if (part.startsWith("答案】")) {
                question.setAnswer(part.substring(3).split("【")[0].trim());
            } else if (part.startsWith("解析】")) {
                question.setAnalysis(part.substring(3).split("【")[0].trim());
            } else if (part.startsWith("评分标准】")) {
                question.setScoringCriteria(part.substring(6).split("【")[0].trim());
            }
        }
        
        question.setKnowledgePoints(Collections.singletonList(knowledgePoint));
        
        return question;
    }
    
    /**
     * 判断题目是否符合配置
     */
    private boolean matchesConfig(Question q, ExamConfig examConfig, QuestionConfig config) {
        // 检查知识点范围
        if (!examConfig.getScope().isEmpty()) {
            boolean inScope = examConfig.getScope().contains(q.getSubject());
            if (!inScope) return false;
        }
        
        // 检查难度
        String difficulty = config.getDifficulty();
        if (!"mixed".equals(difficulty)) {
            if (!difficulty.equals(q.getDifficulty())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 转换数据库题目为试卷题目
     */
    private ExamQuestion convertToExamQuestion(Question q, int number, int score) {
        ExamQuestion eq = new ExamQuestion();
        eq.setId("q" + number);
        eq.setNumber(number);
        eq.setType(q.getType());
        eq.setContent(q.getContent());
        eq.setAnswer(q.getAnswer());
        eq.setAnalysis(q.getAnalysis());
        eq.setScore(score);
        eq.setDifficulty(q.getDifficulty());
        
        // 如果是选择题，解析选项
        if ("choice".equals(q.getType()) && q.getOptions() != null) {
            eq.setOptions(Arrays.asList(q.getOptions().split("\\|")));
        }
        
        return eq;
    }
    
    /**
     * 生成默认分值分布
     */
    private List<Integer> generateDefaultScores(int count) {
        List<Integer> scores = new ArrayList<>();
        int[] template = {6, 8, 8, 10, 10, 12};
        for (int i = 0; i < count; i++) {
            scores.add(template[i % template.length]);
        }
        return scores;
    }
    
    /**
     * 生成试卷元数据
     */
    private ExamMetadata generateMetadata(List<ExamQuestion> questions, ExamConfig config) {
        ExamMetadata metadata = new ExamMetadata();
        
        metadata.setTotalQuestions(questions.size());
        metadata.setTotalScore(questions.stream().mapToInt(ExamQuestion::getScore).sum());
        metadata.setEstimatedTime(config.getDuration());
        
        // 难度分布
        Map<String, Long> difficultyDist = questions.stream()
                .collect(Collectors.groupingBy(
                    q -> q.getDifficulty() != null ? q.getDifficulty() : "中等",
                    Collectors.counting()
                ));
        metadata.setDifficultyDistribution(difficultyDist);
        
        // 知识点覆盖
        Map<String, Double> knowledgeCoverage = new HashMap<>();
        for (String scope : config.getScope()) {
            long count = questions.stream()
                    .filter(q -> q.getKnowledgePoints() != null && 
                                 q.getKnowledgePoints().contains(scope))
                    .count();
            knowledgeCoverage.put(scope, count / (double) questions.size());
        }
        metadata.setKnowledgeCoverage(knowledgeCoverage);
        
        return metadata;
    }
    
    /**
     * 试卷配置
     */
    @Data
    public static class ExamConfig {
        private String subject;
        private String grade;
        private List<String> scope;  // 知识点范围
        private int duration;  // 时长（分钟）
        private int totalScore;
        private QuestionConfig choiceConfig;
        private QuestionConfig fillBlankConfig;
        private QuestionConfig solveConfig;
    }
    
    /**
     * 题型配置
     */
    @Data
    public static class QuestionConfig {
        private int count;
        private int scorePerItem;
        private String difficulty;  // easy/medium/hard/mixed
        private List<Integer> scores;  // 每题分值（用于解答题）
    }
    
    /**
     * 试卷
     */
    @Data
    public static class ExamPaper {
        private String examId;
        private String subject;
        private String grade;
        private int totalScore;
        private int duration;
        private LocalDateTime generateTime;
        private List<ExamQuestion> questions;
        private ExamMetadata metadata;
    }
    
    /**
     * 试卷题目
     */
    @Data
    public static class ExamQuestion {
        private String id;
        private int number;
        private String type;
        private String content;
        private List<String> options;
        private String answer;
        private String analysis;
        private String scoringCriteria;  // 评分标准
        private List<String> knowledgePoints;
        private String difficulty;
        private int score;
    }
    
    /**
     * 试卷元数据
     */
    @Data
    public static class ExamMetadata {
        private int totalQuestions;
        private int totalScore;
        private int estimatedTime;
        private Map<String, Long> difficultyDistribution;
        private Map<String, Double> knowledgeCoverage;
    }
}



