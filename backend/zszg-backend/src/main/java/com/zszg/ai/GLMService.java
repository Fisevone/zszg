package com.zszg.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * GLM大模型集成服务
 * 提供智能分析、知识溯源、题目推荐等AI功能
 */
@Slf4j
@Service
public class GLMService {
    
    @Value("${app.glm.api-key:3f3508c9bcbe476db696356fb8ac6345.n2bT5A7uqHiNDd3l}")
    private String apiKey;
    
    @Value("${app.glm.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 注入缓存服务（使用@Lazy避免循环依赖）
    @Autowired
    @Lazy
    private com.zszg.service.CacheService cacheService;
    
    // 注入重试服务
    @Autowired(required = false)
    @Lazy
    private com.zszg.service.RetryService retryService;
    
    /**
     * 智能分析错题 - 生成错因分析和改进建议（带缓存）
     */
    public String analyzeErrorQuestion(String subject, String questionContent, String correctAnswer, 
                                       String userAnswer, String difficulty) {
        // 使用缓存服务
        if (cacheService != null) {
            return cacheService.getOrGenerateAnalysis(
                questionContent, correctAnswer, userAnswer, difficulty,
                () -> generateAnalysisInternal(subject, questionContent, correctAnswer, userAnswer, difficulty)
            );
        }
        
        // 如果缓存服务不可用,直接生成
        return generateAnalysisInternal(subject, questionContent, correctAnswer, userAnswer, difficulty);
    }
    
    /**
     * 内部方法：实际生成分析
     */
    private String generateAnalysisInternal(String subject, String questionContent, String correctAnswer,
                                           String userAnswer, String difficulty) {
        String prompt = String.format(
            "你是一位资深的%s学科老师。请为学生生成一份详细的解题分析。\n\n" +
            "【题目】%s\n" +
            "【正确答案】%s\n" +
            "【学生答案】%s\n" +
            "【难度】%s\n\n" +
            "=== 重要：必须严格按照以下格式输出，每个部分都必须详细展开 ===\n\n" +
            "【题目解析】\n" +
            "这是一道%s学科的___类型题目，主要考察学生对___知识点的理解和运用能力。（至少写3句话）\n\n" +
            "【第一步：理解题意】\n" +
            "让我们先仔细读题，找出关键信息：\n" +
            "• 已知条件1：___（具体写出）\n" +
            "• 已知条件2：___（具体写出）\n" +
            "• 已知条件3：___（如果有的话）\n" +
            "• 求解目标：题目要求我们求___\n" +
            "• 题目关键词：___（标出重点词汇）\n\n" +
            "【第二步：分析解题思路】\n" +
            "现在我们来想一想，应该怎么解决这道题：\n" +
            "1. 首先要做什么？\n" +
            "   答：___\n" +
            "   为什么？因为___（解释原因，至少2句话）\n\n" +
            "2. 然后要做什么？\n" +
            "   答：___\n" +
            "   为什么？因为___（解释原因，至少2句话）\n\n" +
            "3. 最后要做什么？\n" +
            "   答：___\n" +
            "   为什么？因为___（解释原因，至少2句话）\n\n" +
            "【第三步：详细解答过程】\n" +
            "现在让我们一步步来计算：\n\n" +
            "步骤1：___（写出这一步要做什么）\n" +
            "   计算过程：___（具体的计算式子）\n" +
            "   结果：___\n" +
            "   说明：为什么要这样算？因为___（解释用到的公式或原理）\n\n" +
            "步骤2：___（写出这一步要做什么）\n" +
            "   计算过程：___（具体的计算式子）\n" +
            "   结果：___\n" +
            "   说明：这一步的依据是___\n\n" +
            "步骤3：___（写出这一步要做什么）\n" +
            "   计算过程：___（具体的计算式子）\n" +
            "   最终答案：___\n" +
            "   验证：我们来检查一下答案是否合理：___（至少写一句验证）\n\n" +
            "【错误分析】\n" +
            "学生的答案是：%s\n" +
            "学生的错误在于：___（具体指出错在哪一步）\n" +
            "为什么会这样错？可能的原因有：___（分析至少2个原因）\n" +
            "正确的做法应该是：___（对比说明）\n\n" +
            "【核心知识点】\n" +
            "这道题目主要涉及以下知识点：\n" +
            "• 知识点1：___（说明这个知识点是什么）\n" +
            "• 知识点2：___（说明这个知识点是什么）\n" +
            "• 知识点3：___（如果有的话）\n\n" +
            "【易错提醒】\n" +
            "⚠️ 做这类题目时要特别注意：\n" +
            "1. ___（第一个易错点）\n" +
            "2. ___（第二个易错点）\n" +
            "3. ___（第三个易错点）\n\n" +
            "【改进建议】\n" +
            "为了避免以后再犯类似错误，建议：\n" +
            "1. 重点复习___知识点，特别是___方面\n" +
            "2. 多做___类型的练习题，建议每天练___道\n" +
            "3. 遇到类似题目时，记得先___，再___\n\n" +
            "=== 格式要求 ===\n" +
            "1. 每个【】标题都必须保留\n" +
            "2. 每个步骤的'说明'部分必须写至少2句完整的话\n" +
            "3. 计算过程要写出具体的算式，不能只写结果\n" +
            "4. 总字数不少于500字\n" +
            "5. 语言要像老师在黑板前讲课一样详细和通俗",
            subject, questionContent, correctAnswer, userAnswer, difficulty, subject, userAnswer
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 提取知识点 - 智能识别题目涉及的知识点
     */
    public List<String> extractKnowledgePoints(String subject, String questionContent) {
        String prompt = String.format(
            "请分析以下%s题目，提取出涉及的所有知识点。\n\n" +
            "【题目】%s\n\n" +
            "要求：\n" +
            "1. 只返回知识点列表，每行一个\n" +
            "2. 按照从主要到次要排序\n" +
            "3. 知识点要具体、准确\n" +
            "4. 不要有编号，直接列出知识点名称",
            subject, questionContent
        );
        
        String response = callGLM(prompt, 0.3);
        return Arrays.asList(response.split("\n"));
    }
    
    /**
     * 生成学习路径 - 为学生规划知识点学习顺序
     */
    public String generateLearningPath(String subject, List<String> weakKnowledgePoints) {
        String prompt = String.format(
            "学生在%s学科中，以下知识点掌握较弱：\n%s\n\n" +
            "请为学生制定一个科学的学习路径，包括：\n" +
            "1. 学习顺序（考虑知识点的依赖关系）\n" +
            "2. 每个知识点的学习重点\n" +
            "3. 推荐的练习题类型\n" +
            "4. 预计学习时间\n" +
            "5. 学习建议",
            subject, String.join("、", weakKnowledgePoints)
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 智能推荐相似题目 - 生成同类型题目的描述
     */
    public String recommendSimilarQuestions(String subject, String questionContent, 
                                           String knowledgePoints, String difficulty) {
        String prompt = String.format(
            "基于以下%s错题，推荐3道类似的练习题：\n\n" +
            "【原题】%s\n" +
            "【知识点】%s\n" +
            "【难度】%s\n\n" +
            "要求：\n" +
            "1. 题目要考查相同的知识点\n" +
            "2. 难度要循序渐进（第一题稍简单，第二题同等难度，第三题稍难）\n" +
            "3. 每道题给出完整的题目内容\n" +
            "4. 题目要有区分度，不要完全重复\n" +
            "5. 格式：题目1: [题目内容]\\n题目2: [题目内容]\\n题目3: [题目内容]",
            subject, questionContent, knowledgePoints, difficulty
        );
        
        return callGLM(prompt, 0.8);
    }
    
    /**
     * 班级数据分析 - 为教师生成教学反馈报告
     */
    public String analyzeClassData(String subject, Map<String, Integer> errorStats, 
                                   List<String> topErrorQuestions) {
        String prompt = String.format(
            "请分析以下%s学科的班级错题数据，生成教学反馈报告：\n\n" +
            "【知识点错题统计】\n%s\n\n" +
            "【高频错题】\n%s\n\n" +
            "请从以下角度分析：\n" +
            "1. 整体学习情况评估\n" +
            "2. 薄弱知识点分析\n" +
            "3. 高频错题的共性问题\n" +
            "4. 教学改进建议（具体可操作）\n" +
            "5. 重点关注学生群体\n" +
            "6. 后续教学重点",
            subject, formatErrorStats(errorStats), String.join("\n", topErrorQuestions)
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 智能答疑 - 学生提问AI助手
     */
    public String answerQuestion(String subject, String question, String context) {
        log.warn("=".repeat(60));
        log.warn("📝 收到AI问答请求");
        log.warn("学科: {}", subject);
        log.warn("问题: {}", question);
        log.warn("上下文: {}", context);
        log.warn("=".repeat(60));
        
        String prompt = String.format(
            "你是一位耐心的%s学科AI助教。学生向你提问：\n\n" +
            "【问题】%s\n\n" +
            "%s\n\n" +
            "请用通俗易懂的语言解答，必要时可以举例说明。",
            subject, question, 
            context != null ? "【相关内容】" + context : ""
        );
        
        log.warn("📤 准备调用GLM API");
        log.warn("提示词: {}", prompt.substring(0, Math.min(200, prompt.length())) + "...");
        
        String result = callGLM(prompt, 0.7);
        
        log.warn("=".repeat(60));
        log.warn("✅ GLM API返回结果");
        log.warn("结果长度: {} 字符", result.length());
        log.warn("结果预览: {}", result.substring(0, Math.min(100, result.length())) + "...");
        log.warn("=".repeat(60));
        
        return result;
    }
    
    /**
     * 自动生成题目解析
     */
    public String generateQuestionAnalysis(String subject, String questionContent, String answer) {
        String prompt = String.format(
            "请为以下%s题目生成详细的分步解析：\n\n" +
            "【题目】%s\n" +
            "【答案】%s\n\n" +
            "请按照以下结构生成解析（每个步骤都要详细展开）：\n\n" +
            "【题目类型】\n" +
            "这是一个___题，主要考察___知识点。\n\n" +
            "【第一步：理解题意】\n" +
            "• 已知条件：...\n" +
            "• 求解目标：...\n\n" +
            "【第二步：分析思路】\n" +
            "要解决这道题，需要：\n" +
            "1. 首先做什么（说明原因）\n" +
            "2. 然后做什么（说明原因）\n" +
            "3. 最后做什么（说明原因）\n\n" +
            "【第三步：详细解答过程】\n" +
            "步骤1：（具体计算/推理）\n" +
            "   • 计算：...\n" +
            "   • 说明：为什么这样做\n\n" +
            "步骤2：（继续）\n" +
            "   • 计算：...\n" +
            "   • 说明：...\n\n" +
            "步骤3：（得出答案）\n" +
            "   • 最终答案：...\n\n" +
            "【核心知识点】\n" +
            "• ...\n\n" +
            "【易错提醒】\n" +
            "⚠️ 注意：...\n\n" +
            "【拓展延伸】\n" +
            "...\n\n" +
            "要求：\n" +
            "- 每个步骤都要详细说明\n" +
            "- 用【】标记标题\n" +
            "- 语言通俗易懂\n" +
            "- 重点解释'为什么'而不只是'怎么做'",
            subject, questionContent, answer
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 生成个性化学习报告
     */
    public String generateStudentReport(String studentName, String subject, 
                                       Map<String, Object> learningData) {
        String prompt = String.format(
            "请为学生%s生成%s学科的学习报告：\n\n" +
            "【学习数据】\n" +
            "- 总错题数：%s\n" +
            "- 已订正数：%s\n" +
            "- 薄弱知识点：%s\n" +
            "- 最近错题趋势：%s\n\n" +
            "请生成一份详细的学习报告，包括：\n" +
            "1. 学习成果总结\n" +
            "2. 进步情况分析\n" +
            "3. 存在的主要问题\n" +
            "4. 具体改进建议\n" +
            "5. 下阶段学习目标\n" +
            "6. 鼓励与激励",
            studentName, subject,
            learningData.get("totalErrors"),
            learningData.get("correctedErrors"),
            learningData.get("weakPoints"),
            learningData.get("recentTrend")
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 批量生成练习题
     */
    public String generatePracticeQuestions(String subject, String knowledgePoint, 
                                           String difficulty, int count) {
        String prompt = String.format(
            "请生成%d道%s学科的%s难度练习题，知识点：%s\n\n" +
            "要求：\n" +
            "1. 题目要有区分度\n" +
            "2. 每道题包含题目、答案、解析\n" +
            "3. 格式清晰，便于阅读\n" +
            "4. 题目要有实际应用价值",
            count, subject, difficulty, knowledgePoint
        );
        
        return callGLM(prompt, 0.8);
    }
    
    /**
     * 生成综合学习报告（用于个人中心）
     */
    public String generateLearningReport(String studentName, 
                                        Map<String, Long> subjectStats,
                                        Map<String, Long> difficultyStats,
                                        long totalErrors,
                                        long correctedCount) {
        String prompt = String.format(
            "请为学生%s生成综合学习报告：\n\n" +
            "【整体数据】\n" +
            "- 累计错题：%d题\n" +
            "- 已订正：%d题\n" +
            "- 订正率：%.1f%%\n\n" +
            "【学科分布】\n%s\n\n" +
            "【难度分布】\n%s\n\n" +
            "请从以下角度生成报告：\n" +
            "1. 学习成果总结（用鼓励和积极的语气）\n" +
            "2. 各学科表现分析\n" +
            "3. 进步情况和亮点\n" +
            "4. 需要改进的地方\n" +
            "5. 学习建议（至少3条具体建议）\n" +
            "6. 鼓励和期待\n\n" +
            "要求：\n" +
            "- 语气亲切、鼓励\n" +
            "- 建议要具体可操作\n" +
            "- 适当使用emoji让报告生动\n" +
            "- 总字数300-500字",
            studentName,
            totalErrors,
            correctedCount,
            totalErrors > 0 ? (correctedCount * 100.0 / totalErrors) : 0,
            formatStats(subjectStats),
            formatStats(difficultyStats)
        );
        
        return callGLM(prompt, 0.7);
    }
    
    /**
     * 格式化统计数据为可读文本
     */
    private String formatStats(Map<String, Long> stats) {
        if (stats.isEmpty()) {
            return "暂无数据";
        }
        StringBuilder sb = new StringBuilder();
        stats.forEach((key, value) -> 
            sb.append(String.format("- %s: %d题\n", key, value))
        );
        return sb.toString();
    }
    
    /**
     * 调用GLM API的核心方法（公开 - 供其他服务使用）
     * 带重试机制
     */
    public String callGLM(String prompt, double temperature) {
        // 如果有重试服务,使用带重试的调用
        if (retryService != null) {
            return retryService.executeAICallWithRetry(() -> callGLMInternal(prompt, temperature));
        }
        
        // 否则直接调用
        return callGLMInternal(prompt, temperature);
    }
    
    /**
     * 内部方法：实际调用GLM API
     */
    private String callGLMInternal(String prompt, double temperature) {
        try {
            log.warn("📡 开始调用GLM API");
            log.warn("API URL: {}", apiUrl);
            log.warn("API Key: {}...", apiKey.substring(0, Math.min(20, apiKey.length())));
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4");
            requestBody.put("temperature", temperature);
            requestBody.put("top_p", 0.9);
            requestBody.put("max_tokens", 2000);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
            log.warn("请求体: model=glm-4, temperature={}, max_tokens=2000", temperature);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            log.warn("🚀 发送HTTP请求到GLM API...");
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class
            );
            
            log.warn("📥 收到响应，状态码: {}", response.getStatusCode());
            
            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                log.warn("响应体: {}", response.getBody().substring(0, Math.min(500, response.getBody().length())) + "...");
                
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0)
                          .path("message").path("content").asText();
                
                log.warn("✅ 成功提取AI回复，长度: {}", content.length());
                return content;
            }
            
            log.error("❌ GLM API调用失败: {}", response.getStatusCode());
            log.error("响应体: {}", response.getBody());
            return "AI分析暂时不可用，请稍后重试。状态码：" + response.getStatusCode();
            
        } catch (Exception e) {
            log.error("=".repeat(60));
            log.error("❌ 调用GLM API出错");
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            log.error("=".repeat(60), e);
            return "AI分析出错：" + e.getMessage() + "\n\n请检查GLM API配置是否正确。";
        }
    }
    
    /**
     * 格式化错误统计数据
     */
    private String formatErrorStats(Map<String, Integer> errorStats) {
        StringBuilder sb = new StringBuilder();
        errorStats.forEach((knowledge, count) -> 
            sb.append(String.format("- %s: %d次\n", knowledge, count))
        );
        return sb.toString();
    }
}


