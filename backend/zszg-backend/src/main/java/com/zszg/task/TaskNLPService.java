package com.zszg.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zszg.ai.GLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务NLP智能解析服务
 * 使用AI + 规则引擎智能解析教师的自然语言任务描述
 */
@Slf4j
@Service
public class TaskNLPService {

    @Autowired
    private GLMService glmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 智能解析任务内容（增强版 - 更丰富、更智能）
     */
    public TaskParseResult parseTaskContent(String content) {
        log.info("🧠 开始NLP智能解析任务：{}", content);

        TaskParseResult result = new TaskParseResult();
        result.setOriginalContent(content);

        // 1. 使用GLM AI进行深度语义分析
        String aiAnalysis = analyzeWithAI(content);
        result.setAiAnalysis(aiAnalysis);

        // 2. 提取基础信息
        extractBasicInfo(content, aiAnalysis, result);

        // 3. 提取详细信息
        extractDetailedInfo(content, aiAnalysis, result);

        // 4. 提取学习相关信息
        extractLearningInfo(content, aiAnalysis, result);

        // 5. 生成智能建议
        generateSmartSuggestions(content, aiAnalysis, result);

        log.info("✅ NLP解析完成：{}", result);
        return result;
    }

    /**
     * 提取基础信息
     */
    private void extractBasicInfo(String content, String aiAnalysis, TaskParseResult result) {
        // 标题
        String title = generateTaskTitle(content, aiAnalysis);
        result.setTitle(title);

        // 时间
        LocalDateTime deadline = extractDeadline(content, aiAnalysis);
        result.setDeadline(deadline);

        // 任务类型
        String taskType = extractTaskType(content, aiAnalysis);
        result.setTaskType(taskType);

        // 优先级
        String priority = extractPriority(content, aiAnalysis);
        result.setPriority(priority);
    }

    /**
     * 提取详细信息
     */
    private void extractDetailedInfo(String content, String aiAnalysis, TaskParseResult result) {
        // 地点
        String location = extractLocation(content, aiAnalysis);
        result.setLocation(location);

        // 参与人群
        String participants = extractParticipants(content, aiAnalysis);
        result.setParticipants(participants);

        // 数量要求
        String quantityRequirement = extractQuantityRequirement(content, aiAnalysis);
        result.setQuantityRequirement(quantityRequirement);

        // 智能标签
        List<String> smartTags = generateSmartTags(content, aiAnalysis);
        result.setSmartTags(smartTags);

        // 子任务拆解
        List<SubTask> subTasks = parseSubTasks(content, aiAnalysis);
        result.setSubTasks(subTasks);

        // 重要提醒
        List<String> reminders = extractImportantReminders(content, aiAnalysis);
        result.setImportantReminders(reminders);
    }

