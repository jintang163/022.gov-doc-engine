package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DocArchiveVO {

    private Long id;
    private Long docId;
    private String archiveNo;
    private Integer archiveYear;
    private String archiveType;
    private String archiveDeptId;
    private String archiveDeptName;
    private String securityLevel;
    private String archiveMethod;
    private String archiveMethodName;
    private String archiveLocation;
    private String retentionPeriod;
    private String retentionPeriodName;
    private LocalDate archiveDate;
    private Integer isLocked;
    private String status;
    private String statusName;
    private String docTitle;
    private String docNumber;
    private String docType;
    private String docContentSnapshot;
    private String unitCode;
    private String unitName;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
}
