package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_countersign")
public class WfCountersign extends BaseEntity {

    private String countersignNo;
    private Long processInstanceId;
    private Long processDefId;
    private String nodeId;
    private String nodeName;
    private Long taskId;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String countersignType;
    private String voteType;
    private Integer passPercentage;
    private String signOrder;
    private String status;
    private Integer totalCount;
    private Integer signedCount;
    private Integer passedCount;
    private Integer rejectedCount;
    private Integer abstainedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String variables;
    private String remark;
}
