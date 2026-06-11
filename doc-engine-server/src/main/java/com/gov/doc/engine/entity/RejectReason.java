package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reject_reason")
public class RejectReason extends BaseEntity {

    private String sourceType;
    private String sourceId;
    private String businessKey;
    private String docNumber;
    private String docTitle;
    private Long processInstanceId;
    private Long taskId;
    private String nodeId;
    private String nodeName;
    private String approvalType;
    private String rejectReason;
    private String approverId;
    private String approverName;
    private String approverDeptId;
    private String approverDeptName;
    private String targetNodeId;
    private String targetNodeName;
    private String targetUserId;
    private String targetUserName;
    private LocalDateTime approvalTime;
    private LocalDateTime extractTime;
}
