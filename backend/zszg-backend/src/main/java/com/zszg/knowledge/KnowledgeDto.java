package com.zszg.knowledge;

import lombok.Data;

@Data
public class KnowledgeDto {
    private String subject;
    private String title;
    private String code;
    private Long parentId;
    private String textbookRef;
    private String teacherNotesUrl;
    private String videoUrl;
}


