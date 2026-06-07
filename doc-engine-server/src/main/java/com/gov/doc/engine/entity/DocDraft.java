package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_draft")
public class DocDraft extends BaseEntity {

    private Long documentId;
    private Long templateId;
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
}
