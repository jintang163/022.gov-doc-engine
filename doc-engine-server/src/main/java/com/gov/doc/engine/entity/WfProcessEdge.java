package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_edge")
public class WfProcessEdge extends BaseEntity {

    private Long processDefId;
    private String edgeId;
    private String edgeName;
    private String sourceNodeId;
    private String targetNodeId;
    private String conditionExpression;
    private String conditionLabel;
    private String edgePoints;
    private Integer sortOrder;
    private String remark;
}
