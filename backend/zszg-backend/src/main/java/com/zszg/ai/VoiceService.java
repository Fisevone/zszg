package com.zszg.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 语音服务 - 语音识别(ASR)和语音合成(TTS)
 * 
 * 功能：
 * 1. 语音识别：将语音转换为文字
 * 2. 语音合成：将文字转换为语音
 * 3. 多轮对话管理
 * 4. 支持多种音色和语速
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceService {
    
    @Value("${app.baidu.voice.app-id:YOUR_APP_ID}")
    private String appId;
    
    @Value("${app.baidu.voice.api-key:YOUR_API_KEY}")
    private String apiKey;
    
    @Value("${app.baidu.voice.secret-key:YOUR_SECRET_KEY}")
    private String secretKey;
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    private final GLMService glmService;
    private final DemoDataService demoDataService;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String accessToken = null;
    private long tokenExpireTime = 0;
    
    /**
     * 检查是否为演示模式
     */
    private boolean isDemoMode() {
        return demoDataService.isDemoMode(apiKey);
    }
    
    /**
     * 语音对话 - 完整流程
     * 1. 语音识别（ASR）
     * 2. AI回答（GLM）
     * 3. 语音合成（TTS）
     */
    public VoiceDialogResult voiceDialog(byte[] audioData, String userId, String sessionId, String subject) {
        VoiceDialogResult result = new VoiceDialogResult();
        result.setSessionId(sessionId);
        result.setDemoMode(isDemoMode());
        
        try {
            String recognizedText;
            
            // 1. 语音识别（演示模式下跳过）
            if (isDemoMode()) {
                log.info("演示模式：跳过语音识别，使用示例文本");
                recognizedText = "这是一道数学题，能帮我解答吗？";
                result.setRecognizedText(recognizedText);
            } else {
                log.info("开始语音识别, userId={}, sessionId={}", userId, sessionId);
                try {
                    recognizedText = speechToText(audioData);
                    result.setRecognizedText(recognizedText);
                    log.info("语音识别完成: {}", recognizedText);
                } catch (Exception e) {
                    log.warn("语音识别失败，降级为演示模式", e);
                    recognizedText = "这是一道数学题，能帮我解答吗？";
                    result.setRecognizedText(recognizedText);
                    result.setDemoMode(true);
                }
            }
            
            // 2. 获取对话历史
            String context = getDialogContext(sessionId);
            
            // 3. AI回答（这个一定可用）
            log.info("AI开始回答问题");
            String aiResponse = glmService.answerQuestion(subject, recognizedText, context);
            result.setAiResponse(aiResponse);
            log.info("AI回答完成，长度={}", aiResponse.length());
            
            // 4. 保存对话历史
            saveDialogContext(sessionId, recognizedText, aiResponse);
            
            // 5. 语音合成（演示模式下跳过）
            if (isDemoMode()) {
                log.info("演示模式：跳过语音合成");
                result.setAudioUrl(null);
            } else {
                try {
                    log.info("开始语音合成");
                    String audioUrl = textToSpeech(aiResponse, sessionId);
                    result.setAudioUrl(audioUrl);
                    log.info("语音合成完成: {}", audioUrl);
                } catch (Exception e) {
                    log.warn("语音合成失败，将仅返回文字", e);
                    result.setAudioUrl(null);
                }
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            log.error("语音对话失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 纯文字对话（当语音不可用时）
     */
    public VoiceDialogResult textDialog(String question, String userId, String sessionId, String subject) {
        VoiceDialogResult result = new VoiceDialogResult();
        result.setSessionId(sessionId);
        result.setRecognizedText(question);
        result.setDemoMode(true);
        
        try {
            // 获取对话历史
            String context = getDialogContext(sessionId);
            
            // AI回答
            String aiResponse = glmService.answerQuestion(subject, question, context);
            result.setAiResponse(aiResponse);
            
            // 保存对话历史
            saveDialogContext(sessionId, question, aiResponse);
            
            result.setSuccess(true);
            result.setAudioUrl(null);  // 文字模式不生成语音
            
        } catch (Exception e) {
            log.error("文字对话失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 语音识别 - 将音频转换为文字
     */
    public String speechToText(byte[] audioData) throws IOException {
        String token = getAccessToken();
        
        // 百度语音识别API
        String url = "https://vop.baidu.com/server_api";
        
        // 将音频转换为Base64
        String speech = Base64.getEncoder().encodeToString(audioData);
        
        // 构建请求JSON
        Map<String, Object> params = new HashMap<>();
        params.put("format", "wav");  // 音频格式
        params.put("rate", 16000);    // 采样率
        params.put("channel", 1);     // 声道数
        params.put("cuid", "ZSZG_APP");
        params.put("token", token);
        params.put("speech", speech);
        params.put("len", audioData.length);
        
        String jsonParams = objectMapper.writeValueAsString(params);
        
        // 发送POST请求
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonParams.getBytes(StandardCharsets.UTF_8));
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        // 解析结果
        JsonNode root = objectMapper.readTree(response.toString());
        
        if (root.path("err_no").asInt() != 0) {
            throw new RuntimeException("语音识别失败: " + root.path("err_msg").asText());
        }
        
        // 返回识别的文字
        return root.path("result").get(0).asText();
    }
    
    /**
     * 语音合成 - 将文字转换为语音
     */
    public String textToSpeech(String text, String sessionId) throws IOException {
        String token = getAccessToken();
        
        // 百度语音合成API
        String url = "https://tsn.baidu.com/text2audio";
        
        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("tex", text);
        params.put("tok", token);
        params.put("cuid", "ZSZG_APP");
        params.put("ctp", "1");
        params.put("lan", "zh");
        params.put("spd", "5");   // 语速(0-15)
        params.put("pit", "5");   // 音调(0-15)
        params.put("vol", "5");   // 音量(0-15)
        params.put("per", "4");   // 音色(0:女声, 1:男声, 3:情感男声, 4:情感女声)
        params.put("aue", "3");   // 音频格式(3:mp3, 4:pcm-16k, 5:pcm-8k, 6:wav)
        
        // 构建URL
        StringBuilder urlBuilder = new StringBuilder(url + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                    .append("&");
        }
        
        // 发送请求
        HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        conn.setRequestMethod("GET");
        
        // 检查响应类型
        String contentType = conn.getContentType();
        if (contentType.contains("audio")) {
            // 成功，保存音频文件
            String audioPath = saveAudioFile(conn.getInputStream(), sessionId);
            return audioPath;
        } else {
            // 失败，读取错误信息
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            throw new RuntimeException("语音合成失败: " + response.toString());
        }
    }
    
    /**
     * 保存音频文件
     */
    private String saveAudioFile(InputStream inputStream, String sessionId) throws IOException {
        // 创建音频目录
        Path audioDir = Paths.get(uploadPath, "voice");
        if (!Files.exists(audioDir)) {
            Files.createDirectories(audioDir);
        }
        
        // 生成文件名
        String filename = sessionId + "_" + System.currentTimeMillis() + ".mp3";
        Path audioPath = audioDir.resolve(filename);
        
        // 保存文件
        try (FileOutputStream fos = new FileOutputStream(audioPath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        // 返回相对路径
        return "/uploads/voice/" + filename;
    }
    
    /**
     * 获取对话历史上下文
     */
    private String getDialogContext(String sessionId) {
        if (redisTemplate == null) {
            log.warn("Redis不可用，无法获取对话历史");
            return "";
        }
        
        String key = "voice:dialog:" + sessionId;
        String context = redisTemplate.opsForValue().get(key);
        return context != null ? context : "";
    }
    
    /**
     * 保存对话历史
     */
    private void saveDialogContext(String sessionId, String userQuestion, String aiResponse) {
        if (redisTemplate == null) {
            log.warn("Redis不可用，无法保存对话历史");
            return;
        }
        
        String key = "voice:dialog:" + sessionId;
        String context = String.format("用户：%s\nAI：%s\n\n", userQuestion, aiResponse);
        
        // 追加到现有上下文
        String existingContext = redisTemplate.opsForValue().get(key);
        if (existingContext != null) {
            context = existingContext + context;
            
            // 限制上下文长度（只保留最近5轮对话）
            String[] dialogs = context.split("\n\n");
            if (dialogs.length > 5) {
                context = String.join("\n\n", 
                        Arrays.copyOfRange(dialogs, dialogs.length - 5, dialogs.length));
            }
        }
        
        // 保存到Redis，30分钟过期
        redisTemplate.opsForValue().set(key, context, 30, TimeUnit.MINUTES);
    }
    
    /**
     * 获取百度语音的Access Token
     */
    private String getAccessToken() throws IOException {
        // 如果token还在有效期内，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        log.info("正在获取百度语音 Access Token");
        
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", apiKey);
        params.put("client_secret", secretKey);
        
        // 构建URL
        StringBuilder urlBuilder = new StringBuilder(url + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        conn.setRequestMethod("GET");
        
        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        
        JsonNode root = objectMapper.readTree(response.toString());
        
        if (root.has("error")) {
            throw new RuntimeException("获取Access Token失败: " + root.get("error_description").asText());
        }
        
        accessToken = root.path("access_token").asText();
        int expiresIn = root.path("expires_in").asInt();
        tokenExpireTime = System.currentTimeMillis() + (expiresIn - 600) * 1000;
        
        log.info("Access Token获取成功，有效期{}秒", expiresIn);
        
        return accessToken;
    }
    
    /**
     * 语音对话结果
     */
    @Data
    public static class VoiceDialogResult {
        private boolean success;
        private String sessionId;
        private String recognizedText;  // 识别的文字
        private String aiResponse;      // AI回答
        private String audioUrl;        // 合成的语音URL
        private double audioDuration;   // 音频时长（秒）
        private String errorMessage;
        private boolean demoMode;       // 是否为演示模式
    }
}



