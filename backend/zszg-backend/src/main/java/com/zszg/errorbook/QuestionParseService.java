package com.zszg.errorbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zszg.ai.GLMService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目智能解析服务
 * 根据题目内容自动识别学科、难度、答案、解析等信息
 */
@Slf4j
@Service
public class QuestionParseService {

    @Autowired
    private GLMService glmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 智能解析题目内容
     */
    public QuestionParseResult parseQuestion(String questionContent) {
        log.info("🧠 开始智能解析题目：{}", questionContent.substring(0, Math.min(50, questionContent.length())) + "...");

        QuestionParseResult result = new QuestionParseResult();
        result.setOriginalContent(questionContent);

        try {
            // 调用GLM AI进行智能分析
            String aiResponse = analyzeQuestionWithAI(questionContent);
            log.info("✅ AI分析完成");

            // 解析AI返回的JSON结果
            parseAIResponse(aiResponse, result);

        } catch (Exception e) {
            log.error("智能解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("解析失败：" + e.getMessage());

            // 失败时使用规则引擎尝试基础识别
            fallbackParse(questionContent, result);
        }

        log.info("📋 解析结果：学科={}, 难度={}, 成功={}", 
                 result.getSubject(), result.getDifficulty(), result.isSuccess());
        return result;
    }

    /**
     * 使用GLM AI进行深度语义分析
     */
    private String analyzeQuestionWithAI(String questionContent) {
        String prompt = String.format(
            "你是一个智能题目分析助手。请**深入、全面、细致**地分析以下题目内容：\n\n" +
            "【题目内容】\n%s\n\n" +
            "请以JSON格式返回**详细的分析结果**（只返回JSON，不要其他说明文字）：\n" +
            "{\n" +
            "  \"subject\": \"学科名称（数学/语文/英语/物理/化学/生物/历史/地理/政治，必须从这些选项中选一个）\",\n" +
            "  \"difficulty\": \"难度级别（简单/中等/困难，必须从这三个选项中选一个）\",\n" +
            "  \"questionType\": \"题目类型（选择题/填空题/计算题/解答题/应用题/证明题/作文题等）\",\n" +
            "  \"answer\": \"正确答案（⚠️重要：对于计算题、填空题等，必须计算并给出具体答案！）\",\n" +
            "  \"analysis\": \"详细解析（必须非常详细，包含完整的解题步骤，至少200字）\",\n" +
            "  \"knowledgePoints\": [\"知识点1\", \"知识点2\", \"知识点3\"],\n" +
            "  \"errorPoints\": [\"易错点1\", \"易错点2\"],\n" +
            "  \"tags\": [\"标签1\", \"标签2\", \"标签3\"],\n" +
            "  \"tips\": \"解题技巧和注意事项（至少2句话）\",\n" +
            "  \"confidence\": \"识别置信度（高/中/低）\",\n" +
            "  \"reasoning\": \"为什么判断是这个学科和难度（说明理由）\"\n" +
            "}\n\n" +
            "=== 分析要求 ===\n" +
            "1. 学科判断：根据题目内容、术语、符号等综合判断\n" +
            "   - 数学：数字、公式、几何、代数、函数等\n" +
            "   - 语文：文学、语法、阅读理解、作文等\n" +
            "   - 英语：英文单词、语法、翻译等\n" +
            "   - 物理：力、能量、运动、光、电等\n" +
            "   - 化学：元素、反应、物质等\n" +
            "   - 生物：细胞、遗传、生态等\n" +
            "   - 其他：根据关键词判断\n\n" +
            "2. 难度判断：\n" +
            "   - 简单：基础概念、直接套用公式、一步计算\n" +
            "   - 中等：需要多步骤、综合运用、有一定思考\n" +
            "   - 困难：复杂推理、综合多个知识点、需要深度理解\n\n" +
            "3. ⚠️【关键】答案计算与提取（必须认真对待）：\n" +
            "   - 对于计算题、填空题：必须完整计算，给出最终答案\n" +
            "   - 对于选择题：如果能推理，给出正确选项\n" +
            "   - 对于解答题：给出完整答案或解答要点\n" +
            "   - 如果题目中已经提到答案（如错题本场景），提取出来\n" +
            "   - 例如：填空题有多个空，答案格式为 \"周长：31.4米，面积：78.5平方米\"\n" +
            "   - 例如：选择题答案格式为 \"B\" 或 \"B. 选项内容\"\n\n" +
            "4. 【重要】解析要求（必须非常详细，是整个解析的核心）：\n" +
            "   ⚠️ 解析字段必须写得非常详细，至少200字！\n" +
            "   \n" +
            "   解析必须包含以下结构：\n" +
            "   \n" +
            "   第一步：理解题意\n" +
            "   - 列出所有已知条件（具体数字和关系）\n" +
            "   - 明确题目要求我们求什么\n" +
            "   \n" +
            "   第二步：分析思路\n" +
            "   - 说明解题的整体思路（为什么这样做）\n" +
            "   - 需要用到哪些知识点或公式\n" +
            "   \n" +
            "   第三步：详细计算过程\n" +
            "   - 步骤1：具体做什么，列出计算式子\n" +
            "   - 步骤2：继续计算，说明每一步的依据\n" +
            "   - 步骤3：得出最终答案\n" +
            "   \n" +
            "   第四步：验证答案\n" +
            "   - 检查答案是否合理\n" +
            "   - 有没有符合题目要求\n" +
            "   \n" +
            "   例如（应用题示例）：\n" +
            "   \"第一步：理解题意。商店原有10kg糖，上午卖出了2/5，也就是卖出了10×2/5=4kg。下午又补货了2/5，这里的2/5指的是下午补货了原来的2/5，也就是10×2/5=4kg。题目问的是：现在的糖量还是10kg吗？为什么？\n" +
            "   \n" +
            "   第二步：分析思路。这是一道分数应用题。关键是要分清楚两个2/5的含义：第一个2/5是卖出的量，第二个2/5是补货的量。由于卖出和补货的量相等，所以最终糖量应该不变。\n" +
            "   \n" +
            "   第三步：详细计算。\n" +
            "   步骤1：计算上午卖出多少 = 10 × 2/5 = 4kg\n" +
            "   步骤2：上午卖出后剩余 = 10 - 4 = 6kg  \n" +
            "   步骤3：计算下午补货多少 = 10 × 2/5 = 4kg（这里的2/5是原来10kg的2/5）\n" +
            "   步骤4：最终糖量 = 6 + 4 = 10kg\n" +
            "   \n" +
            "   第四步：验证答案。确实还是10kg，因为卖出的和补货的数量相等，都是原来的2/5。\"\n" +
            "   \n" +
            "   ⚠️ 每个步骤都要写得很详细，要有具体的计算过程和说明！\n\n" +
            "5. 知识点：提取3-5个核心知识点\n" +
            "6. 易错点：列出容易出错的地方，要具体说明容易在哪里出错\n" +
            "7. 标签：生成3-5个有助于分类和搜索的标签\n\n" +
            "=== 注意事项 ===\n" +
            "- 必须返回有效的JSON格式\n" +
            "- analysis字段必须非常详细，至少200字，包含完整的解题步骤\n" +
            "- 每个计算步骤都要列出具体的算式\n" +
            "- 要解释每一步为什么这样做\n" +
            "- 分析要专业、准确、有深度\n" +
            "- 如果不确定，在reasoning中说明原因\n",
            questionContent
        );

        return glmService.callGLM(prompt, 0.3);
    }

    /**
     * 解析AI返回的JSON结果
     */
    private void parseAIResponse(String aiResponse, QuestionParseResult result) {
        try {
            // 提取JSON部分（去除可能的markdown包装）
            String jsonStr = extractJSON(aiResponse);

            JsonNode json = objectMapper.readTree(jsonStr);

            // 提取各个字段，并转换分数格式
            result.setSubject(getStringValue(json, "subject"));
            result.setDifficulty(getStringValue(json, "difficulty"));
            result.setQuestionType(getStringValue(json, "questionType"));
            result.setAnswer(convertLatexFractionsToSimple(getStringValue(json, "answer")));
            result.setAnalysis(convertLatexFractionsToSimple(getStringValue(json, "analysis")));
            result.setTips(convertLatexFractionsToSimple(getStringValue(json, "tips")));
            result.setConfidence(getStringValue(json, "confidence"));
            result.setReasoning(getStringValue(json, "reasoning"));

            // 提取数组字段
            result.setKnowledgePoints(getArrayValues(json, "knowledgePoints"));
            result.setErrorPoints(getArrayValues(json, "errorPoints"));
            result.setTags(getArrayValues(json, "tags"));

            result.setSuccess(true);

        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            throw new RuntimeException("解析AI响应失败：" + e.getMessage());
        }
    }
    
    /**
     * 将LaTeX分数格式转换为简单格式
     * 例如：\frac{2}{5} → 2/5
     */
    private String convertLatexFractionsToSimple(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 处理 \frac{分子}{分母} 格式
        // 使用正则表达式匹配 \frac{...}{...}
        String result = text;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(result);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String numerator = matcher.group(1);    // 分子
            String denominator = matcher.group(2);  // 分母
            matcher.appendReplacement(sb, numerator + "/" + denominator);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 从响应中提取JSON字符串
     */
    private String extractJSON(String response) {
        // 去除markdown代码块标记
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        
        // 🔧 关键修复：转义JSON字符串中的控制字符
        // 这是为了处理AI返回的JSON中可能包含的未转义换行符、制表符等
        cleaned = escapeControlCharacters(cleaned);
        
        return cleaned;
    }
    
    /**
     * 转义字符串中的控制字符
     * 防止JSON解析时出现 "Illegal unquoted character" 错误
     */
    private String escapeControlCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // 追踪是否在字符串字面量内
            if (c == '"' && !escaped) {
                inString = !inString;
                result.append(c);
                continue;
            }
            
            // 追踪转义状态
            if (c == '\\' && !escaped) {
                escaped = true;
                result.append(c);
                continue;
            }
            
            // 如果在字符串内且遇到控制字符，进行转义
            if (inString && !escaped) {
                switch (c) {
                    case '\n':
                        result.append("\\n");
                        break;
                    case '\r':
                        result.append("\\r");
                        break;
                    case '\t':
                        result.append("\\t");
                        break;
                    case '\f':
                        result.append("\\f");
                        break;
                    case '\b':
                        result.append("\\b");
                        break;
                    default:
                        // 其他控制字符（ASCII < 32）
                        if (c < 32) {
                            result.append(String.format("\\u%04x", (int) c));
                        } else {
                            result.append(c);
                        }
                        break;
                }
            } else {
                result.append(c);
            }
            
            // 重置转义状态
            if (escaped && c != '\\') {
                escaped = false;
            }
        }
        
        return result.toString();
    }

