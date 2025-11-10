package com.zszg.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * GLM-4V 视觉模型服务 - 免费的图片识别方案
 * 
 * 优势：
 * 1. 完全免费 - 使用现有的GLM-4 API密钥
 * 2. 无需配置 - 直接可用
 * 3. 识别更准 - AI直接理解图片内容
 * 4. 一步到位 - 识别+理解同时完成
 */
@Slf4j
@Service
public class GLMVisionService {
    
    @Value("${app.glm.api-key:3f3508c9bcbe476db696356fb8ac6345.n2bT5A7uqHiNDd3l}")
    private String apiKey;
    
    @Value("${app.glm.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 使用GLM-4V识别图片中的题目
     * 
     * 🆓 完全免费 - 使用现有的GLM-4 API密钥
     */
    public VisionResult recognizeQuestion(MultipartFile file) throws IOException {
        log.warn("=".repeat(60));
        log.warn("🆓 开始使用GLM-4V视觉模型（完全免费）");
        log.warn("文件名: {}", file.getOriginalFilename());
        log.warn("文件大小: {} KB", file.getSize() / 1024);
        log.warn("=".repeat(60));
        
        VisionResult result = new VisionResult();
        
        try {
            // 保存并读取图片
            String savedPath = saveUploadFile(file);
            log.info("📁 图片已保存: {}", savedPath);
            
            byte[] imageBytes = Files.readAllBytes(Paths.get(savedPath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            log.info("🔄 图片已转换为Base64，长度: {} 字符", base64Image.length());
            
            result.setImagePath(savedPath);
            
            // 使用GLM-4V识别图片
            log.warn("📡 正在调用GLM-4V API...");
            String recognizedText = recognizeImageWithGLM4V(base64Image);
            
            result.setSuccess(true);
            result.setQuestionText(recognizedText);
            result.setMethod("GLM-4V视觉模型（免费）");
            
            log.warn("=".repeat(60));
            log.warn("✅ GLM-4V识别成功！");
            log.warn("识别内容预览: {}", 
                recognizedText.substring(0, Math.min(100, recognizedText.length())) + "...");
            log.warn("总长度: {} 字符", recognizedText.length());
            log.warn("=".repeat(60));
            
        } catch (Exception e) {
            log.error("=".repeat(60));
            log.error("❌ GLM-4V识别失败", e);
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            log.error("=".repeat(60));
            
            result.setSuccess(false);
            result.setErrorMessage("GLM-4V识别失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 使用GLM-4V识别图片并直接解答
     * 
     * 🚀 一步到位 - 识别+解答同时完成
     */
    public VisionResult recognizeAndSolve(MultipartFile file, String subject) throws IOException {
        log.info("🚀 使用GLM-4V识别并解答图片: {}", file.getOriginalFilename());
        
        VisionResult result = new VisionResult();
        
        // 保存并读取图片
        String savedPath = saveUploadFile(file);
        byte[] imageBytes = Files.readAllBytes(Paths.get(savedPath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        result.setImagePath(savedPath);
        
        try {
            // 使用GLM-4V识别并解答
            String prompt = String.format(
                "请识别这张图片中的%s题目，并提供详细解答。\n\n" +
                "请按以下格式回答：\n\n" +
                "【题目内容】\n" +
                "（完整题目文字）\n\n" +
                "【详细解答】\n" +
                "（解题步骤和答案）",
                subject
            );
            
            String response = callGLM4V(base64Image, prompt);
            
            // 解析响应，提取题目和解答
            String[] parts = response.split("【详细解答】");
            String questionText = "";
            String answer = "";
            
            if (parts.length >= 1) {
                questionText = parts[0].replace("【题目内容】", "").trim();
            }
            if (parts.length >= 2) {
                answer = parts[1].trim();
            }
            
            result.setSuccess(true);
            result.setQuestionText(questionText);
            result.setAnswer(answer);
            result.setMethod("GLM-4V视觉模型（免费）");
            
            log.info("✅ GLM-4V识别并解答成功");
            
        } catch (Exception e) {
            log.error("GLM-4V识别失败", e);
            result.setSuccess(false);
            result.setErrorMessage("图片识别失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 使用GLM-4V识别图片内容
     */
    private String recognizeImageWithGLM4V(String base64Image) throws IOException {
        String prompt = "请识别这张图片中的题目内容，要求：\n\n" +
            "1. **识别所有文字和公式**（包括数学公式、分数、特殊符号等）\n" +
            "   - ⚠️ **分数必须使用简单格式**：例如写成 2/5，不要使用 \\frac{2}{5} 这种复杂格式\n" +
            "   - 例如：三分之二 → 2/3，五分之一 → 1/5\n" +
            "2. **详细描述图表内容**：\n" +
            "   - 如果有线段图、条形图、扇形图等，请详细描述其含义\n" +
            "   - 说明图中各部分的数量关系\n" +
            "   - 说明哪个多、哪个少、相差多少等\n" +
            "3. **描述几何图形**：\n" +
            "   - 如果有三角形、圆形、长方形等，请描述其特征\n" +
            "   - 标注的角度、长度、面积等数据\n" +
            "4. **保持原题格式**，包括题号、选项等\n\n" +
            "请将图表、图形的视觉信息转换为文字描述，插入到题目中对应位置。\n" +
            "只返回题目内容，不要添加任何解答或分析。";
        
        String result = callGLM4V(base64Image, prompt);
        
        // 后处理：将LaTeX分数格式转换为简单格式
        result = convertLatexFractionsToSimple(result);
        
        return result;
    }
    
    /**
     * 将LaTeX分数格式转换为简单格式
     * 例如：\frac{2}{5} → 2/5
     */
    private String convertLatexFractionsToSimple(String text) {
        if (text == null) {
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
        
        log.info("分数格式转换完成");
        return sb.toString();
    }
    
    /**
     * 调用GLM-4V API
     */
    private String callGLM4V(String base64Image, String textPrompt) throws IOException {
        log.warn("=".repeat(60));
        log.warn("📡 准备调用GLM-4V API");
        log.warn("API地址: {}", apiUrl);
        log.warn("模型: glm-4v");
        log.warn("提示词: {}", textPrompt);
        log.warn("=".repeat(60));
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4v");  // 使用视觉模型
        
        // 构建消息
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        
        // 构建内容（文本+图片）
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 文本提示
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", textPrompt);
        content.add(textContent);
        
        // 图片内容
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrl);
        content.add(imageContent);
        
        message.put("content", content);
        messages.add(message);
        
        requestBody.put("messages", messages);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        // 解析响应
        JsonNode root = objectMapper.readTree(response.getBody());
        
        if (root.has("error")) {
            throw new IOException("GLM-4V API错误: " + root.get("error").get("message").asText());
        }
        
        String result = root.path("choices")
            .get(0)
            .path("message")
            .path("content")
            .asText();
        
        log.info("GLM-4V API调用成功");
        
        return result;
    }
    
    /**
     * 保存上传的文件
     */
    private String saveUploadFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath, "vision");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
    }
    
    /**
     * 视觉识别结果
     */
    @Data
    public static class VisionResult {
        private boolean success;
        private String imagePath;
        private String questionText;     // 识别的题目
        private String answer;           // AI解答（如果请求了）
        private String method;           // 识别方法
        private String errorMessage;
    }
}

