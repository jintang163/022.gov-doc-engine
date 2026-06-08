package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfNodeTypeEnum {

    START("start", "开始"),
    END("end", "结束"),
    USER_TASK("userTask", "用户任务"),
    PARALLEL_GATEWAY("parallelGateway", "并行网关"),
    EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
    INCLUSIVE_GATEWAY("inclusiveGateway", "包容网关"),
    COUNTERSIGN("countersign", "会签");

    private final String code;
    private final String name;

    WfNodeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfNodeTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
