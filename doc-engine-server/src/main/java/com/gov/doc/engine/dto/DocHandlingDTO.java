package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DocHandlingDTO {

    @NotNull(message = "收文ID不能为空")
    private Long incomingId;

    @NotBlank(message = "处理类型不能为空")
    private String handlingType;

    private String opinion;

    private String targetDeptId;

    private String targetDeptName;

    private String targetUserId;

    private String targetUserName;

    private String deadline;

    private String remark;
}
