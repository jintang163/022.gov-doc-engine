package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WfTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String taskNo;
    private Long processInstanceId;
    private Long processDefId;
    private String nodeId;
    private String nodeName;
    private String taskType;
    private String taskTypeName;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String status;
    private String statusName;
    private String assigneeType;
    private String assigneeTypeName;
    private String assigneeId;
    private String assigneeName;
    private String delegatedFromUserId;
    private String delegatedFromUserName;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private LocalDateTime dueTime;
    private Integer priority;
    private String priorityName;
    private Map<String, Object> formData;
    private Map<String, Object> variables;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private WfProcessInstanceVO processInstance;
    private WfCountersignVO countersign;
    private Boolean canDelegate;
    private Boolean canAddSign;
    private Boolean canTerminate;
}
