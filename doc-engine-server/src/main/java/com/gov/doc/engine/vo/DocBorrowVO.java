package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DocBorrowVO {

    private Long id;
    private Long archiveId;
    private Long docId;
    private String borrowNo;
    private String applicantId;
    private String applicantName;
    private String applicantDeptId;
    private String applicantDeptName;
    private String borrowReason;
    private String borrowType;
    private String borrowTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String statusName;
    private String approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveOpinion;
    private LocalDateTime returnTime;
    private String watermarkText;
    private Integer viewCount;
    private LocalDateTime lastViewTime;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private DocArchiveVO archive;
}
