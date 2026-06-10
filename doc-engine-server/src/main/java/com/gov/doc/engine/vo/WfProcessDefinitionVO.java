package com.gov.doc.engine.vo;

import com.gov.doc.engine.dto.WfProcessEdgeDTO;
import com.gov.doc.engine.dto.WfProcessNodeDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WfProcessDefinitionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String processCode;
    private String processName;
    private String processType;
    private String processTypeName;
    private String processCategory;
    private Integer version;
    private Integer isCurrentVersion;
    private Integer status;
    private String statusName;
    private String description;
    private String bpmnXml;
    private String processGraph;
    private String formKey;
    private String unitCode;
    private String unitName;
    private String remark;
    private String camundaDeploymentId;
    private String camundaProcessDefId;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private List<WfProcessNodeDTO> nodes;
    private List<WfProcessEdgeDTO> edges;
}
