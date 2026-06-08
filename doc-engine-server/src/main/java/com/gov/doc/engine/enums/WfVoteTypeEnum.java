package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum WfVoteTypeEnum {

    ONE_PASS("one_pass", "一票通过"),
    ALL_PASS("all_pass", "全部通过"),
    ONE_REJECT("one_reject", "一票否决"),
    PERCENTAGE("percentage", "百分比通过");

    private final String code;
    private final String name;

    WfVoteTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (WfVoteTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
