package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_borrow")
public class DocBorrow extends BaseEntity {

    private Long archiveId;
    private Long docId;
    private String borrowNo;
    private String applicantId;
    private String applicantName;
    private String applicantDeptId;
    private String applicantDeptName;
    private String borrowReason;
    private String borrowType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveOpinion;
    private LocalDateTime returnTime;
    private String watermarkText;
    private Integer viewCount;
    private LocalDateTime lastViewTime;
    private String remark;
}
