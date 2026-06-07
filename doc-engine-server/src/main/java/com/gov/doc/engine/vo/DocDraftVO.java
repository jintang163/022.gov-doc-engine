package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DocDraftVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long documentId;
    private Long templateId;
    private String templateName;
    private String docTitle;
    private String docNumber;
    private String docType;
    private String securityLevel;
    private String urgencyLevel;
    private String mainSendDept;
    private String copySendDept;
    private String signer;
    private LocalDate signDate;
    private LocalDate writtenDate;
    private String docContent;
    private String attachmentInfo;
    private String fieldData;
    private Integer autoSave;
    private LocalDateTime lastSaveTime;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
}
