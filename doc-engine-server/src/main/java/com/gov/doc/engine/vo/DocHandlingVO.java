package com.gov.doc.engine.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DocHandlingVO {

    private Long id;
    private Long incomingId;
    private String handlingNo;
    private String handlingType;
    private String handlingTypeName;
    private String opinion;
    private String handlerId;
    private String handlerName;
    private String handlerDeptId;
    private String handlerDeptName;
    private String targetDeptId;
    private String targetDeptName;
    private String targetUserId;
    private String targetUserName;
    private LocalDate deadline;
    private LocalDateTime handlingTime;
    private String feedbackContent;
    private LocalDateTime feedbackTime;
    private String feedbackAttachment;
    private LocalDateTime signReceiptTime;
    private String signReceiptIp;
    private String signReceiptUa;
    private String status;
    private String statusName;
    private String remark;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private DocIncomingVO incoming;
}
