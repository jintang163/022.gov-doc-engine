package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task")
public class WfTask extends BaseEntity {

    private String taskNo;
    private String camundaTaskId;
    private Long processInstanceId;
    private Long processDefId;
    private String nodeId;
    private String nodeName;
    private String taskType;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String status;
    private String assigneeType;
    private String assigneeId;
    private String assigneeName;
    private String delegatedFromUserId;
    private String delegatedFromUserName;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private LocalDateTime dueTime;
    private Integer priority;
    private String formData;
    private String variables;
    private String remark;
}