    /**
     * 从JSON中安全获取字符串值
     */
    private String getStringValue(JsonNode json, String fieldName) {
        if (json.has(fieldName) && !json.get(fieldName).isNull()) {
            return json.get(fieldName).asText();
        }
        return "";
    }

    /**
     * 从JSON中获取数组值
     */
    private List<String> getArrayValues(JsonNode json, String fieldName) {
        List<String> values = new ArrayList<>();
        if (json.has(fieldName) && json.get(fieldName).isArray()) {
            for (JsonNode item : json.get(fieldName)) {
                values.add(item.asText());
            }
        }
        return values;
    }

    /**
     * 规则引擎后备方案
     * 当AI解析失败时，使用简单规则进行基础识别
     */
    private void fallbackParse(String questionContent, QuestionParseResult result) {
        log.info("🔄 使用规则引擎进行基础识别");

        String contentLower = questionContent.toLowerCase();

        // 简单的学科识别规则
        if (containsAny(contentLower, "函数", "方程", "几何", "三角形", "面积", "体积", "sin", "cos", "tan", 
                        "x", "y", "z", "=", "+", "-", "×", "÷", "²", "³", "√", "∫", "求解", "计算", "证明")) {
            result.setSubject("数学");
        } else if (containsAny(contentLower, "力", "速度", "加速度", "能量", "功率", "电流", "电压", "电阻", 
                              "牛顿", "焦耳", "瓦特", "光", "声", "热")) {
            result.setSubject("物理");
        } else if (containsAny(contentLower, "元素", "分子", "原子", "化学式", "反应", "氧化", "还原", "酸", "碱", "盐")) {
            result.setSubject("化学");
        } else if (containsAny(contentLower, "细胞", "基因", "dna", "rna", "遗传", "进化", "生态", "光合作用", "呼吸作用")) {
            result.setSubject("生物");
        } else if (containsAny(contentLower, "the", "is", "are", "was", "were", "have", "has", "do", "does", 
                              "a", "an", "this", "that", "translate", "grammar")) {
            result.setSubject("英语");
        } else if (containsAny(contentLower, "古文", "文言文", "诗词", "成语", "语法", "修辞", "作文", "阅读理解", 
                              "议论文", "说明文", "记叙文")) {
            result.setSubject("语文");
        } else {
            result.setSubject("其他");
        }

        // 简单的难度识别规则
        int length = questionContent.length();
        if (length < 50 || contentLower.contains("简单") || contentLower.contains("基础")) {
            result.setDifficulty("简单");
        } else if (length > 200 || contentLower.contains("困难") || contentLower.contains("复杂") || 
                   contentLower.contains("综合")) {
            result.setDifficulty("困难");
        } else {
            result.setDifficulty("中等");
        }

        result.setSuccess(true);
        result.setConfidence("低");
        result.setReasoning("AI解析失败，使用规则引擎进行基础识别");
    }

    /**
     * 检查字符串是否包含任意关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 题目解析结果
     */
    @Data
    public static class QuestionParseResult {
        private boolean success;
        private String errorMessage;

        // 原始内容
        private String originalContent;

        // 识别结果
        private String subject;          // 学科
        private String difficulty;       // 难度
        private String questionType;     // 题目类型
        private String answer;           // 答案
        private String analysis;         // 解析
        private String tips;             // 提示
        private String confidence;       // 置信度
        private String reasoning;        // 推理过程

        // 列表字段
        private List<String> knowledgePoints = new ArrayList<>();  // 知识点
        private List<String> errorPoints = new ArrayList<>();      // 易错点
        private List<String> tags = new ArrayList<>();             // 标签
    }
}

