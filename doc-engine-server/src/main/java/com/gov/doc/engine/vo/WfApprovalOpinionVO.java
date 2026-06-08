package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class WfApprovalOpinionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processInstanceId;
    private Long taskId;
    private String nodeId;
    private String nodeName;
    private String businessKey;
    private String approvalType;
    private String approvalTypeName;
    private String approvalResult;
    private String approvalResultName;
    private String approvalOpinion;
    private Long opinionTemplateId;
    private List<Map<String, Object>> attachments;
    private String approverId;
    private String approverName;
    private String approverDeptId;
    private String approverDeptName;
    private String targetUserId;
    private String targetUserName;
    private String targetNodeId;
    private String targetNodeName;
    private LocalDateTime approvalTime;
    private Map<String, Object> variables;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
}
