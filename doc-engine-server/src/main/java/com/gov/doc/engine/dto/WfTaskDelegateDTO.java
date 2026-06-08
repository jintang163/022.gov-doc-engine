package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class WfTaskDelegateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "目标用户ID不能为空")
    private String targetUserId;

    @NotBlank(message = "目标用户姓名不能为空")
    private String targetUserName;

    private String delegateReason;
    private String remark;
}
