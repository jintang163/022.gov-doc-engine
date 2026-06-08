package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class WfOpinionTemplateSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    @NotBlank(message = "模板内容不能为空")
    private String templateContent;

    private Integer sortOrder;
    private String remark;
}
