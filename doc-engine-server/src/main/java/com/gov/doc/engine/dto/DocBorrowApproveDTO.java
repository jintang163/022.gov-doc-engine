package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DocBorrowApproveDTO {

    @NotNull(message = "借阅ID不能为空")
    private Long borrowId;

    @NotNull(message = "审批结果不能为空")
    private String approveResult;

    private String approveOpinion;
}
