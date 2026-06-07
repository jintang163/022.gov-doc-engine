package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocSealGrantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "印章ID不能为空")
    private Long sealId;

    @NotBlank(message = "授权类型不能为空")
    private String grantType;

    @NotNull(message = "授权目标ID不能为空")
    private Long grantTargetId;

    private String grantTargetName;

    private LocalDateTime grantStartTime;

    private LocalDateTime grantEndTime;

    private Integer signLimit;

    private Integer status;

    private String remark;
}
