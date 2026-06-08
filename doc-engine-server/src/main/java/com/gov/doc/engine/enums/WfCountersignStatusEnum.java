package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfCountersignStatusEnum {

    PENDING("pending", "待开始"),
    SIGNING("signing", "会签中"),
    COMPLETED("completed", "已完成"),
    REJECTED("rejected", "已否决");

    private final String code;
    private final String name;

    WfCountersignStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfCountersignStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
