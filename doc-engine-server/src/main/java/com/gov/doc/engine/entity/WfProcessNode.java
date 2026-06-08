package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_node")
public class WfProcessNode extends BaseEntity {

    private Long processDefId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String nodeConfig;
    private String formProperties;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
    private String remark;
}
