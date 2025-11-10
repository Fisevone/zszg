# 🎨 AI应用流程图集合

本文档包含项目中AI应用的所有关键流程图，使用Mermaid格式。

---

## 1. AI整体架构图（简洁版）

```mermaid
graph TD
    A[前端用户操作] --> B{什么功能?}
    
    B -->|拍照识别| C1[GLM-4V视觉模型]
    B -->|错题分析| C2[GLM-4语言模型]
    B -->|知识图谱| C2
    B -->|智能问答| C2
    
    C1 --> D1[图片识别结果]
    C2 --> D2[文本分析结果]
    
    D1 --> E[存入数据库]
    D2 --> F[Redis缓存]
    
    F --> G[返回前端显示]
    E --> G
    
    style C1 fill:#FFE5B4
    style C2 fill:#B4D7FF
    style F fill:#FFB4B4
```

---

## 2. 错题AI分析完整流程

```mermaid
sequenceDiagram
    participant 学生 as 学生端
    participant 后端 as Spring Boot
    participant 缓存 as Redis缓存
    participant AI as GLM-4 API
    
    学生->>后端: 点击"AI分析"按钮
    Note over 学生,后端: POST /api/ai/analyze
    
    后端->>后端: 提取题目信息<br/>(题目/答案/难度)
    
    后端->>缓存: 检查是否有缓存
    
    alt 缓存命中
        缓存-->>后端: 返回缓存结果
        Note over 缓存,后端: 响应时间: 50ms
        后端-->>学生: 返回分析结果
    else 缓存未命中
        Note over 后端: 构建Prompt<br/>(100+行结构化指令)
        
        后端->>AI: 调用GLM-4 API
        Note over 后端,AI: POST请求<br/>temperature=0.7
        
        AI->>AI: AI处理中...
        Note over AI: 处理时间: 2-5秒
        
        AI-->>后端: 返回800字分析
        
        后端->>缓存: 存入Redis
        Note over 缓存: 有效期24小时
        
        后端-->>学生: 返回分析结果
    end
    
    学生->>学生: 渲染显示分析内容
```

---

## 3. Prompt工程核心流程

```mermaid
graph LR
    A[原始需求:<br/>分析错题] --> B[Prompt设计]
    
    B --> C1[要素1: 角色设定<br/>资深数学老师]
    B --> C2[要素2: 任务描述<br/>生成详细分析报告]
    B --> C3[要素3: 输出格式<br/>8个章节结构]
    B --> C4[要素4: 示例引导<br/>填空占位符]
    B --> C5[要素5: 约束条件<br/>500字以上]
    B --> C6[要素6: 质量标准<br/>通俗易懂]
    B --> C7[要素7: 上下文<br/>题目+答案信息]
    
    C1 --> D[完整Prompt<br/>100+行]
    C2 --> D
    C3 --> D
    C4 --> D
    C5 --> D
    C6 --> D
    C7 --> D
    
    D --> E[发送给GLM-4]
    E --> F[输出: 800-1000字<br/>结构化分析]
    
    style D fill:#90EE90
    style F fill:#FFD700
```

---

## 4. GLM-4V图片识别流程

```mermaid
graph TD
    A[学生上传题目图片] --> B[前端获取图片File]
    
    B --> C[转换为Base64编码]
    C --> D[发送到后端API]
    
    D --> E[后端构建多模态请求]
    
    E --> F[请求体包含]
    F --> F1[文字Prompt:<br/>识别要求]
    F --> F2[图片数据:<br/>Base64字符串]
    
    F1 --> G[调用GLM-4V API]
    F2 --> G
    
    G --> H[GLM-4V处理]
    
    H --> H1[视觉识别]
    H --> H2[文字提取]
    H --> H3[公式识别LaTeX]
    
    H1 --> I[返回识别结果]
    H2 --> I
    H3 --> I
    
    I --> J[解析JSON响应]
    J --> K[提取content字段]
    K --> L[返回前端]
    
    L --> M[自动填充表单]
    
    style G fill:#FFB6C1
    style M fill:#98FB98
```

---

## 5. 知识图谱生成流程

