package com.zszg.resource;

import com.zszg.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;

    /**
     * 获取所有已审核的资源
     */
    public List<ResourceItem> getApprovedResources() {
        return resourceRepository.findByApprovedTrueOrderByCreatedAtDesc();
    }

    /**
     * 获取待审核的资源
     */
    public List<ResourceItem> getPendingResources() {
        return resourceRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }

    /**
     * 按学科筛选资源
     */
    public List<ResourceItem> getResourcesBySubject(String subject) {
        return resourceRepository.findBySubjectAndApprovedTrueOrderByCreatedAtDesc(subject);
    }

    /**
     * 按类型筛选资源
     */
    public List<ResourceItem> getResourcesByType(String type) {
        return resourceRepository.findByTypeAndApprovedTrueOrderByCreatedAtDesc(type);
    }

    /**
     * 上传资源
     */
    @Transactional
    public ResourceItem uploadResource(User uploader, String title, String type, 
                                        String subject, String fileUrl) {
        ResourceItem resource = ResourceItem.builder()
                .title(title)
                .type(type)
                .subject(subject)
                .fileUrl(fileUrl)
                .uploadedBy(uploader)
                .approved(false)
                .build();
        
        return resourceRepository.save(resource);
    }

    /**
     * 审核资源
     */
    @Transactional
    public ResourceItem reviewResource(Long resourceId, User reviewer, boolean approve) {
        ResourceItem resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        
        resource.setApproved(approve);
        resource.setApprovedBy(reviewer);
        resource.setApprovedAt(Instant.now());
        
        return resourceRepository.save(resource);
    }

    /**
     * 更新资源信息
     */
    @Transactional
    public ResourceItem updateResource(Long resourceId, String title, String subject) {
        ResourceItem resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        
        if (title != null) resource.setTitle(title);
        if (subject != null) resource.setSubject(subject);
        
        return resourceRepository.save(resource);
    }

    /**
     * 删除资源
     */
    @Transactional
    public void deleteResource(Long resourceId) {
        resourceRepository.deleteById(resourceId);
    }

    /**
     * 搜索资源
     */
    public List<ResourceItem> searchResources(String keyword) {
        return resourceRepository.findByTitleContainingAndApprovedTrue(keyword);
    }
}


