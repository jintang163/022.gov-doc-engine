package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SysDeptSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "所属单位不能为空")
    private Long unitId;

    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private Long parentId;
    private String deptType;
    private String leader;
    private String phone;
    private String email;
    private Integer sortOrder;
    private String remark;
}
