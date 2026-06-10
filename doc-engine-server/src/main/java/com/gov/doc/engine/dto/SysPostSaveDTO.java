package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class SysPostSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long unitId;

    @NotBlank(message = "岗位编码不能为空")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    private String postType;
    private Integer postLevel;
    private Long deptId;
    private Integer sortOrder;
    private String remark;
}
