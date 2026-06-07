package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocAttachmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long documentId;
    private String attachmentName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileSizeDisplay;
    private String fileType;
    private String fileExt;
    private Integer sortOrder;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
}
