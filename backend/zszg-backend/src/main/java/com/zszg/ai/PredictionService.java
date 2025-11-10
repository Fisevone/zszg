package com.zszg.ai;

import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI预测性干预服务
 * 
 * 功能：
 * 1. 分析学习数据，预测薄弱知识点
 * 2. 预警可能的学习问题
 * 3. 生成个性化干预建议
 * 4. 预测考试成绩
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {
    
    private final ErrorBookRepository errorBookRepository;
    private final GLMService glmService;
    private final DemoDataService demoDataService;
    
    /**
     * 预测学生的薄弱知识点
     */
    public PredictionReport predictWeakness(User user, String subject) {
        log.info("开始预测分析, userId={}, subject={}", user.getId(), subject);
        
        PredictionReport report = new PredictionReport();
        report.setUserId(user.getId());
        report.setSubject(subject);
        report.setGenerateTime(LocalDateTime.now());
        
        // 1. 获取学生的错题数据
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        List<ErrorBook> subjectErrors = errorBooks.stream()
                .filter(eb -> subject.equals(eb.getQuestion().getSubject()))
                .collect(Collectors.toList());
        
        // 如果没有错题数据，使用演示数据
        if (subjectErrors.isEmpty() || subjectErrors.size() < 3) {
            log.info("错题数据不足，使用演示数据");
            return generateDemoReport(user.getId(), subject);
        }
        
        // 2. 分析知识点掌握情况
        Map<String, KnowledgeStatus> knowledgeStats = analyzeKnowledgePoints(subjectErrors);
        
        // 3. 生成预测
        List<WeaknessPrediction> predictions = new ArrayList<>();
        for (Map.Entry<String, KnowledgeStatus> entry : knowledgeStats.entrySet()) {
            String knowledgePoint = entry.getKey();
            KnowledgeStatus status = entry.getValue();
            
            WeaknessPrediction prediction = predictKnowledgePoint(knowledgePoint, status, subject);
            if (prediction.getRiskLevel() != RiskLevel.LOW) {
                predictions.add(prediction);
            }
        }
        
        // 4. 排序（按风险等级和优先级）
        predictions.sort((a, b) -> {
            int riskCompare = b.getRiskLevel().ordinal() - a.getRiskLevel().ordinal();
            if (riskCompare != 0) return riskCompare;
            return b.getPriority() - a.getPriority();
        });
        
        report.setPredictions(predictions);
        
        // 5. 计算整体风险
        report.setOverallRisk(calculateOverallRisk(predictions));
        
        // 6. 预测考试成绩
        report.setExamPrediction(predictExamScore(user, subject, knowledgeStats));
        
        log.info("预测分析完成, 发现{}个薄弱点", predictions.size());
        
        return report;
    }
    
    /**
     * 分析知识点掌握情况
     */
    private Map<String, KnowledgeStatus> analyzeKnowledgePoints(List<ErrorBook> errorBooks) {
        Map<String, KnowledgeStatus> stats = new HashMap<>();
        
        for (ErrorBook error : errorBooks) {
            String subject = error.getQuestion().getSubject();
            KnowledgeStatus status = stats.computeIfAbsent(subject, k -> new KnowledgeStatus());
            
            status.totalErrors++;
            status.lastErrorTime = error.getCreatedAt();
            
            // 计算时间跨度
            if (status.firstErrorTime == null) {
                status.firstErrorTime = error.getCreatedAt();
            }
            
            // 检查是否订正
            if (error.getCorrection() != null && !error.getCorrection().isEmpty()) {
                status.correctedCount++;
                status.lastCorrectionTime = error.getUpdatedAt();
            }
            
            // 难度分布
            String difficulty = error.getQuestion().getDifficulty();
            status.difficultyDistribution.put(
                difficulty, 
                status.difficultyDistribution.getOrDefault(difficulty, 0) + 1
            );
        }
        
        // 计算掌握度
        for (KnowledgeStatus status : stats.values()) {
            status.masteryLevel = calculateMasteryLevel(status);
            if (status.lastErrorTime != null) {
                status.daysSinceLastError = ChronoUnit.DAYS.between(status.lastErrorTime, Instant.now());
            }
        }
        
        return stats;
    }
    
    /**
     * 计算知识点掌握度 (0-1)
     */
    private double calculateMasteryLevel(KnowledgeStatus status) {
        // 基础分：订正率
        double correctionRate = status.correctedCount / (double) status.totalErrors;
        double score = correctionRate * 50;
        
        // 时间因素：最近有没有复习（遗忘曲线）
        if (status.lastCorrectionTime != null) {
            long daysSinceCorrection = ChronoUnit.DAYS.between(
                status.lastCorrectionTime, Instant.now());
            
            // Ebbinghaus遗忘曲线：y = e^(-t/S)
            // t: 时间（天）, S: 记忆强度（这里假设为7天）
            double retention = Math.exp(-daysSinceCorrection / 7.0);
            score += retention * 30;
        }
        
        // 错误频率：错得越少越好
        if (status.totalErrors <= 2) {
            score += 20;
        } else if (status.totalErrors <= 5) {
            score += 10;
        }
        
        return Math.min(100, score) / 100.0;
    }
    
    /**
     * 预测单个知识点
     */
    private WeaknessPrediction predictKnowledgePoint(
            String knowledgePoint, KnowledgeStatus status, String subject) {
        
        WeaknessPrediction prediction = new WeaknessPrediction();
        prediction.setKnowledgePoint(knowledgePoint);
        
        // 计算风险概率
        double riskProbability = 1.0 - status.masteryLevel;
        prediction.setProbability(riskProbability);
        
        // 判断风险等级
        if (riskProbability >= 0.7) {
            prediction.setRiskLevel(RiskLevel.HIGH);
            prediction.setPriority(1);
        } else if (riskProbability >= 0.4) {
            prediction.setRiskLevel(RiskLevel.MEDIUM);
            prediction.setPriority(2);
        } else {
            prediction.setRiskLevel(RiskLevel.LOW);
            prediction.setPriority(3);
        }
        
        // 生成原因分析
        String reason = generateReason(status);
        prediction.setReason(reason);
        
        // 生成建议
        List<String> suggestions = generateSuggestions(knowledgePoint, status, subject);
        prediction.setSuggestions(suggestions);
        
        // 预计学习时间
        prediction.setEstimatedTime(estimateStudyTime(status));
        
        return prediction;
    }
    
    /**
     * 生成原因分析
     */
    private String generateReason(KnowledgeStatus status) {
        List<String> reasons = new ArrayList<>();
        
        // 错误率高
        if (status.totalErrors >= 5) {
            reasons.add(String.format("该知识点错题率较高（共%d次错误）", status.totalErrors));
        }
        
        // 订正率低
        double correctionRate = status.correctedCount / (double) status.totalErrors;
        if (correctionRate < 0.5) {
            reasons.add(String.format("订正率较低（仅%.0f%%）", correctionRate * 100));
        }
        
        // 长时间未复习
        if (status.daysSinceLastError > 14) {
            reasons.add(String.format("已%d天未复习，可能已遗忘", status.daysSinceLastError));
        }
        
        // 难题错误多
        int hardCount = status.difficultyDistribution.getOrDefault("困难", 0);
        if (hardCount >= 2) {
            reasons.add("在难题上频繁出错");
        }
        
        return String.join("；", reasons);
    }
    
    /**
     * 生成学习建议
     */
    private List<String> generateSuggestions(String knowledgePoint, KnowledgeStatus status, String subject) {
        List<String> suggestions = new ArrayList<>();
        
        // 根据掌握度给建议
        if (status.masteryLevel < 0.3) {
            suggestions.add("建议今天就开始复习该知识点");
            suggestions.add("先从基础概念复习，不要急于做难题");
            suggestions.add("预计需要3-5天时间系统学习");
        } else if (status.masteryLevel < 0.6) {
            suggestions.add("建议近3天内安排复习");
            suggestions.add("重点突破易错题型");
            suggestions.add("预计需要2-3天时间巩固");
        } else {
            suggestions.add("建议1周内安排复习");
            suggestions.add("可以做一些提升题保持状态");
        }
        
        // 根据错题情况给建议
        if (status.correctedCount == 0) {
            suggestions.add("尚未订正过错题，建议先完成订正");
        }
        
        // 推荐练习题数量
        int recommendCount = (int) Math.ceil(status.totalErrors * 0.6);
        suggestions.add(String.format("推荐完成%d道类似题目巩固", Math.max(3, recommendCount)));
        
        return suggestions;
    }
    
    /**
     * 估算学习时间
     */
    private String estimateStudyTime(KnowledgeStatus status) {
        if (status.masteryLevel < 0.3) {
            return "3-5天";
        } else if (status.masteryLevel < 0.6) {
            return "2-3天";
        } else {
            return "1-2天";
        }
    }
    
    /**
     * 计算整体风险
     */
    private String calculateOverallRisk(List<WeaknessPrediction> predictions) {
        if (predictions.isEmpty()) {
            return "low";
        }
        
        long highRiskCount = predictions.stream()
                .filter(p -> p.getRiskLevel() == RiskLevel.HIGH)
                .count();
        
        if (highRiskCount >= 3) {
            return "high";
        } else if (highRiskCount >= 1 || predictions.size() >= 5) {
            return "medium";
        } else {
            return "low";
        }
    }
    
    /**
     * 预测考试成绩
     */
    private ExamPrediction predictExamScore(
            User user, String subject, Map<String, KnowledgeStatus> knowledgeStats) {
        
        ExamPrediction prediction = new ExamPrediction();
        prediction.setSubject(subject);
        
        // 计算平均掌握度
        double avgMastery = knowledgeStats.values().stream()
                .mapToDouble(KnowledgeStatus::getMasteryLevel)
                .average()
                .orElse(0.5);
        
        // 预测分数（假设满分100）
        // 基础分60 + 掌握度贡献40
        int estimatedScore = (int) (60 + avgMastery * 40);
        prediction.setEstimatedScore(estimatedScore);
        
        // 计算提升空间
        int improvementSpace = 100 - estimatedScore;
        prediction.setImprovementSpace(improvementSpace);
        
        // 生成建议
        if (estimatedScore < 70) {
            prediction.setAdvice("当前基础较弱，建议系统复习，预计可提升" + improvementSpace + "分");
        } else if (estimatedScore < 85) {
            prediction.setAdvice("整体掌握良好，重点突破薄弱环节，预计可提升" + improvementSpace + "分");
        } else {
            prediction.setAdvice("掌握优秀，保持状态即可，可挑战难题冲刺满分");
        }
        
        return prediction;
    }
    
    /**
     * 生成演示报告（当数据不足时）
     */
    private PredictionReport generateDemoReport(Long userId, String subject) {
        PredictionReport report = new PredictionReport();
        report.setUserId(userId);
        report.setSubject(subject);
        report.setGenerateTime(LocalDateTime.now());
        report.setDemoMode(true);
        
        // 创建演示预测
        List<WeaknessPrediction> predictions = new ArrayList<>();
        
        // 高风险知识点
        WeaknessPrediction high = new WeaknessPrediction();
        high.setKnowledgePoint(subject + "核心概念");
        high.setRiskLevel(RiskLevel.HIGH);
        high.setProbability(0.75);
        high.setReason("该知识点近期错题较多，订正率不足50%");
        high.setSuggestions(Arrays.asList(
            "建议今天就开始复习该知识点",
            "先从基础概念复习，不要急于做难题",
            "预计需要3-5天时间系统学习"
        ));
        high.setEstimatedTime("3-5天");
        high.setPriority(1);
        predictions.add(high);
        
        // 中风险知识点
        WeaknessPrediction medium = new WeaknessPrediction();
        medium.setKnowledgePoint(subject + "应用技巧");
        medium.setRiskLevel(RiskLevel.MEDIUM);
        medium.setProbability(0.55);
        medium.setReason("该知识点有少量错题，建议加强练习");
        medium.setSuggestions(Arrays.asList(
            "建议近3天内安排复习",
            "重点突破易错题型",
            "预计需要2-3天时间巩固"
        ));
        medium.setEstimatedTime("2-3天");
        medium.setPriority(2);
        predictions.add(medium);
        
        report.setPredictions(predictions);
        report.setOverallRisk("medium");
        
        // 演示考试预测
        ExamPrediction examPred = new ExamPrediction();
        examPred.setSubject(subject);
        examPred.setEstimatedScore(75);
        examPred.setImprovementSpace(25);
        examPred.setAdvice("当前掌握良好，重点突破薄弱环节，预计可提升25分");
        report.setExamPrediction(examPred);
        
        return report;
    }
    
    /**
     * 知识点状态
     */
    @Data
    private static class KnowledgeStatus {
        private int totalErrors = 0;
        private int correctedCount = 0;
        private Instant firstErrorTime;
        private Instant lastErrorTime;
        private Instant lastCorrectionTime;
        private long daysSinceLastError;
        private Map<String, Integer> difficultyDistribution = new HashMap<>();
        private double masteryLevel;  // 掌握度 0-1
    }
    
    /**
     * 预测报告
     */
    @Data
    public static class PredictionReport {
        private Long userId;
        private String subject;
        private LocalDateTime generateTime;
        private List<WeaknessPrediction> predictions;
        private String overallRisk;  // high/medium/low
        private ExamPrediction examPrediction;
        private boolean demoMode = false;  // 是否为演示模式
    }
    
    /**
     * 薄弱点预测
     */
    @Data
    public static class WeaknessPrediction {
        private String knowledgePoint;
        private RiskLevel riskLevel;
        private double probability;  // 出错概率 0-1
        private String reason;
        private List<String> suggestions;
        private String estimatedTime;
        private int priority;  // 优先级 1最高
    }
    
    /**
     * 考试预测
     */
    @Data
    public static class ExamPrediction {
        private String subject;
        private int estimatedScore;
        private int improvementSpace;
        private String advice;
    }
    
    /**
     * 风险等级
     */
    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}



