package com.gov.doc.engine.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WfCountersignItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long countersignId;
    private Long processInstanceId;
    private Long taskId;
    private String signUserId;
    private String signUserName;
    private String signUserDeptId;
    private String signUserDeptName;
    private String signType;
    private String signTypeName;
    private Integer signOrder;
    private String status;
    private String statusName;
    private String signResult;
    private String signResultName;
    private String signOpinion;
    private LocalDateTime signTime;
    private Long duration;
    private String durationText;
    private Integer signSequence;
    private Map<String, Object> variables;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
