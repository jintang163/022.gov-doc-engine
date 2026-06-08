package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_participant")
public class WfParticipant extends BaseEntity {

    private Long processDefId;
    private String nodeId;
    private String participantType;
    private String participantValue;
    private String participantName;
    private String assignmentType;
    private Integer sortOrder;
    private String remark;
}
