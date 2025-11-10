package com.zszg.ai;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 演示数据服务
 * 当API不可用或数据不足时，提供模拟数据供演示
 */
@Service
public class DemoDataService {
    
    /**
     * 获取演示OCR结果
     */
    public String getDemoOCRText() {
        String[] samples = {
            "已知函数 f(x) = x² - 2x + 3，求该函数的最小值。",
            "解方程：2x + 5 = 13",
            "计算：∫(x² + 2x)dx",
            "证明：在△ABC中，若∠A = 90°，则 a² = b² + c²",
            "求极限：lim(x→0) (sin x) / x"
        };
        return samples[new Random().nextInt(samples.length)];
    }
    
    /**
     * 获取演示公式
     */
    public List<String> getDemoFormulas() {
        return Arrays.asList(
            "f(x) = x² - 2x + 3",
            "x = (b ± √(b² - 4ac)) / 2a",
            "∫ x dx = x²/2 + C"
        );
    }
    
    /**
     * 获取演示思维导图数据
     */
    public String getDemoMindMapData(String subject) {
        return String.format(
            "# %s知识体系\n\n" +
            "## 基础概念\n" +
            "### 定义与性质\n" +
            "描述：基本概念和核心性质\n" +
            "示例：相关例题\n\n" +
            "### 公式推导\n" +
            "描述：重要公式的推导过程\n" +
            "示例：推导实例\n\n" +
            "## 应用方法\n" +
            "### 解题技巧\n" +
            "描述：常用解题方法\n" +
            "示例：典型例题\n\n" +
            "### 易错点\n" +
            "描述：常见错误分析\n" +
            "示例：易错题型\n\n" +
            "关系：\n" +
            "- 基础概念 -> 应用方法 : 依赖\n" +
            "- 公式推导 -> 解题技巧 : 应用\n",
            subject
        );
    }
    
    /**
     * 获取演示试题
     */
    public List<Map<String, Object>> getDemoQuestions(String subject, int count) {
        List<Map<String, Object>> questions = new ArrayList<>();
        
        String[] contents = {
            "下列说法正确的是（　）",
            "计算下列式子的值",
            "已知条件如下，求解",
            "判断下列命题的真假",
            "化简下列表达式"
        };
        
        String[][] options = {
            {"A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"},
            {"A. 结果1", "B. 结果2", "C. 结果3", "D. 结果4"}
        };
        
        String[] answers = {"A", "B", "C", "D"};
        
        for (int i = 0; i < Math.min(count, 10); i++) {
            Map<String, Object> q = new HashMap<>();
            q.put("id", "demo_" + i);
            q.put("content", contents[i % contents.length]);
            q.put("options", options[i % options.length]);
            q.put("answer", answers[i % answers.length]);
            q.put("analysis", "这是一道" + subject + "基础题，主要考查基本概念的理解。");
            q.put("subject", subject);
            q.put("difficulty", i % 3 == 0 ? "简单" : (i % 3 == 1 ? "中等" : "困难"));
            q.put("score", 3);
            questions.add(q);
        }
        
        return questions;
    }
    
    /**
     * 获取演示预测数据
     */
    public Map<String, Object> getDemoPrediction(String subject) {
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("knowledgePoint", subject + "核心概念");
        prediction.put("riskLevel", "MEDIUM");
        prediction.put("probability", 0.65);
        prediction.put("reason", "该知识点近期错题较多，建议加强练习");
        prediction.put("suggestions", Arrays.asList(
            "建议今天开始复习该知识点",
            "完成3-5道相关练习题",
            "预计需要2-3天时间掌握"
        ));
        prediction.put("estimatedTime", "2-3天");
        prediction.put("priority", 2);
        return prediction;
    }
    
    /**
     * 判断是否为演示模式
     */
    public boolean isDemoMode(String apiKey) {
        return apiKey == null || 
               apiKey.isEmpty() || 
               apiKey.equals("YOUR_API_KEY") ||
               apiKey.equals("YOUR_APP_ID");
    }
}