```mermaid
graph TD
    A[触发: 学生积累多道错题] --> B[提取所有错题的知识点]
    
    B --> C[知识点列表<br/>例: 导数/极限/函数]
    
    C --> D[构建知识图谱Prompt]
    
    D --> E[Prompt要求]
    E --> E1[分析知识点层级关系]
    E --> E2[输出Markdown格式]
    E --> E3[标注依赖/应用关系]
    
    E1 --> F[调用GLM-4]
    E2 --> F
    E3 --> F
    
    F --> G[AI返回结构化文本]
    
    G --> H[MindMapService解析]
    
    H --> I[解析步骤]
    I --> I1[提取节点:<br/># ## ###识别层级]
    I --> I2[提取关系:<br/>-> 箭头解析]
    I --> I3[计算重要度:<br/>层级+子节点数]
    
    I1 --> J[生成节点数组]
    I2 --> K[生成边数组]
    I3 --> L[标注颜色]
    
    J --> M[完整图谱数据]
    K --> M
    L --> M
    
    M --> N[前端ECharts渲染]
    
    N --> O[可视化知识图谱<br/>红色=薄弱 绿色=掌握]
    
    style F fill:#87CEEB
    style O fill:#FFD700
```

---

## 6. AI参数调优决策树

```mermaid
graph TD
    A[选择temperature参数] --> B{任务类型?}
    
    B -->|需要准确性| C[低temperature]
    B -->|需要创造性| D[高temperature]
    B -->|需要平衡| E[中temperature]
    
    C --> C1[公式识别: 0.2<br/>知识点提取: 0.3]
    D --> D1[相似题推荐: 0.8<br/>创意解释: 0.9]
    E --> E1[错题分析: 0.7<br/>智能问答: 0.7]
    
    C1 --> F{输出质量?}
    D1 --> F
    E1 --> F
    
    F -->|太死板| G[增加0.1]
    F -->|太发散| H[减少0.1]
    F -->|刚刚好| I[✅ 确定参数]
    
    G --> J[重新测试]
    H --> J
    J --> F
    
    I --> K[投入生产环境]
    
    style I fill:#90EE90
    style K fill:#FFD700
```

---

## 7. Redis缓存策略流程

```mermaid
graph LR
    A[AI分析请求] --> B[生成缓存Key]
    
    B --> C[MD5题目+答案+难度]
    C --> D[Key: ai:analysis:xxxxx]
    
    D --> E{Redis查询}
    
    E -->|命中| F1[直接返回]
    E -->|未命中| F2[调用AI]
    
    F1 --> G1[响应时间: 50ms]
    F2 --> G2[响应时间: 3000ms]
    
    G2 --> H[存入Redis]
    H --> I[设置TTL=24h]
    
    I --> J[下次查询命中]
    J --> F1
    
    G1 --> K[返回给用户]
    
    style F1 fill:#90EE90
    style F2 fill:#FFB6C1
    style K fill:#FFD700
```

---

## 8. AI错误处理与降级流程

```mermaid
graph TD
    A[调用AI API] --> B{请求是否成功?}
    
    B -->|成功200| C[✅ 解析响应]
    B -->|失败| D{错误类型?}
    
    D -->|401未授权| E1[API密钥错误]
    D -->|429配额| E2[请求过多]
    D -->|500服务器| E3[AI服务故障]
    D -->|超时| E4[网络超时]
    
    E1 --> F1[返回: 系统配置错误]
    
    E2 --> G{重试次数<3?}
    E3 --> G
    E4 --> G
    
    G -->|是| H[等待1秒]
    G -->|否| I[触发降级方案]
    
    H --> J[指数退避<br/>延迟翻倍]
    J --> A
    
    I --> K[FallbackService]
    K --> L[返回简化版分析]
    
    C --> M[正常返回]
    F1 --> N[友好提示]
    L --> N
    
    M --> O[用户看到结果]
    N --> O
    
    style C fill:#90EE90
    style I fill:#FFD700
    style L fill:#FFA500
```

---

## 9. 双模型协同工作流程

```mermaid
graph TD
    A[学生上传手写错题照片] --> B[GLM-4V处理]
    
    B --> C[识别题目内容]
    C --> D[识别学生答案]
    D --> E[识别数学公式LaTeX]
    
    E --> F[组装数据]
    F --> G{识别成功?}
    
    G -->|是| H[数据传递给GLM-4]
    G -->|否| Z[提示重新拍照]
    
    H --> I[GLM-4分析]
    
    I --> J[分析题目知识点]
    J --> K[对比正确答案]
    K --> L[生成错因分析]
    L --> M[生成详细步骤]
    M --> N[生成学习建议]
    
    N --> O[完整分析报告]
    
    O --> P[存入数据库]
    P --> Q[返回前端]
    
    Q --> R[Markdown渲染]
    R --> S[KaTeX公式渲染]
    
    S --> T[用户看到美观结果]
    
    style B fill:#FFE5B4
    style I fill:#B4D7FF
    style T fill:#90EE90
```

