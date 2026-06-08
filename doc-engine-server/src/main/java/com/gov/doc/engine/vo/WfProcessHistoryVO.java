package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WfProcessHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processInstanceId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String nodeTypeName;
    private Long taskId;
    private String operatorId;
    private String operatorName;
    private String operationType;
    private String operationTypeName;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;
    private Long duration;
    private String durationText;
    private String status;
    private String statusName;
    private Map<String, Object> variables;
    private String remark;

    private WfApprovalOpinionVO opinion;
}
