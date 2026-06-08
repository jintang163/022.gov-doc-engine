package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_countersign_item")
public class WfCountersignItem extends BaseEntity {

    private Long countersignId;
    private Long processInstanceId;
    private Long taskId;
    private String signUserId;
    private String signUserName;
    private String signUserDeptId;
    private String signUserDeptName;
    private String signType;
    private Integer signOrder;
    private String status;
    private String signResult;
    private String signOpinion;
    private LocalDateTime signTime;
    private Long duration;
    private Integer signSequence;
    private String variables;
    private String remark;
}
