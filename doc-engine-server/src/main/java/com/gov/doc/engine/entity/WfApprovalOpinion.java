package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_approval_opinion")
public class WfApprovalOpinion extends BaseEntity {

    private Long processInstanceId;
    private Long taskId;
    private String nodeId;
    private String nodeName;
    private String businessKey;
    private String approvalType;
    private String approvalResult;
    private String approvalOpinion;
    private Long opinionTemplateId;
    private String attachments;
    private String approverId;
    private String approverName;
    private String approverDeptId;
    private String approverDeptName;
    private String targetUserId;
    private String targetUserName;
    private String targetNodeId;
    private String targetNodeName;
    private LocalDateTime approvalTime;
    private String variables;
    private String remark;
}
