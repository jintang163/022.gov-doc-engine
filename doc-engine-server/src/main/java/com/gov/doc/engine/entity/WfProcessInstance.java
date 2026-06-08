package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_instance")
public class WfProcessInstance extends BaseEntity {

    private String instanceNo;
    private Long processDefId;
    private String processCode;
    private String processName;
    private String businessKey;
    private String businessType;
    private String businessTitle;
    private String status;
    private String startUserId;
    private String startUserName;
    private String startDeptId;
    private String startDeptName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String currentNodeId;
    private String currentNodeName;
    private String formData;
    private String variables;
    private String remark;
}
