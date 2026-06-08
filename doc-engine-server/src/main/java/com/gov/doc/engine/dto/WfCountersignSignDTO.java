package com.gov.doc.engine.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class WfCountersignSignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "会签项ID不能为空")
    private Long countersignItemId;

    @NotBlank(message = "签署结果不能为空")
    private String signResult;

    @NotBlank(message = "签署意见不能为空")
    private String signOpinion;

    private List<Long> attachmentIds;
    private Map<String, Object> variables;
    private String remark;
}
