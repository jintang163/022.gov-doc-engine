package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class SysUnitSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "单位编码不能为空")
    private String unitCode;

    @NotBlank(message = "单位名称不能为空")
    private String unitName;

    private String unitType;
    private Long parentId;
    private String shortName;
    private String leader;
    private String phone;
    private String address;
    private Integer sortOrder;
    private String remark;
}
