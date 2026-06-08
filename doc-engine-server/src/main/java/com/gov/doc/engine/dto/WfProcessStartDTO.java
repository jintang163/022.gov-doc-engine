package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@Data
public class WfProcessStartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "流程定义ID不能为空")
    private Long processDefId;

    private String businessKey;
    private String businessType;

    @NotBlank(message = "业务标题不能为空")
    private String businessTitle;

    private Map<String, Object> formData;
    private Map<String, Object> variables;
    private String remark;
}
