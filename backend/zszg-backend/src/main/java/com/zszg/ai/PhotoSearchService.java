package com.zszg.ai;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 拍照搜题服务 - 真实可用的AI搜题功能
 * 
 * 流程：
 * 1. OCR识别图片 → 获取题目文字
 * 2. GLM-4分析题目 → 生成详细解答
 * 3. 提取知识点
 * 4. 返回完整答案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoSearchService {
    
    private final OCRService ocrService;
    private final GLMService glmService;
    
    /**
     * 拍照搜题 - 完整流程
     */
    public SearchResult searchQuestion(MultipartFile imageFile, String subject) throws IOException {
        log.info("开始拍照搜题, 文件={}, 学科={}", imageFile.getOriginalFilename(), subject);
        
        SearchResult result = new SearchResult();
        
        // 第1步：OCR识别图片
        log.info("步骤1：OCR识别图片内容");
        OCRService.OCRResult ocrResult = ocrService.recognizeImage(imageFile, OCRService.OCRType.MIXED);
        
        if (!ocrResult.isSuccess()) {
            result.setSuccess(false);
            result.setErrorMessage(ocrResult.getErrorMessage() != null ? 
                ocrResult.getErrorMessage() : "图片识别失败");
            return result;
        }
        
        String questionText = ocrResult.getText();
        result.setQuestionText(questionText);
        result.setImagePath(ocrResult.getOriginalImage());
        result.setOcrConfidence(ocrResult.getConfidence());
        
        // 检查是否识别到内容
        if (questionText == null || questionText.trim().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("未识别到题目内容。\n\n可能原因：\n1. 图片中没有文字\n2. 图片质量过低\n3. 图片角度不正确\n\n请重新拍摄清晰的题目图片。");
            return result;
        }
        
        log.info("OCR识别完成，文字长度={}", questionText.length());
        
        // 第2步：GLM-4智能分析题目
        log.info("步骤2：AI分析题目并生成解答");
        String answer = analyzeQuestion(questionText, subject);
        result.setAnswer(answer);
        
        log.info("AI解答完成，长度={}", answer.length());
        
        // 第3步：提取知识点
        log.info("步骤3：提取知识点");
        try {
            List<String> knowledgePoints = glmService.extractKnowledgePoints(subject, questionText);
            result.setKnowledgePoints(knowledgePoints);
        } catch (Exception e) {
            log.warn("知识点提取失败，使用默认值", e);
            result.setKnowledgePoints(new ArrayList<>());
        }
        
        // 第4步：生成相似题推荐（可选）
        log.info("步骤4：生成学习建议");
        String suggestion = generateSuggestion(questionText, subject);
        result.setSuggestion(suggestion);
        
        result.setSuccess(true);
        log.info("拍照搜题完成！");
        
        return result;
    }
    
    /**
     * 使用GLM-4分析题目并生成详细解答
     */
    private String analyzeQuestion(String questionText, String subject) {
        String prompt = String.format(
            "你是一位专业的%s老师。学生拍照上传了一道题目，请详细解答。\n\n" +
            "【题目内容】\n%s\n\n" +
            "请按以下格式回答：\n\n" +
            "## 题目分析\n" +
            "（简要说明这是什么类型的题目，考查什么知识点）\n\n" +
            "## 解题思路\n" +
            "（详细的解题思路和步骤）\n\n" +
            "## 完整解答\n" +
            "（详细的解题过程，包括每一步的计算）\n\n" +
            "## 答案\n" +
            "（最终答案）\n\n" +
            "## 知识点总结\n" +
            "（涉及的核心知识点）\n\n" +
            "## 易错提醒\n" +
            "（这类题目的常见错误和注意事项）\n\n" +
            "请用清晰、易懂的语言，帮助学生真正理解这道题。",
            subject, questionText
        );
        
        return glmService.callGLM(prompt, 0.7);
    }
    
    /**
     * 生成学习建议
     */
    private String generateSuggestion(String questionText, String subject) {
        String prompt = String.format(
            "基于这道%s题目：\n%s\n\n" +
            "请给学生提供3条学习建议（每条建议一行，不要编号）：",
            subject, questionText
        );
        
        try {
            return glmService.callGLM(prompt, 0.7);
        } catch (Exception e) {
            log.warn("学习建议生成失败", e);
            return "1. 多做类似题目巩固\n2. 注意基础概念的理解\n3. 总结解题规律";
        }
    }
    
    /**
     * 快速搜题（仅返回答案，不分析）
     */
    public String quickSearch(String questionText, String subject) {
        String prompt = String.format(
            "请直接给出这道%s题的答案和简要解析：\n\n%s",
            subject, questionText
        );
        
        return glmService.callGLM(prompt, 0.5);
    }
    
    /**
     * 搜题结果
     */
    @Data
    public static class SearchResult {
        private boolean success;
        private String errorMessage;
        
        // OCR相关
        private String imagePath;           // 图片路径
        private String questionText;        // 识别的题目文字
        private double ocrConfidence;       // OCR置信度
        
        // AI解答相关
        private String answer;              // AI生成的完整解答
        private List<String> knowledgePoints;  // 知识点列表
        private String suggestion;          // 学习建议
        
        // 推荐内容
        private List<SimilarQuestion> similarQuestions;  // 相似题目
    }
    
    /**
     * 相似题目
     */
    @Data
    public static class SimilarQuestion {
        private String content;
        private String answer;
        private String difficulty;
    }
}

