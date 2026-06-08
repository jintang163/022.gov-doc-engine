package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_definition")
public class WfProcessDefinition extends BaseEntity {

    private String processCode;
    private String processName;
    private String processType;
    private String processCategory;
    private Integer version;
    private Integer isCurrentVersion;
    private Integer status;
    private String description;
    private String bpmnXml;
    private String processGraph;
    private String formKey;
    private String unitCode;
    private String unitName;
    private String remark;
}
