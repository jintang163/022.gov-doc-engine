package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfApprovalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "审批类型不能为空")
    private String approvalType;

    private String approvalResult;

    @NotBlank(message = "审批意见不能为空")
    private String approvalOpinion;

    private Long opinionTemplateId;
    private List<Long> attachmentIds;
    private Map<String, Object> formData;
    private Map<String, Object> variables;

    private String targetUserId;
    private String targetUserName;
    private String targetNodeId;
    private String targetNodeName;

    private String remark;
}
