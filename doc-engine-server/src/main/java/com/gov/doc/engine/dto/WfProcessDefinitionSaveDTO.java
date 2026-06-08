package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class WfProcessDefinitionSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "流程编码不能为空")
    private String processCode;

    @NotBlank(message = "流程名称不能为空")
    private String processName;

    @NotBlank(message = "流程类型不能为空")
    private String processType;

    private String processCategory;
    private String description;
    private String bpmnXml;
    private String processGraph;
    private String formKey;
    private String unitCode;
    private String unitName;
    private String remark;

    private List<WfProcessNodeDTO> nodes;
    private List<WfProcessEdgeDTO> edges;
    private List<WfParticipantDTO> participants;
}
