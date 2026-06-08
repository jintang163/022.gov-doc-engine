package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfProcessStatusEnum {

    RUNNING("running", "运行中"),
    COMPLETED("completed", "已完成"),
    SUSPENDED("suspended", "已挂起"),
    TERMINATED("terminated", "已终止");

    private final String code;
    private final String name;

    WfProcessStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfProcessStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
