package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_handling")
public class DocHandling extends BaseEntity {

    private Long incomingId;
    private String handlingNo;
    private String handlingType;
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
    private String remark;
}