    /**
     * 提取学习相关信息
     */
    private void extractLearningInfo(String content, String aiAnalysis, TaskParseResult result) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);

            // 难度
            String difficulty = json.path("difficulty").asText();
            if (!difficulty.isEmpty() && !difficulty.equals("null")) {
                result.setDifficulty(difficulty);
            }

            // 学科
            String subject = json.path("subject").asText();
            if (!subject.isEmpty() && !subject.equals("null")) {
                result.setSubject(subject);
            }

            // 知识点
            JsonNode knowledgePointsNode = json.path("knowledgePoints");
            if (knowledgePointsNode.isArray()) {
                List<String> knowledgePoints = new ArrayList<>();
                for (JsonNode node : knowledgePointsNode) {
                    knowledgePoints.add(node.asText());
                }
                result.setKnowledgePoints(knowledgePoints);
            }

            // 时长要求
            String timeRequirement = json.path("timeRequirement").asText();
            if (!timeRequirement.isEmpty() && !timeRequirement.equals("null")) {
                result.setTimeRequirement(timeRequirement);
            }

            // 学习目标
            JsonNode objectivesNode = json.path("objectives");
            if (objectivesNode.isArray()) {
                List<String> objectives = new ArrayList<>();
                for (JsonNode node : objectivesNode) {
                    objectives.add(node.asText());
                }
                result.setObjectives(objectives);
            }

            // 需要的材料
            JsonNode materialsNode = json.path("materials");
            if (materialsNode.isArray()) {
                List<String> materials = new ArrayList<>();
                for (JsonNode node : materialsNode) {
                    materials.add(node.asText());
                }
                result.setMaterials(materials);
            }

        } catch (Exception e) {
            log.warn("解析学习信息失败", e);
        }
    }

    /**
     * 生成智能建议
     */
    private void generateSmartSuggestions(String content, String aiAnalysis, TaskParseResult result) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);

            // 学习建议
            JsonNode tipsNode = json.path("tips");
            if (tipsNode.isArray()) {
                List<String> tips = new ArrayList<>();
                for (JsonNode node : tipsNode) {
                    tips.add(node.asText());
                }
                result.setTips(tips);
            }

            // 预期成果
            String expectedOutcome = json.path("expectedOutcome").asText();
            if (!expectedOutcome.isEmpty() && !expectedOutcome.equals("null")) {
                result.setExpectedOutcome(expectedOutcome);
            }

            // 评价标准
            JsonNode criteriaNode = json.path("evaluationCriteria");
            if (criteriaNode.isArray()) {
                List<String> criteria = new ArrayList<>();
                for (JsonNode node : criteriaNode) {
                    criteria.add(node.asText());
                }
                result.setEvaluationCriteria(criteria);
            }

            // 相关主题
            JsonNode relatedTopicsNode = json.path("relatedTopics");
            if (relatedTopicsNode.isArray()) {
                List<String> relatedTopics = new ArrayList<>();
                for (JsonNode node : relatedTopicsNode) {
                    relatedTopics.add(node.asText());
                }
                result.setRelatedTopics(relatedTopics);
            }

        } catch (Exception e) {
            log.warn("生成智能建议失败", e);
        }

        // 生成AI备注
        String aiNotes = generateAINotes(content, aiAnalysis, result);
        result.setAiNotes(aiNotes);
    }

    /**
     * 使用GLM AI进行深度语义分析
     */
    private String analyzeWithAI(String content) {
        String prompt = String.format(
            "你是一个智能任务解析助手。请分析以下教师发布的任务内容，提取关键信息。\n\n" +
            "【任务内容】\n%s\n\n" +
            "请以JSON格式返回分析结果（只返回JSON，不要markdown代码块标记）：\n" +
            "{\n" +
            "  \"title\": \"任务标题\",\n" +
            "  \"taskType\": \"作业\",\n" +
            "  \"deadline\": \"明天下午三点\",\n" +
            "  \"priority\": \"普通\",\n" +
            "  \"location\": null,\n" +
            "  \"participants\": \"全体学生\",\n" +
            "  \"quantityRequirement\": \"5道题\",\n" +
            "  \"timeRequirement\": \"1小时\",\n" +
            "  \"difficulty\": \"中等\",\n" +
            "  \"subject\": \"数学\",\n" +
            "  \"knowledgePoints\": [\"函数\", \"导数\"],\n" +
            "  \"smartTags\": [\"课后作业\", \"数学\", \"函数\"],\n" +
            "  \"subTasks\": [\n" +
            "    {\"name\": \"完成错题整理\", \"description\": \"整理第三章错题\"},\n" +
            "    {\"name\": \"标注知识点\", \"description\": \"为每道题标注相关知识点\"}\n" +
            "  ],\n" +
            "  \"reminders\": [\"注意截止时间\", \"记得标注知识点\"],\n" +
            "  \"materials\": [\"错题本\", \"课本\"],\n" +
            "  \"objectives\": [\"巩固函数知识\", \"掌握导数运算\"],\n" +
            "  \"evaluationCriteria\": [\"完成数量达标\", \"知识点标注准确\"],\n" +
            "  \"tips\": [\"先复习课本\", \"分析错误原因\"],\n" +
            "  \"relatedTopics\": [\"函数图像\", \"导数应用\"],\n" +
            "  \"expectedOutcome\": \"掌握函数与导数的基本概念\"\n" +
            "}\n\n" +
            "**关键要求**：\n" +
            "1. **必须返回完整的JSON**，所有字段都要填写（如果任务中没有明确提到某个信息，可以智能推断或填null）\n" +
            "2. **任务类型**：根据内容判断是作业/考试/复习/预习/活动/通知/其他\n" +
            "3. **学科**：识别是数学/语文/英语/物理/化学/生物/历史/地理/政治等\n" +
            "4. **难度**：根据任务要求判断为简单/中等/困难\n" +
            "5. **子任务拆解**：将任务分解为2-4个具体步骤\n" +
            "6. **智能标签**：生成3-5个有意义的标签\n" +
            "7. **学习目标**：提炼出学生完成任务后能获得的学习成果\n" +
            "8. **截止时间**：保留原文（如\"明天\"、\"下周五\"、\"三天后\"）\n" +
            "9. **重要提醒**：提取任务中强调的注意事项\n" +
            "10. **学习建议**：给出2-3条有用的学习方法建议\n\n" +
            "特别注意：\n" +
            "- 请认真分析任务内容，尽量填充所有字段\n" +
            "- 即使任务描述简短，也要智能推断合理的信息\n" +
            "- 数组字段至少要有1-2个元素，不要返回空数组\n" +
            "- 确保JSON格式正确，不要有语法错误",
            content
        );

        try {
            String response = glmService.callGLM(prompt, 0.7); // 提高temperature获得更丰富的结果
            log.info("🤖 AI原始分析结果：{}", response);
            
            // 清理可能的markdown代码块标记
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
            
            log.info("🤖 清理后的JSON：{}", cleaned);
            return cleaned;
        } catch (Exception e) {
            log.error("❌ AI分析失败", e);
            return "{}";
        }
    }

    /**
     * 提取截止时间（重点：将"明天"转换为实际日期）
     */
    private LocalDateTime extractDeadline(String content, String aiAnalysis) {
        LocalDate today = LocalDate.now();
        LocalTime defaultTime = LocalTime.of(23, 59); // 默认截止时间为当天23:59

        // 先尝试从AI分析结果中提取
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String deadlineStr = json.path("deadline").asText();

            if (!deadlineStr.isEmpty() && !deadlineStr.equals("null")) {
                // 解析相对时间表达
                LocalDate targetDate = parseRelativeDate(deadlineStr, today);
                if (targetDate != null) {
                    // 尝试从原文中提取具体时间
                    LocalTime targetTime = extractTime(content);
                    return LocalDateTime.of(targetDate, targetTime != null ? targetTime : defaultTime);
                }
            }
        } catch (Exception e) {
            log.warn("从AI分析中提取时间失败，使用规则引擎", e);
        }

        // 使用规则引擎提取时间
        // 1. 明天
        if (content.contains("明天") || content.contains("明日")) {
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today.plusDays(1), time != null ? time : defaultTime);
        }

        // 2. 后天
        if (content.contains("后天")) {
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today.plusDays(2), time != null ? time : defaultTime);
        }

        // 3. 今天
        if (content.contains("今天") || content.contains("今日")) {
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today, time != null ? time : defaultTime);
        }

        // 4. 本周X
        Pattern weekPattern = Pattern.compile("本周([一二三四五六日天])");
        Matcher weekMatcher = weekPattern.matcher(content);
        if (weekMatcher.find()) {
            String dayStr = weekMatcher.group(1);
            int targetDayOfWeek = getDayOfWeek(dayStr);
            int currentDayOfWeek = today.getDayOfWeek().getValue();
            int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today.plusDays(daysToAdd), time != null ? time : defaultTime);
        }

        // 5. 下周X
        Pattern nextWeekPattern = Pattern.compile("下周([一二三四五六日天])");
        Matcher nextWeekMatcher = nextWeekPattern.matcher(content);
        if (nextWeekMatcher.find()) {
            String dayStr = nextWeekMatcher.group(1);
            int targetDayOfWeek = getDayOfWeek(dayStr);
            int currentDayOfWeek = today.getDayOfWeek().getValue();
            int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7 + 7;
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today.plusDays(daysToAdd), time != null ? time : defaultTime);
        }

        // 6. X天后/X天内
        Pattern daysPattern = Pattern.compile("([0-9一二三四五六七八九十]+)天[后内之]");
        Matcher daysMatcher = daysPattern.matcher(content);
        if (daysMatcher.find()) {
            int days = parseChineseNumber(daysMatcher.group(1));
            LocalTime time = extractTime(content);
            return LocalDateTime.of(today.plusDays(days), time != null ? time : defaultTime);
        }

        // 7. 具体日期 MM-DD 或 M月D日
        Pattern datePattern = Pattern.compile("(\\d{1,2})[月/-](\\d{1,2})");
        Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
            int month = Integer.parseInt(dateMatcher.group(1));
            int day = Integer.parseInt(dateMatcher.group(2));
            LocalDate targetDate = LocalDate.of(today.getYear(), month, day);
            if (targetDate.isBefore(today)) {
                targetDate = targetDate.plusYears(1);
            }
            LocalTime time = extractTime(content);
            return LocalDateTime.of(targetDate, time != null ? time : defaultTime);
        }

        // 默认：如果没有提取到时间，返回null
        return null;
    }

    /**
     * 解析相对时间表达
     */
    private LocalDate parseRelativeDate(String dateStr, LocalDate today) {
        dateStr = dateStr.toLowerCase().trim();

        if (dateStr.contains("明天") || dateStr.contains("明日") || dateStr.equals("tomorrow")) {
            return today.plusDays(1);
        }
        if (dateStr.contains("后天")) {
            return today.plusDays(2);
        }
        if (dateStr.contains("今天") || dateStr.contains("今日") || dateStr.equals("today")) {
            return today;
        }
        if (dateStr.contains("下周一")) return today.plusDays((8 - today.getDayOfWeek().getValue()) % 7 + 7);
        if (dateStr.contains("下周二")) return today.plusDays((9 - today.getDayOfWeek().getValue()) % 7 + 7);
        if (dateStr.contains("下周三")) return today.plusDays((10 - today.getDayOfWeek().getValue()) % 7 + 7);
        if (dateStr.contains("下周四")) return today.plusDays((11 - today.getDayOfWeek().getValue()) % 7 + 7);
        if (dateStr.contains("下周五")) return today.plusDays((12 - today.getDayOfWeek().getValue()) % 7 + 7);

        return null;
    }

    /**
     * 从文本中提取时间
     */
    private LocalTime extractTime(String content) {
        // 1. 优先匹配 "下午X点"、"上午X点"、"晚上X点" 等带时段的格式（必须优先，否则会被"X点"匹配掉）
        Pattern ampmPattern = Pattern.compile("(上午|下午|中午|晚上|早上|凌晨)([0-9一二三四五六七八九十]+)点");
        Matcher ampmMatcher = ampmPattern.matcher(content);
        if (ampmMatcher.find()) {
            String period = ampmMatcher.group(1);
            int hour = parseChineseNumber(ampmMatcher.group(2));
            
            // 根据时段调整小时数
            if ("下午".equals(period)) {
                // 下午1点-下午11点 -> 13:00-23:00
                if (hour >= 1 && hour <= 11) {
                    hour += 12;
                }
            } else if ("晚上".equals(period)) {
                // 晚上6点-晚上11点 -> 18:00-23:00
                if (hour >= 6 && hour <= 11) {
                    hour += 12;
                } else if (hour >= 1 && hour <= 5) {
                    // 晚上1点-晚上5点也可能指深夜，也加12
                    hour += 12;
                }
            } else if ("上午".equals(period) || "早上".equals(period)) {
                // 上午12点 -> 0点
                if (hour == 12) {
                    hour = 0;
                }
            } else if ("中午".equals(period)) {
                // 中午12点
                if (hour == 12) {
                    hour = 12;
                } else if (hour < 12) {
                    hour = 12;
                }
            } else if ("凌晨".equals(period)) {
                // 凌晨1点-5点 -> 1:00-5:00
                // 不需要调整
            }
            
            return LocalTime.of(hour, 0);
        }

        // 2. 匹配 HH:MM 格式
        Pattern timePattern = Pattern.compile("(\\d{1,2})[:：](\\d{1,2})");
        Matcher timeMatcher = timePattern.matcher(content);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            return LocalTime.of(hour, minute);
        }

        // 3. 最后匹配 "X点" 格式（无上下午标识）
        Pattern hourPattern = Pattern.compile("([0-9一二三四五六七八九十]+)点");
        Matcher hourMatcher = hourPattern.matcher(content);
        if (hourMatcher.find()) {
            int hour = parseChineseNumber(hourMatcher.group(1));
            return LocalTime.of(hour, 0);
        }

        return null;
    }

    /**
     * 将中文数字转换为阿拉伯数字
     */
    private int parseChineseNumber(String chinese) {
        Map<Character, Integer> map = new HashMap<>();
        map.put('一', 1); map.put('二', 2); map.put('三', 3);
        map.put('四', 4); map.put('五', 5); map.put('六', 6);
        map.put('七', 7); map.put('八', 8); map.put('九', 9);
        map.put('十', 10);

        try {
            return Integer.parseInt(chinese);
        } catch (NumberFormatException e) {
            // 处理中文数字
            if (chinese.length() == 1) {
                return map.getOrDefault(chinese.charAt(0), 0);
            }
            if (chinese.startsWith("十")) {
                if (chinese.length() == 1) return 10;
                return 10 + map.getOrDefault(chinese.charAt(1), 0);
            }
            if (chinese.endsWith("十")) {
                return map.getOrDefault(chinese.charAt(0), 0) * 10;
            }
            // 如 二十三
            if (chinese.length() == 3 && chinese.charAt(1) == '十') {
                return map.getOrDefault(chinese.charAt(0), 0) * 10 + map.getOrDefault(chinese.charAt(2), 0);
            }
        }
        return 0;
    }

    /**
     * 获取星期几对应的数字（1=周一，7=周日）
     */
    private int getDayOfWeek(String dayStr) {
        Map<String, Integer> map = new HashMap<>();
        map.put("一", 1); map.put("二", 2); map.put("三", 3);
        map.put("四", 4); map.put("五", 5); map.put("六", 6);
        map.put("日", 7); map.put("天", 7);
        return map.getOrDefault(dayStr, 1);
    }

    /**
     * 提取任务类型
     */
    private String extractTaskType(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String taskType = json.path("taskType").asText();
            if (!taskType.isEmpty() && !taskType.equals("null")) {
                return taskType;
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则匹配
        if (content.contains("作业") || content.contains("练习")) return "作业";
        if (content.contains("考试") || content.contains("测验") || content.contains("模考")) return "考试";
        if (content.contains("活动") || content.contains("比赛") || content.contains("竞赛")) return "活动";
        if (content.contains("学习") || content.contains("复习") || content.contains("预习")) return "学习任务";
        if (content.contains("通知") || content.contains("提醒")) return "通知";

        return "学习任务";
    }

    /**
     * 提取优先级
     */
    private String extractPriority(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String priority = json.path("priority").asText();
            if (!priority.isEmpty() && !priority.equals("null")) {
                return priority;
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则匹配
        if (content.contains("紧急") || content.contains("立即") || content.contains("马上") || content.contains("务必")) {
            return "紧急";
        }
        if (content.contains("重要") || content.contains("必须") || content.contains("一定要")) {
            return "重要";
        }

        return "普通";
    }

    /**
     * 提取地点
     */
    private String extractLocation(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String location = json.path("location").asText();
            if (!location.isEmpty() && !location.equals("null")) {
                return location;
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则匹配
        Pattern locationPattern = Pattern.compile("在([^，,。.！!?？\\s]{2,15})[举进行召开]");
        Matcher matcher = locationPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 提取参与人群
     */
    private String extractParticipants(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String participants = json.path("participants").asText();
            if (!participants.isEmpty() && !participants.equals("null")) {
                return participants;
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则匹配
        if (content.contains("全体") || content.contains("所有同学") || content.contains("班级所有")) {
            return "全体学生";
        }
        if (content.contains("部分同学") || content.contains("双差")) {
            return "部分同学";
        }

        return "全体学生";
    }

    /**
     * 提取数量要求
     */
    private String extractQuantityRequirement(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String quantity = json.path("quantityRequirement").asText();
            if (!quantity.isEmpty() && !quantity.equals("null")) {
                return quantity;
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则匹配数量
        Pattern quantityPattern = Pattern.compile("([0-9一二三四五六七八九十百千]+)[道题个件份页张篇]");
        Matcher matcher = quantityPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return "不限";
    }

    /**
     * 生成智能标签
     */
    private List<String> generateSmartTags(String content, String aiAnalysis) {
        List<String> tags = new ArrayList<>();

        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            JsonNode tagsNode = json.path("smartTags");
            if (tagsNode.isArray()) {
                for (JsonNode tag : tagsNode) {
                    tags.add(tag.asText());
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // 如果AI没有生成标签，使用规则生成
        if (tags.isEmpty()) {
            if (content.contains("作业")) tags.add("课后作业");
            if (content.contains("考试") || content.contains("模考")) tags.add("模考双差");
            if (content.contains("活动")) tags.add("专题活动");
            if (content.contains("复习")) tags.add("考前复习");
            if (content.contains("预习")) tags.add("课前预习");
            if (content.contains("练习")) tags.add("配套练习");
            if (content.contains("通知")) tags.add("通知公告");
            if (content.contains("家长")) tags.add("家校沟通");
            if (content.contains("小组")) tags.add("小组学习");
        }

        return tags.isEmpty() ? Arrays.asList("学习任务") : tags;
    }

    /**
     * 拆解子任务
     */
    private List<SubTask> parseSubTasks(String content, String aiAnalysis) {
        List<SubTask> subTasks = new ArrayList<>();

        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            JsonNode subTasksNode = json.path("subTasks");
            if (subTasksNode.isArray()) {
                for (JsonNode taskNode : subTasksNode) {
                    SubTask subTask = new SubTask();
                    subTask.setName(taskNode.path("name").asText());
                    subTask.setDescription(taskNode.path("description").asText());
                    subTasks.add(subTask);
                }
            }
        } catch (Exception e) {
            log.warn("解析子任务失败", e);
        }

        return subTasks;
    }

    /**
     * 提取重要提醒
     */
    private List<String> extractImportantReminders(String content, String aiAnalysis) {
        List<String> reminders = new ArrayList<>();

        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            JsonNode remindersNode = json.path("reminders");
            if (remindersNode.isArray()) {
                for (JsonNode reminder : remindersNode) {
                    reminders.add(reminder.asText());
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // 规则提取
        if (content.contains("注意") || content.contains("提醒") || content.contains("务必")) {
            Pattern reminderPattern = Pattern.compile("[注提务][意醒必][：:】]([^，,。.！!\\n]{5,50})");
            Matcher matcher = reminderPattern.matcher(content);
            if (matcher.find()) {
                reminders.add(matcher.group(1));
            }
        }

        return reminders;
    }

    /**
     * 生成任务标题
     */
    private String generateTaskTitle(String content, String aiAnalysis) {
        try {
            JsonNode json = objectMapper.readTree(aiAnalysis);
            String title = json.path("title").asText();
            if (!title.isEmpty() && !title.equals("null")) {
                return title;
            }
        } catch (Exception e) {
            // ignore
        }

        // 自动生成标题：取第一句话或前30个字符
        String title = content.split("[。！\n]")[0];
        return title.length() > 30 ? title.substring(0, 30) + "..." : title;
    }

    /**
     * 生成AI备注
     */
    private String generateAINotes(String content, String aiAnalysis, TaskParseResult result) {
        StringBuilder notes = new StringBuilder();
        notes.append("AI智能解析结果：\n");

        int infoCount = 0;

        if (result.getDeadline() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
            notes.append("• 截止时间：").append(result.getDeadline().format(formatter)).append("\n");
            infoCount++;
        }

        if (result.getTaskType() != null && !result.getTaskType().isEmpty()) {
            notes.append("• 任务类型：").append(result.getTaskType()).append("\n");
            infoCount++;
        }

        if (result.getSubject() != null && !result.getSubject().isEmpty()) {
            notes.append("• 学科：").append(result.getSubject()).append("\n");
            infoCount++;
        }

        if (result.getDifficulty() != null && !result.getDifficulty().isEmpty()) {
            notes.append("• 难度：").append(result.getDifficulty()).append("\n");
            infoCount++;
        }

        if (!result.getSmartTags().isEmpty()) {
            notes.append("• 智能标签：").append(String.join("、", result.getSmartTags())).append("\n");
            infoCount++;
        }

        if (!result.getSubTasks().isEmpty()) {
            notes.append("• 任务拆解：").append(result.getSubTasks().size()).append(" 个步骤\n");
            infoCount++;
        }

        if (!result.getObjectives().isEmpty()) {
            notes.append("• 学习目标：").append(result.getObjectives().size()).append(" 项\n");
            infoCount++;
        }

        if (!result.getImportantReminders().isEmpty()) {
            notes.append("• 重要提醒：").append(result.getImportantReminders().size()).append(" 条\n");
            infoCount++;
        }

        notes.append("\n共提取 ").append(infoCount).append(" 类信息");

        return notes.toString();
    }

    /**
     * 任务解析结果（增强版 - 包含更多字段）
     */
    public static class TaskParseResult {
        // 基础信息
        private String originalContent;
        private String aiAnalysis;
        private String title;
        private String taskType;
        private LocalDateTime deadline;
        private String priority;
        
        // 详细信息
        private String location;
        private String participants;
        private String quantityRequirement;
        private String timeRequirement;
        private String difficulty;
        private String subject;
        
        // 智能分析
        private List<String> smartTags = new ArrayList<>();
        private List<String> knowledgePoints = new ArrayList<>();
        private List<SubTask> subTasks = new ArrayList<>();
        private List<String> importantReminders = new ArrayList<>();
        
        // 学习指导
        private List<String> objectives = new ArrayList<>();
        private List<String> materials = new ArrayList<>();
        private List<String> tips = new ArrayList<>();
        private List<String> evaluationCriteria = new ArrayList<>();
        private List<String> relatedTopics = new ArrayList<>();
        private String expectedOutcome;
        
        // AI备注
        private String aiNotes;

        // Getters and Setters
        public String getOriginalContent() { return originalContent; }
        public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
        public String getAiAnalysis() { return aiAnalysis; }
        public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getParticipants() { return participants; }
        public void setParticipants(String participants) { this.participants = participants; }
        public String getQuantityRequirement() { return quantityRequirement; }
        public void setQuantityRequirement(String quantityRequirement) { this.quantityRequirement = quantityRequirement; }
        public String getTimeRequirement() { return timeRequirement; }
        public void setTimeRequirement(String timeRequirement) { this.timeRequirement = timeRequirement; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public List<String> getSmartTags() { return smartTags; }
        public void setSmartTags(List<String> smartTags) { this.smartTags = smartTags; }
        public List<String> getKnowledgePoints() { return knowledgePoints; }
        public void setKnowledgePoints(List<String> knowledgePoints) { this.knowledgePoints = knowledgePoints; }
        public List<SubTask> getSubTasks() { return subTasks; }
        public void setSubTasks(List<SubTask> subTasks) { this.subTasks = subTasks; }
        public List<String> getImportantReminders() { return importantReminders; }
        public void setImportantReminders(List<String> importantReminders) { this.importantReminders = importantReminders; }
        public List<String> getObjectives() { return objectives; }
        public void setObjectives(List<String> objectives) { this.objectives = objectives; }
        public List<String> getMaterials() { return materials; }
        public void setMaterials(List<String> materials) { this.materials = materials; }
        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
        public List<String> getEvaluationCriteria() { return evaluationCriteria; }
        public void setEvaluationCriteria(List<String> evaluationCriteria) { this.evaluationCriteria = evaluationCriteria; }
        public List<String> getRelatedTopics() { return relatedTopics; }
        public void setRelatedTopics(List<String> relatedTopics) { this.relatedTopics = relatedTopics; }
        public String getExpectedOutcome() { return expectedOutcome; }
        public void setExpectedOutcome(String expectedOutcome) { this.expectedOutcome = expectedOutcome; }
        public String getAiNotes() { return aiNotes; }
        public void setAiNotes(String aiNotes) { this.aiNotes = aiNotes; }

        @Override
        public String toString() {
            return "TaskParseResult{" +
                    "title='" + title + '\'' +
                    ", taskType='" + taskType + '\'' +
                    ", deadline=" + deadline +
                    ", priority='" + priority + '\'' +
                    ", difficulty='" + difficulty + '\'' +
                    ", subject='" + subject + '\'' +
                    ", smartTags=" + smartTags +
                    ", subTasks=" + subTasks.size() +
                    ", objectives=" + objectives.size() +
                    '}';
        }
    }

    /**
     * 子任务
     */
    public static class SubTask {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

