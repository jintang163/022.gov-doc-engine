package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DocSignReceiptDTO {

    @NotNull(message = "处理ID不能为空")
    private Long handlingId;
}
