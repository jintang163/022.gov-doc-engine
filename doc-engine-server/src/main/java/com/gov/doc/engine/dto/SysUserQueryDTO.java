package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Data
public class SysUserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userCode;
    private String userName;
    private Long unitId;
    private Long deptId;
    private Long postId;
    private Integer status;
    private String keyword;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;
}
