package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class DocTemplateSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    private String templateCategory;

    private Integer version;

    private Integer isCurrentVersion;

    private Long parentTemplateId;

    private Integer status;

    private String description;

    private String unitCode;

    private String unitName;

    private String securityLevel;

    private String urgencyLevel;

    private String permissionRoles;

    private String permissionUsers;

    private String permissionDepts;

    private String remark;

    @Valid
    @NotNull(message = "红头样式不能为空")
    private DocTemplateHeaderDTO header;

    @Valid
    private List<DocTemplateFieldDTO> fields;
}
