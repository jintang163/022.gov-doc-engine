package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DocIncomingVO {

    private Long id;
    private String incomingNo;
    private String source;
    private String sourceName;
    private String sourceUnit;
    private String sourceDocNumber;
    private String docTitle;
    private String docType;
    private String securityLevel;
    private String urgencyLevel;
    private LocalDate receivedDate;
    private String receivedMethod;
    private String receivedMethodName;
    private Integer copies;
    private Integer pages;
    private String docContent;
    private String attachmentInfo;
    private String keyword;
    private String abstractText;
    private String status;
    private String statusName;
    private String registrantId;
    private String registrantName;
    private String registrantDeptId;
    private String registrantDeptName;
    private String unitCode;
    private String unitName;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
}
