package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_analysis")
public class DocAnalysis extends BaseEntity {

    private String sourceType;
    private String sourceId;
    private String businessKey;
    private String docNumber;
    private String docTitle;
    private String docType;
    private Long processInstanceId;
    private String processCode;
    private String processName;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String operatorId;
    private String operatorName;
    private String operatorDeptId;
    private String operatorDeptName;
    private String operationType;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;
    private Long duration;
    private String durationText;
    private Integer isOverdue;
    private LocalDateTime deadlineTime;
    private String variables;
    private LocalDateTime extractTime;
}
