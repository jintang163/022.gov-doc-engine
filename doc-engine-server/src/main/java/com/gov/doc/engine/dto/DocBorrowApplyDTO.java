package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class DocBorrowApplyDTO {

    @NotNull(message = "归档记录ID不能为空")
    private Long archiveId;

    @NotBlank(message = "借阅事由不能为空")
    private String borrowReason;

    private String borrowType;

    @NotNull(message = "借阅开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "借阅结束日期不能为空")
    private LocalDate endDate;

    private String remark;
}
