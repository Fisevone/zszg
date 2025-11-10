package com.zszg.ai;

import com.zszg.common.ApiResponse;
import com.zszg.errorbook.ErrorBook;
import com.zszg.errorbook.ErrorBookRepository;
import com.zszg.question.Question;
import com.zszg.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI助手控制器
 * 提供智能分析、答疑、推荐等功能
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAssistantController {
    
    private final GLMService glmService;
    private final ErrorBookRepository errorBookRepository;
    private final OCRService ocrService;
    private final VoiceService voiceService;
    private final MindMapService mindMapService;
    private final PredictionService predictionService;
    private final ExamGeneratorService examGeneratorService;
    private final PhotoSearchService photoSearchService;
    private final GLMVisionService glmVisionService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 智能分析错题
     */
    @PostMapping("/analyze-error")
    public ApiResponse<String> analyzeError(@RequestBody AnalyzeErrorRequest request) {
        String analysis = glmService.analyzeErrorQuestion(
            request.getSubject(),
            request.getQuestionContent(),
            request.getCorrectAnswer(),
            request.getUserAnswer(),
            request.getDifficulty()
        );
        return ApiResponse.ok(analysis);
    }
    
    /**
     * 提取知识点
     */
    @PostMapping("/extract-knowledge")
    public ApiResponse<List<String>> extractKnowledge(@RequestBody ExtractKnowledgeRequest request) {
        List<String> knowledgePoints = glmService.extractKnowledgePoints(
            request.getSubject(),
            request.getQuestionContent()
        );
        return ApiResponse.ok(knowledgePoints);
    }
    
    /**
     * 生成学习路径
     */
    @PostMapping("/learning-path")
    public ApiResponse<String> generateLearningPath(
            @AuthenticationPrincipal User user,
            @RequestBody LearningPathRequest request) {
        
        String learningPath = glmService.generateLearningPath(
            request.getSubject(),
            request.getWeakKnowledgePoints()
        );
        return ApiResponse.ok(learningPath);
    }
    
    /**
     * 推荐相似题目
     */
    @PostMapping("/recommend-questions")
    public ApiResponse<String> recommendQuestions(@RequestBody RecommendQuestionsRequest request) {
        String recommendations = glmService.recommendSimilarQuestions(
            request.getSubject(),
            request.getQuestionContent(),
            request.getKnowledgePoints(),
            request.getDifficulty()
        );
        return ApiResponse.ok(recommendations);
    }
    
    /**
     * 智能答疑
     */
    @PostMapping("/ask")
    public ApiResponse<String> askQuestion(@RequestBody AskQuestionRequest request) {
        String answer = glmService.answerQuestion(
            request.getSubject(),
            request.getQuestion(),
            request.getContext()
        );
        return ApiResponse.ok(answer);
    }
    
    /**
     * 生成题目解析
     */
    @PostMapping("/generate-analysis")
    public ApiResponse<String> generateAnalysis(@RequestBody GenerateAnalysisRequest request) {
        String analysis = glmService.generateQuestionAnalysis(
            request.getSubject(),
            request.getQuestionContent(),
            request.getAnswer()
        );
        return ApiResponse.ok(analysis);
    }
    
    /**
     * 生成个性化学习报告
     */
    @GetMapping("/student-report")
    public ApiResponse<String> generateStudentReport(
            @AuthenticationPrincipal User user,
            @RequestParam String subject) {
        
        // 获取学生学习数据
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        List<ErrorBook> subjectErrors = errorBooks.stream()
            .filter(eb -> subject.equals(eb.getQuestion().getSubject()))
            .collect(Collectors.toList());
        
        long correctedCount = subjectErrors.stream()
            .filter(eb -> eb.getCorrection() != null && !eb.getCorrection().isEmpty())
            .count();
        
        // 统计薄弱知识点（这里简化处理，实际可以更复杂）
        Map<String, Long> knowledgeStats = subjectErrors.stream()
            .collect(Collectors.groupingBy(
                eb -> eb.getQuestion().getSubject(), 
                Collectors.counting()
            ));
        
        Map<String, Object> learningData = new HashMap<>();
        learningData.put("totalErrors", subjectErrors.size());
        learningData.put("correctedErrors", correctedCount);
        learningData.put("weakPoints", String.join("、", knowledgeStats.keySet()));
        learningData.put("recentTrend", "稳步提升");
        
        String report = glmService.generateStudentReport(
            user.getRealName() != null ? user.getRealName() : user.getUsername(),
            subject,
            learningData
        );
        
        return ApiResponse.ok(report);
    }
    
    /**
     * 班级数据分析（教师功能）
     */
    @GetMapping("/class-analysis")
    public ApiResponse<String> analyzeClassData(
            @RequestParam String subject,
            @RequestParam(required = false) String classId) {
        
        // 获取班级错题数据
        List<ErrorBook> allErrors = errorBookRepository.findAll();
        List<ErrorBook> classErrors = allErrors.stream()
            .filter(eb -> subject.equals(eb.getQuestion().getSubject()))
            .collect(Collectors.toList());
        
        // 统计知识点错误次数
        Map<String, Integer> errorStats = new HashMap<>();
        classErrors.forEach(eb -> {
            String subj = eb.getQuestion().getSubject();
            errorStats.put(subj, errorStats.getOrDefault(subj, 0) + 1);
        });
        
        // 获取高频错题
        List<String> topErrorQuestions = classErrors.stream()
            .limit(5)
            .map(eb -> eb.getQuestion().getContent())
            .collect(Collectors.toList());
        
        String analysis = glmService.analyzeClassData(subject, errorStats, topErrorQuestions);
        return ApiResponse.ok(analysis);
    }
    
    /**
     * 批量生成练习题（教师功能）
     */
    @PostMapping("/generate-practice")
    public ApiResponse<String> generatePractice(@RequestBody GeneratePracticeRequest request) {
        String questions = glmService.generatePracticeQuestions(
            request.getSubject(),
            request.getKnowledgePoint(),
            request.getDifficulty(),
            request.getCount()
        );
        return ApiResponse.ok(questions);
    }
    
    // ============ 新增功能 ============
    
    /**
     * AI拍照识题 - OCR识别（旧版，仅识别）
     */
    @PostMapping("/ocr/recognize")
    public ApiResponse<OCRService.OCRResult> recognizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "MIXED") String typeStr) throws IOException {
        
        OCRService.OCRType type = OCRService.OCRType.valueOf(typeStr.toUpperCase());
        OCRService.OCRResult result = ocrService.recognizeImage(file, type);
        return ApiResponse.ok(result);
    }
    
    /**
     * 🔥 拍照搜题 - 完整的AI搜题功能（新版，推荐使用）
     * 
     * 功能：
     * 1. OCR识别图片
     * 2. AI分析题目
     * 3. 生成详细解答
     * 4. 提取知识点
     * 5. 学习建议
     */
    @PostMapping("/photo-search")
    public ApiResponse<PhotoSearchService.SearchResult> photoSearch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subject", defaultValue = "数学") String subject) throws IOException {
        
        PhotoSearchService.SearchResult result = photoSearchService.searchQuestion(file, subject);
        return ApiResponse.ok(result);
    }
    
    /**
     * 🔥 文字搜题 - 直接输入题目文字搜索
     */
    @PostMapping("/text-search")
    public ApiResponse<String> textSearch(@RequestBody TextSearchRequest request) {
        String answer = photoSearchService.quickSearch(request.getQuestion(), request.getSubject());
        return ApiResponse.ok(answer);
    }
    
    /**
     * 🆓 免费拍照搜题 - 使用GLM-4V视觉模型（完全免费）
     * 
     * 优势：
     * 1. 完全免费 - 使用现有的GLM-4 API密钥
     * 2. 无需配置 - 直接可用
     * 3. 一步到位 - 识别+解答同时完成
     */
    @PostMapping("/free-photo-search")
    public ApiResponse<GLMVisionService.VisionResult> freePhotoSearch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subject", defaultValue = "数学") String subject) throws IOException {
        
        GLMVisionService.VisionResult result = glmVisionService.recognizeAndSolve(file, subject);
        return ApiResponse.ok(result);
    }
    
    /**
     * AI语音对话
     */
    @PostMapping("/voice/chat")
    public ApiResponse<VoiceService.VoiceDialogResult> voiceChat(
            @AuthenticationPrincipal User user,
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "subject", defaultValue = "通用") String subject) throws IOException {
        
        byte[] audioData = audioFile.getBytes();
        VoiceService.VoiceDialogResult result = voiceService.voiceDialog(
            audioData, user.getId().toString(), sessionId, subject);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 文字对话（语音助教的降级版本）
     */
    @PostMapping("/voice/text-chat")
    public ApiResponse<VoiceService.VoiceDialogResult> textChat(
            @AuthenticationPrincipal User user,
            @RequestBody TextChatRequest request) {
        
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "voice_" + user.getId() + "_" + System.currentTimeMillis();
        }
        
        VoiceService.VoiceDialogResult result = voiceService.textDialog(
            request.getQuestion(), 
            user.getId().toString(), 
            sessionId, 
            request.getSubject() != null ? request.getSubject() : "通用");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 生成思维导图
     */
    @PostMapping("/mindmap/generate")
    public ApiResponse<MindMapService.MindMapData> generateMindMap(
            @RequestBody GenerateMindMapRequest request) {
        
        MindMapService.MindMapType type = MindMapService.MindMapType.valueOf(
            request.getType().toUpperCase());
        
        MindMapService.MindMapData mindMap = mindMapService.generateMindMap(
            request.getContent(), request.getSubject(), type);
        
        return ApiResponse.ok(mindMap);
    }
    
    /**
     * AI预测薄弱点
     */
    @GetMapping("/predict/weakness")
    public ApiResponse<PredictionService.PredictionReport> predictWeakness(
            @AuthenticationPrincipal User user,
            @RequestParam String subject) {
        
        PredictionService.PredictionReport report = predictionService.predictWeakness(user, subject);
        return ApiResponse.ok(report);
    }
    
    /**
     * AI生成试卷
     */
    @PostMapping("/exam/generate")
    public ApiResponse<ExamGeneratorService.ExamPaper> generateExam(
            @RequestBody ExamGeneratorService.ExamConfig config) {
        
        ExamGeneratorService.ExamPaper paper = examGeneratorService.generateExam(config);
        return ApiResponse.ok(paper);
    }
    
    /**
     * AI推荐共享错题 - 基于学生的学习情况智能推荐
     */
    @GetMapping("/recommend-shares")
    public ApiResponse<List<com.zszg.sharepool.SharePool>> recommendShares(
            @AuthenticationPrincipal User user) {
        
        try {
            // 获取学生的错题数据，分析薄弱点
            List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
            
            // 如果学生没有错题，返回热门共享错题
            if (errorBooks.isEmpty()) {
                com.zszg.sharepool.SharePoolRepository sharePoolRepo = 
                    applicationContext.getBean(com.zszg.sharepool.SharePoolRepository.class);
                List<com.zszg.sharepool.SharePool> popularShares = 
                    sharePoolRepo.findByApprovedTrueOrderByCreatedAtDesc();
                return ApiResponse.ok(popularShares.stream().limit(10).collect(Collectors.toList()));
            }
            
            // 统计学科分布
            Map<String, Long> subjectStats = errorBooks.stream()
                .collect(Collectors.groupingBy(
                    eb -> eb.getQuestion().getSubject(),
                    Collectors.counting()
                ));
            
            // 找出错题最多的学科（薄弱学科）
            String weakSubject = subjectStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("数学");
            
            // 根据薄弱学科推荐共享错题
            com.zszg.sharepool.SharePoolRepository sharePoolRepo = 
                applicationContext.getBean(com.zszg.sharepool.SharePoolRepository.class);
            List<com.zszg.sharepool.SharePool> recommendedShares = 
                sharePoolRepo.findBySubject(weakSubject);
            
            // 返回前20个推荐
            return ApiResponse.ok(recommendedShares.stream().limit(20).collect(Collectors.toList()));
        } catch (Exception e) {
            // 如果出错，返回空列表而不是报错
            return ApiResponse.ok(new ArrayList<>());
        }
    }
    
    /**
     * 生成个性化学习报告 - 完整版
     */
    @GetMapping("/learning-report")
    public ApiResponse<Map<String, Object>> generateLearningReport(
            @AuthenticationPrincipal User user) {
        
        Map<String, Object> report = new HashMap<>();
        
        // 获取学生的错题数据
        List<ErrorBook> errorBooks = errorBookRepository.findByUserOrderByCreatedAtDesc(user);
        
        // 基础统计
        long totalErrors = errorBooks.size();
        long correctedCount = errorBooks.stream()
            .filter(eb -> eb.getCorrection() != null && !eb.getCorrection().isEmpty())
            .count();
        long sharedCount = errorBooks.stream()
            .filter(eb -> "SHARED".equals(eb.getStatus()))
            .count();
        
        // 学科分布
        Map<String, Long> subjectStats = errorBooks.stream()
            .collect(Collectors.groupingBy(
                eb -> eb.getQuestion().getSubject(),
                Collectors.counting()
            ));
        
        // 难度分布
        Map<String, Long> difficultyStats = errorBooks.stream()
            .filter(eb -> eb.getQuestion().getDifficulty() != null)
            .collect(Collectors.groupingBy(
                eb -> eb.getQuestion().getDifficulty(),
                Collectors.counting()
            ));
        
        report.put("totalErrors", totalErrors);
        report.put("correctedCount", correctedCount);
        report.put("sharedCount", sharedCount);
        report.put("correctionRate", totalErrors > 0 ? (correctedCount * 100 / totalErrors) : 0);
        report.put("subjectStats", subjectStats);
        report.put("difficultyStats", difficultyStats);
        
        // 生成AI分析文本
        String aiAnalysis = glmService.generateLearningReport(
            user.getRealName() != null ? user.getRealName() : user.getUsername(),
            subjectStats,
            difficultyStats,
            totalErrors,
            correctedCount
        );
        report.put("aiAnalysis", aiAnalysis);
        
        return ApiResponse.ok(report);
    }
    
    // ============ DTO类 ============
    
    @Data
    public static class AnalyzeErrorRequest {
        private String subject;
        private String questionContent;
        private String correctAnswer;
        private String userAnswer;
        private String difficulty;
    }
    
    @Data
    public static class ExtractKnowledgeRequest {
        private String subject;
        private String questionContent;
    }
    
    @Data
    public static class LearningPathRequest {
        private String subject;
        private List<String> weakKnowledgePoints;
    }
    
    @Data
    public static class RecommendQuestionsRequest {
        private String subject;
        private String questionContent;
        private String knowledgePoints;
        private String difficulty;
    }
    
    @Data
    public static class AskQuestionRequest {
        private String subject;
        private String question;
        private String context;
    }
    
    @Data
    public static class GenerateAnalysisRequest {
        private String subject;
        private String questionContent;
        private String answer;
    }
    
    @Data
    public static class GeneratePracticeRequest {
        private String subject;
        private String knowledgePoint;
        private String difficulty;
        private int count;
    }
    
    @Data
    public static class GenerateMindMapRequest {
        private String content;
        private String subject;
        private String type;  // chapter/topic/errorbook
    }
    
    @Data
    public static class TextChatRequest {
        private String question;
        private String sessionId;
        private String subject;
    }
    
    @Data
    public static class TextSearchRequest {
        private String question;
        private String subject;
    }
}


