package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DocSupervisionDTO {

    @NotNull(message = "收文ID不能为空")
    private Long incomingId;

    private Long handlingId;

    @NotBlank(message = "督办类型不能为空")
    private String supervisionType;

    private String responsibleUserId;
    private String responsibleUserName;
    private String responsibleDeptId;
    private String responsibleDeptName;
    private String leaderId;
    private String leaderName;
    private String supervisionContent;
    private String remark;
}
