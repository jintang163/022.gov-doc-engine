package com.gov.doc.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.doc.engine.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_efficiency")
public class StatEfficiency extends BaseEntity {

    private String statMonth;

    private String rankType;

    private String targetId;

    private String targetName;

    private String deptId;

    private String deptName;

    private String unitCode;

    private String unitName;

    private Long totalTask;

    private Long completedTask;

    private Long overdueTask;

    private Double completionRate;

    private Long avgDurationMinutes;

    private String avgDurationText;

    private Double efficiencyScore;

    private Integer rankNo;
}
