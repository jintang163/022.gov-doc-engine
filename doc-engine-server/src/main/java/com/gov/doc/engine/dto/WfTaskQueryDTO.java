package com.gov.doc.engine.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WfTaskQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assigneeId;
    private String status;
    private String taskType;
    private String businessType;
    private String businessKey;
    private Long processInstanceId;
    private Long processDefId;
    private String nodeId;
    private Integer priority;
    private String keyword;
}
