package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class WfProcessInstanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String instanceNo;
    private String camundaProcessInstanceId;
    private Long processDefId;
    private String processCode;
    private String processName;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String status;
    private String statusName;
    private String startUserId;
    private String startUserName;
    private String startDeptId;
    private String startDeptName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String durationText;
    private String currentNodeId;
    private String currentNodeName;
    private Map<String, Object> formData;
    private Map<String, Object> variables;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private List<WfTaskVO> currentTasks;
    private List<WfApprovalOpinionVO> opinions;
    private List<WfProcessHistoryVO> history;
}
