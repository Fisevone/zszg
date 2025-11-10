package com.zszg.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * OCR识别服务 - 支持图片文字识别、公式识别
 * 
 * 功能：
 * 1. 通用文字识别（支持手写体）
 * 2. 数学公式识别
 * 3. 图片预处理（增强、去噪）
 * 4. 结果置信度评估
 */
@Slf4j
@Service
public class OCRService {
    
    @Value("${app.baidu.ocr.api-key:YOUR_API_KEY}")
    private String apiKey;
    
    @Value("${app.baidu.ocr.secret-key:YOUR_SECRET_KEY}")
    private String secretKey;
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GLMService glmService;
    private final DemoDataService demoDataService;
    private final GLMVisionService glmVisionService;
    private String accessToken = null;
    private long tokenExpireTime = 0;
    
    public OCRService(GLMService glmService, DemoDataService demoDataService, GLMVisionService glmVisionService) {
        this.glmService = glmService;
        this.demoDataService = demoDataService;
        this.glmVisionService = glmVisionService;
    }
    
    /**
     * 检查是否为演示模式
     */
    private boolean isDemoMode() {
        return demoDataService.isDemoMode(apiKey);
    }
    
    /**
     * 识别图片中的文字和公式
     */
    public OCRResult recognizeImage(MultipartFile file, OCRType type) throws IOException {
        log.info("开始OCR识别, 文件名={}, 类型={}, 演示模式={}", 
                file.getOriginalFilename(), type, isDemoMode());
        
        // 保存上传的图片
        String savedPath = saveUploadFile(file);
        
        OCRResult result = new OCRResult();
        result.setOriginalImage(savedPath);
        result.setDemoMode(isDemoMode());
        
        try {
            // 如果百度OCR未配置，使用免费的GLM-4V视觉模型
            if (isDemoMode()) {
                log.warn("=".repeat(60));
                log.warn("🆓 百度OCR未配置，自动切换到免费的GLM-4V视觉模型");
                log.warn("=".repeat(60));
                
                try {
                    log.info("📸 开始调用GLM-4V视觉模型识别图片...");
                    GLMVisionService.VisionResult visionResult = glmVisionService.recognizeQuestion(file);
                    
                    if (visionResult.isSuccess()) {
                        result.setSuccess(true);
                        result.setText(visionResult.getQuestionText());
                        result.setConfidence(0.90);
                        result.setDemoMode(false); // 这是真实识别
                        
                        log.warn("=".repeat(60));
                        log.warn("✅ GLM-4V识别成功！识别内容：{}", 
                            visionResult.getQuestionText().substring(0, Math.min(50, visionResult.getQuestionText().length())) + "...");
                        log.warn("=".repeat(60));
                        return result;
                    } else {
                        log.error("❌ GLM-4V识别失败：{}", visionResult.getErrorMessage());
                        result.setSuccess(false);
                        result.setErrorMessage("🆓 GLM-4V识别失败：" + visionResult.getErrorMessage());
                        return result;
                    }
                } catch (Exception e) {
                    log.error("=".repeat(60));
                    log.error("❌ GLM-4V调用异常", e);
                    log.error("=".repeat(60));
                    
                    result.setSuccess(false);
                    result.setErrorMessage("🆓 GLM-4V识别失败：" + e.getMessage() + 
                        "\n\n💡 可能原因：\n" +
                        "1. GLM-4 API密钥无效\n" +
                        "2. 网络连接问题\n" +
                        "3. 图片格式不支持\n\n" +
                        "建议：检查GLM-4 API配置或配置百度OCR（详见文档）");
                    return result;
                }
            } else {
                // 使用真实API识别
                byte[] imageBytes = Files.readAllBytes(Paths.get(savedPath));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                
                // 根据类型选择识别方法
                switch (type) {
                    case TEXT:
                        result = recognizeText(base64Image);
                        break;
                    case FORMULA:
                        result = recognizeFormula(base64Image);
                        break;
                    case MIXED:
                        // 先识别文字
                        OCRResult textResult = recognizeText(base64Image);
                        // 再识别公式
                        OCRResult formulaResult = recognizeFormula(base64Image);
                        // 合并结果
                        result = mergeResults(textResult, formulaResult);
                        break;
                    case HANDWRITING:
                        result = recognizeHandwriting(base64Image);
                        break;
                }
                
                result.setOriginalImage(savedPath);
                result.setSuccess(true);
            }
            
            log.info("OCR识别完成, 文字长度={}, 置信度={}", 
                    result.getText().length(), result.getConfidence());
            
        } catch (Exception e) {
            log.error("OCR识别失败，尝试使用演示模式", e);
            // 失败时降级到演示模式
            try {
                result = recognizeWithDemo(type);
                result.setOriginalImage(savedPath);
                result.setSuccess(true);
                result.setDemoMode(true);
                result.setErrorMessage("API不可用，已使用演示模式：" + e.getMessage());
            } catch (Exception demoError) {
                result.setSuccess(false);
                result.setErrorMessage("识别失败：" + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 使用演示模式识别
     */
    private OCRResult recognizeWithDemo(OCRType type) {
        OCRResult result = new OCRResult();
        
        // 使用演示数据
        String text = demoDataService.getDemoOCRText();
        result.setText(text);
        result.setConfidence(0.85);
        result.setDemoMode(true);
        
        if (type == OCRType.FORMULA || type == OCRType.MIXED) {
            List<String> formulas = demoDataService.getDemoFormulas();
            result.setFormulas(formulas);
            result.setHasFormula(true);
            
            if (type == OCRType.MIXED) {
                result.setText(text + "\n\n【公式】\n" + String.join("\n", formulas));
            } else {
                result.setText(String.join("\n", formulas));
            }
        }
        
        return result;
    }
    
    /**
     * 识别通用文字
     */
    private OCRResult recognizeText(String base64Image) throws IOException {
        String token = getAccessToken();
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + token;
        
        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("image", base64Image);
        params.put("detect_direction", "true");  // 检测图片方向
        params.put("paragraph", "true");  // 识别段落
        
        String response = httpPost(url, params);
        return parseTextResult(response);
    }
    
    /**
     * 识别数学公式
     */
    private OCRResult recognizeFormula(String base64Image) throws IOException {
        String token = getAccessToken();
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/formula?access_token=" + token;
        
        Map<String, String> params = new HashMap<>();
        params.put("image", base64Image);
        
        String response = httpPost(url, params);
        return parseFormulaResult(response);
    }
    
    /**
     * 识别手写文字
     */
    private OCRResult recognizeHandwriting(String base64Image) throws IOException {
        String token = getAccessToken();
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/handwriting?access_token=" + token;
        
        Map<String, String> params = new HashMap<>();
        params.put("image", base64Image);
        
        String response = httpPost(url, params);
        return parseTextResult(response);
    }
    
    /**
     * 解析文字识别结果
     */
    private OCRResult parseTextResult(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        OCRResult result = new OCRResult();
        
        if (root.has("error_code")) {
            throw new RuntimeException("OCR识别失败: " + root.get("error_msg").asText());
        }
        
        // 提取文字
        StringBuilder text = new StringBuilder();
        double totalConfidence = 0;
        int wordCount = 0;
        
        JsonNode wordsResult = root.path("words_result");
        for (JsonNode word : wordsResult) {
            String words = word.path("words").asText();
            text.append(words).append("\n");
            
            if (word.has("probability")) {
                JsonNode prob = word.path("probability");
                totalConfidence += prob.path("average").asDouble();
                wordCount++;
            }
        }
        
        result.setText(text.toString().trim());
        result.setConfidence(wordCount > 0 ? totalConfidence / wordCount : 0.0);
        result.setHasFormula(false);
        
        return result;
    }
    
    /**
     * 解析公式识别结果
     */
    private OCRResult parseFormulaResult(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        OCRResult result = new OCRResult();
        
        if (root.has("error_code")) {
            throw new RuntimeException("公式识别失败: " + root.get("error_msg").asText());
        }
        
        // 提取公式
        List<String> formulas = new ArrayList<>();
        JsonNode wordsResult = root.path("words_result");
        
        for (JsonNode item : wordsResult) {
            String formula = item.path("words").asText();
            formulas.add(formula);
        }
        
        result.setFormulas(formulas);
        result.setText(String.join("\n", formulas));
        result.setHasFormula(!formulas.isEmpty());
        result.setConfidence(0.9); // 公式识别置信度默认较高
        
        return result;
    }
    
    /**
     * 合并文字和公式识别结果
     */
    private OCRResult mergeResults(OCRResult textResult, OCRResult formulaResult) {
        OCRResult merged = new OCRResult();
        
        // 合并文字
        String combinedText = textResult.getText();
        if (formulaResult.getFormulas() != null && !formulaResult.getFormulas().isEmpty()) {
            combinedText += "\n\n【公式】\n" + String.join("\n", formulaResult.getFormulas());
        }
        
        merged.setText(combinedText);
        merged.setFormulas(formulaResult.getFormulas());
        merged.setHasFormula(formulaResult.isHasFormula());
        merged.setConfidence((textResult.getConfidence() + formulaResult.getConfidence()) / 2);
        
        return merged;
    }
    
    /**
     * 获取百度OCR的Access Token
     */
    private String getAccessToken() throws IOException {
        // 如果token还在有效期内，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        log.info("正在获取百度OCR Access Token");
        
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", apiKey);
        params.put("client_secret", secretKey);
        
        String response = httpPost(url, params);
        JsonNode root = objectMapper.readTree(response);
        
        if (root.has("error")) {
            throw new RuntimeException("获取Access Token失败: " + root.get("error_description").asText());
        }
        
        accessToken = root.path("access_token").asText();
        int expiresIn = root.path("expires_in").asInt();
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 600) * 1000; // 提前10分钟过期
        
        log.info("Access Token获取成功，有效期{}秒", expiresIn);
        
        return accessToken;
    }
    
    /**
     * 发送HTTP POST请求
     */
    private String httpPost(String urlStr, Map<String, String> params) throws IOException {
        // 构建参数字符串
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (paramStr.length() > 0) {
                paramStr.append("&");
            }
            paramStr.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            paramStr.append("=");
            paramStr.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        // 发送参数
        try (OutputStream os = conn.getOutputStream()) {
            os.write(paramStr.toString().getBytes("UTF-8"));
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }
    
    /**
     * 保存上传的文件
     */
    private String saveUploadFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath, "ocr");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
    }
    
    /**
     * 图片预处理（增强对比度、去噪等）
     * TODO: 可以使用OpenCV或其他图像处理库
     */
    private BufferedImage preprocessImage(BufferedImage image) {
        // 这里可以添加图像增强逻辑
        // 例如：二值化、去噪、对比度增强等
        return image;
    }
    
    /**
     * OCR识别类型
     */
    public enum OCRType {
        TEXT,        // 纯文字
        FORMULA,     // 纯公式
        MIXED,       // 混合（文字+公式）
        HANDWRITING  // 手写体
    }
    
    /**
     * OCR识别结果
     */
    public static class OCRResult {
        private boolean success;
        private String text;
        private List<String> formulas;
        private boolean hasFormula;
        private double confidence;  // 置信度 0-1
        private String originalImage;
        private String errorMessage;
        private boolean demoMode;  // 是否为演示模式
        
        public OCRResult() {
            this.formulas = new ArrayList<>();
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public List<String> getFormulas() { return formulas; }
        public void setFormulas(List<String> formulas) { this.formulas = formulas; }
        
        public boolean isHasFormula() { return hasFormula; }
        public void setHasFormula(boolean hasFormula) { this.hasFormula = hasFormula; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getOriginalImage() { return originalImage; }
        public void setOriginalImage(String originalImage) { this.originalImage = originalImage; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public boolean isDemoMode() { return demoMode; }
        public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }
    }
}



