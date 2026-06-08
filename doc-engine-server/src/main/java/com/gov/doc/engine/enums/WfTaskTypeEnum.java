package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfTaskTypeEnum {

    USER_TASK("userTask", "用户任务"),
    COUNTERSIGN("countersign", "会签任务");

    private final String code;
    private final String name;

    WfTaskTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfTaskTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
