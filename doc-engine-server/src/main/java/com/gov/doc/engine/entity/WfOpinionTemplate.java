package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_opinion_template")
public class WfOpinionTemplate extends BaseEntity {

    private String templateType;
    private String templateContent;
    private Integer sortOrder;
    private Integer isPreset;
    private String userId;
    private String userName;
    private Integer status;
    private String remark;
}
