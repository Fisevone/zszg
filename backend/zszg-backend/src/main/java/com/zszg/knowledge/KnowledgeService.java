package com.zszg.knowledge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeRepository knowledgeRepository;

    /**
     * 获取所有知识点
     */
    public List<Knowledge> getAllKnowledge() {
        return knowledgeRepository.findAll();
    }

    /**
     * 按学科获取知识点
     */
    public List<Knowledge> getKnowledgeBySubject(String subject) {
        return knowledgeRepository.findBySubject(subject);
    }

    /**
     * 获取根知识点（父级为空）
     */
    public List<Knowledge> getRootKnowledge(String subject) {
        return knowledgeRepository.findBySubjectAndParentIsNull(subject);
    }

    /**
     * 获取子知识点
     */
    public List<Knowledge> getChildrenKnowledge(Long parentId) {
        Knowledge parent = knowledgeRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        return knowledgeRepository.findByParent(parent);
    }

    /**
     * 创建知识点
     */
    @Transactional
    public Knowledge createKnowledge(String subject, String title, String code, 
                                      Long parentId, String textbookRef, 
                                      String teacherNotesUrl, String videoUrl) {
        Knowledge knowledge = new Knowledge();
        knowledge.setSubject(subject);
        knowledge.setTitle(title);
        knowledge.setCode(code);
        knowledge.setTextbookRef(textbookRef);
        knowledge.setTeacherNotesUrl(teacherNotesUrl);
        knowledge.setVideoUrl(videoUrl);
        
        if (parentId != null) {
            Knowledge parent = knowledgeRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("父知识点不存在"));
            knowledge.setParent(parent);
        }
        
        return knowledgeRepository.save(knowledge);
    }

    /**
     * 更新知识点
     */
    @Transactional
    public Knowledge updateKnowledge(Long id, String title, String textbookRef, 
                                      String teacherNotesUrl, String videoUrl) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识点不存在"));
        
        if (title != null) knowledge.setTitle(title);
        if (textbookRef != null) knowledge.setTextbookRef(textbookRef);
        if (teacherNotesUrl != null) knowledge.setTeacherNotesUrl(teacherNotesUrl);
        if (videoUrl != null) knowledge.setVideoUrl(videoUrl);
        
        return knowledgeRepository.save(knowledge);
    }

    /**
     * 删除知识点
     */
    @Transactional
    public void deleteKnowledge(Long id) {
        knowledgeRepository.deleteById(id);
    }

    /**
     * 搜索知识点
     */
    public List<Knowledge> searchKnowledge(String keyword) {
        return knowledgeRepository.findAll().stream()
                .filter(k -> k.getTitle().contains(keyword) || 
                            (k.getCode() != null && k.getCode().contains(keyword)))
                .collect(Collectors.toList());
    }
}