---

## 10. AI成本优化流程

```mermaid
graph LR
    A[原始方案:<br/>每次都调用AI] --> B[成本分析]
    
    B --> C[每次0.02元<br/>月10万次=2000元]
    
    C --> D[💡 优化思路]
    
    D --> E1[策略1: Redis缓存]
    D --> E2[策略2: 批量处理]
    D --> E3[策略3: 降级方案]
    
    E1 --> F1[命中率95%<br/>节省1900元/月]
    E2 --> F2[减少重复调用<br/>节省200元/月]
    E3 --> F3[故障时不调用<br/>避免浪费]
    
    F1 --> G[优化后成本]
    F2 --> G
    F3 --> G
    
    G --> H[100元/月<br/>节省95%]
    
    H --> I[✅ 投入使用]
    
    style C fill:#FFB6C1
    style H fill:#90EE90
    style I fill:#FFD700
```

---

## 11. 学生使用AI功能的完整旅程

```mermaid
journey
    title 学生使用AI分析错题的体验旅程
    section 发现问题
      做错一道题: 3: 学生
      感到困惑: 2: 学生
      想要理解: 4: 学生
    section 使用AI
      点击AI分析: 5: 学生
      等待2-3秒: 4: 学生
      看到详细分析: 5: 学生
    section 理解巩固
      阅读错因分析: 5: 学生
      学习正确步骤: 5: 学生
      理解知识点: 5: 学生
    section 效果反馈
      恍然大悟: 5: 学生
      掌握方法: 5: 学生
      下次不错: 5: 学生
```

---

## 12. Prompt迭代优化历程

```mermaid
graph TD
    V1[V1.0版本<br/>简单Prompt<br/>输出50字] --> T1[测试]
    
    T1 --> P1{问题}
    P1 --> P1A[内容太简短]
    P1 --> P1B[格式混乱]
    
    P1A --> V2[V2.0版本<br/>加上输出格式<br/>输出150字]
    P1B --> V2
    
    V2 --> T2[测试]
    T2 --> P2{问题}
    P2 --> P2A[还是不够详细]
    
    P2A --> V3[V3.0版本<br/>加上结构化标记<br/>输出300字]
    
    V3 --> T3[测试]
    T3 --> P3{问题}
    P3 --> P3A[内容太简短]
    
    P3A --> V4[V4.0版本<br/>加上示例和约束<br/>输出600字]
    
    V4 --> T4[测试]
    T4 --> P4{问题}
    P4 --> P4A[还可以更好]
    
    P4A --> V5[V5.0版本<br/>完整7要素<br/>输出900字]
    
    V5 --> T5[测试]
    T5 --> OK{质量评估}
    
    OK --> YES[✅ 满意度95%<br/>投入使用]
    
    style V1 fill:#FFB6C1
    style V5 fill:#90EE90
    style YES fill:#FFD700
```

---

## 使用说明

### 如何在Markdown中嵌入这些图表？

1. **复制对应的mermaid代码块**
2. **粘贴到支持Mermaid的平台**：
   - GitHub README
   - Typora编辑器
   - VS Code + Markdown Preview Enhanced
   - 在线工具: https://mermaid.live/

### 如何在PPT中使用？

1. 访问 https://mermaid.live/
2. 粘贴mermaid代码
3. 点击"PNG"或"SVG"导出图片
4. 插入到PPT中

### 颜色说明

- 🟢 **绿色** - 成功/最终结果/推荐方案
- 🔵 **蓝色** - GLM-4语言模型相关
- 🟡 **黄色** - 重要节点/用户看到的结果
- 🔴 **红色** - 缓存/存储相关
- 🟠 **橙色** - GLM-4V视觉模型相关
- 🟣 **紫色** - 备选方案

---

## 答辩时如何使用这些图？

### 图1-2：整体介绍
> "这是我们AI的整体架构和主流程，采用双模型设计..."

### 图3：技术深度
> "这张图展示了我们Prompt Engineering的核心技术..."

### 图5：创新点
> "知识图谱生成是一个复杂的流程，涉及AI分析、数据解析、可视化三个环节..."

### 图7：性能优化
> "通过Redis缓存，我们将响应时间从3秒降到50ms..."

### 图10：成本控制
> "通过这些优化策略，我们节省了95%的AI API成本..."


