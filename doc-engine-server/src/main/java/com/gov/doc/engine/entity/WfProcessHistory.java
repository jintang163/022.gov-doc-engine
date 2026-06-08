package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_history")
public class WfProcessHistory extends BaseEntity {

    private Long processInstanceId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Long taskId;
    private String operatorId;
    private String operatorName;
    private String operationType;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;
    private Long duration;
    private String status;
    private String variables;
    private String remark;
}
