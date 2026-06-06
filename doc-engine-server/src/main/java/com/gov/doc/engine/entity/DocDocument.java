package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_document")
public class DocDocument extends BaseEntity {

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
    private String status;
    private String currentNode;
    private String processInstanceId;
    private String creatorDeptId;
    private String creatorDeptName;
    private String unitCode;
    private String unitName;
    private String remark;
}
