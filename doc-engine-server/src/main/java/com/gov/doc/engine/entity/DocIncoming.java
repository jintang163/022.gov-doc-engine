package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_incoming")
public class DocIncoming extends BaseEntity {

    private String incomingNo;
    private String source;
    private String sourceUnit;
    private String sourceDocNumber;
    private String docTitle;
    private String docType;
    private String securityLevel;
    private String urgencyLevel;
    private LocalDate receivedDate;
    private String receivedMethod;
    private Integer copies;
    private Integer pages;
    private String docContent;
    private String attachmentInfo;
    private String keyword;
    private String abstractText;
    private String status;
    private String registrantId;
    private String registrantName;
    private String registrantDeptId;
    private String registrantDeptName;
    private String unitCode;
    private String unitName;
    private String remark;
}
