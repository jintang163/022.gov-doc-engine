package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class WfTaskAddSignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String addSignType;

    @NotNull(message = "加签人员不能为空")
    private List<WfParticipantDTO> signUsers;

    private String addSignReason;
    private String remark;
}
