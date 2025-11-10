package com.zszg.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 思维导图服务 - AI生成知识点思维导图
 * 
 * 功能：
 * 1. 从文本/章节提取知识点
 * 2. 分析知识点层级关系
 * 3. 生成思维导图数据结构
 * 4. 支持多种导图类型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MindMapService {
    
    private final GLMService glmService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成思维导图
     */
    public MindMapData generateMindMap(String content, String subject, MindMapType type) {
        log.info("开始生成思维导图, 科目={}, 类型={}", subject, type);
        
        MindMapData mindMap = new MindMapData();
        mindMap.setSubject(subject);
        mindMap.setType(type.name());
        
        try {
            // 1. 使用AI提取知识点和关系
            String knowledgeStructure = extractKnowledgeStructure(content, subject, type);
            
            // 2. 解析知识结构，构建节点和边
            parseKnowledgeStructure(knowledgeStructure, mindMap);
            
            // 3. 计算节点的重要度
            calculateNodeImportance(mindMap);
            
            log.info("思维导图生成完成, 节点数={}, 边数={}", 
                    mindMap.getNodes().size(), mindMap.getEdges().size());
            
        } catch (Exception e) {
            log.error("思维导图生成失败", e);
            throw new RuntimeException("思维导图生成失败: " + e.getMessage());
        }
        
        return mindMap;
    }
    
    /**
     * 使用AI提取知识结构
     */
    private String extractKnowledgeStructure(String content, String subject, MindMapType type) {
        String prompt = buildPrompt(content, subject, type);
        
        // 调用GLM-4提取知识结构
        String response = glmService.answerQuestion(subject, prompt, null);
        
        return response;
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(String content, String subject, MindMapType type) {
        String basePrompt = String.format(
            "请分析以下%s学科内容，提取知识点并建立它们之间的关系。\n\n" +
            "【内容】\n%s\n\n" +
            "请按以下格式输出（严格按格式）：\n\n" +
            "# 根节点名称\n" +
            "## 一级节点1\n" +
            "### 二级节点1-1\n" +
            "描述：这个知识点的简要说明\n" +
            "示例：具体例子\n" +
            "### 二级节点1-2\n" +
            "描述：...\n" +
            "## 一级节点2\n" +
            "...\n\n" +
            "关系：\n" +
            "- 节点A -> 节点B : 依赖（B依赖A）\n" +
            "- 节点C -> 节点D : 应用（C应用于D）\n" +
            "- 节点E <-> 节点F : 相关（E和F相关）\n",
            subject, content
        );
        
        // 根据类型添加特定要求
        switch (type) {
            case CHAPTER:
                basePrompt += "\n注意：这是章节总结，要突出知识点的系统性和完整性。";
                break;
            case TOPIC:
                basePrompt += "\n注意：这是专题梳理，要突出知识点的应用和联系。";
                break;
            case ERRORBOOK:
                basePrompt += "\n注意：这是错题分析，要突出易错点和关键概念。";
                break;
        }
        
        return basePrompt;
    }
    
    /**
     * 解析知识结构，构建节点和边
     */
    private void parseKnowledgeStructure(String structure, MindMapData mindMap) {
        String[] lines = structure.split("\n");
        
        Map<String, MindMapNode> nodeMap = new HashMap<>();
        MindMapNode rootNode = null;
        MindMapNode currentParent = null;
        int nodeIdCounter = 1;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 解析节点
            if (line.startsWith("#")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') {
                    level++;
                }
                
                String nodeName = line.substring(level).trim();
                String nodeId = "node_" + nodeIdCounter++;
                
                MindMapNode node = new MindMapNode();
                node.setId(nodeId);
                node.setLabel(nodeName);
                node.setLevel(level - 1);
                
                nodeMap.put(nodeName, node);
                mindMap.getNodes().add(node);
                
                // 设置根节点
                if (level == 1) {
                    rootNode = node;
                    mindMap.setRootId(nodeId);
                } else if (level == 2) {
                    currentParent = node;
                    if (rootNode != null) {
                        node.setParent(rootNode.getId());
                        rootNode.getChildren().add(nodeId);
                    }
                } else if (level == 3 && currentParent != null) {
                    node.setParent(currentParent.getId());
                    currentParent.getChildren().add(nodeId);
                }
            }
            // 解析描述
            else if (line.startsWith("描述：")) {
                if (!mindMap.getNodes().isEmpty()) {
                    MindMapNode lastNode = mindMap.getNodes().get(mindMap.getNodes().size() - 1);
                    lastNode.setContent(line.substring(3).trim());
                }
            }
            // 解析示例
            else if (line.startsWith("示例：")) {
                if (!mindMap.getNodes().isEmpty()) {
                    MindMapNode lastNode = mindMap.getNodes().get(mindMap.getNodes().size() - 1);
                    lastNode.setExample(line.substring(3).trim());
                }
            }
            // 解析关系
            else if (line.startsWith("-") && line.contains("->")) {
                parseEdge(line, nodeMap, mindMap);
            }
        }
        
        // 计算元数据
        mindMap.getMetadata().put("totalNodes", mindMap.getNodes().size());
        mindMap.getMetadata().put("maxDepth", calculateMaxDepth(mindMap.getNodes()));
    }
    
    /**
     * 解析边（关系）
     */
    private void parseEdge(String line, Map<String, MindMapNode> nodeMap, MindMapData mindMap) {
        // 格式：- 节点A -> 节点B : 关系类型
        Pattern pattern = Pattern.compile("-\\s*(.+?)\\s*->\\s*(.+?)\\s*:\\s*(.+)");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            String fromName = matcher.group(1).trim();
            String toName = matcher.group(2).trim();
            String relationType = matcher.group(3).trim();
            
            MindMapNode fromNode = nodeMap.get(fromName);
            MindMapNode toNode = nodeMap.get(toName);
            
            if (fromNode != null && toNode != null) {
                MindMapEdge edge = new MindMapEdge();
                edge.setFrom(fromNode.getId());
                edge.setTo(toNode.getId());
                edge.setType(parseRelationType(relationType));
                edge.setLabel(relationType);
                
                mindMap.getEdges().add(edge);
            }
        }
    }
    
    /**
     * 解析关系类型
     */
    private String parseRelationType(String relationType) {
        if (relationType.contains("依赖")) return "depend";
        if (relationType.contains("应用")) return "apply";
        if (relationType.contains("相关")) return "relate";
        if (relationType.contains("包含")) return "include";
        return "relate";
    }
    
    /**
     * 计算节点重要度
     */
    private void calculateNodeImportance(MindMapData mindMap) {
        for (MindMapNode node : mindMap.getNodes()) {
            int importance = 0;
            
            // 根据层级计算（层级越高越重要）
            importance += (3 - node.getLevel()) * 30;
            
            // 根据子节点数量
            importance += node.getChildren().size() * 10;
            
            // 根据关联边数量
            long edgeCount = mindMap.getEdges().stream()
                    .filter(e -> e.getFrom().equals(node.getId()) || e.getTo().equals(node.getId()))
                    .count();
            importance += edgeCount * 5;
            
            // 归一化到0-100
            importance = Math.min(100, importance);
            
            // 设置重要度等级
            if (importance >= 70) {
                node.setImportance("high");
            } else if (importance >= 40) {
                node.setImportance("medium");
            } else {
                node.setImportance("low");
            }
        }
    }
    
    /**
     * 计算最大深度
     */
    private int calculateMaxDepth(List<MindMapNode> nodes) {
        return nodes.stream()
                .mapToInt(MindMapNode::getLevel)
                .max()
                .orElse(0);
    }
    
    /**
     * 思维导图类型
     */
    public enum MindMapType {
        CHAPTER,    // 章节总结
        TOPIC,      // 专题梳理
        ERRORBOOK   // 错题分析
    }
    
    /**
     * 思维导图数据
     */
    @Data
    public static class MindMapData {
        private String subject;
        private String type;
        private String rootId;
        private List<MindMapNode> nodes = new ArrayList<>();
        private List<MindMapEdge> edges = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();
    }
    
    /**
     * 思维导图节点
     */
    @Data
    public static class MindMapNode {
        private String id;
        private String label;
        private int level;          // 层级 0:根节点 1:一级 2:二级...
        private String importance;  // high/medium/low
        private String parent;
        private List<String> children = new ArrayList<>();
        private String content;     // 描述
        private String example;     // 示例
    }
    
    /**
     * 思维导图边
     */
    @Data
    public static class MindMapEdge {
        private String from;
        private String to;
        private String type;  // depend/apply/relate/include
        private String label;
    }
}



