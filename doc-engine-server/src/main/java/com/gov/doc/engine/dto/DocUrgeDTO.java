package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DocUrgeDTO {

    @NotNull(message = "收文ID不能为空")
    private Long incomingId;

    private Long handlingId;

    private String urgeType;

    private String urgeContent;

    private String remark;
}
