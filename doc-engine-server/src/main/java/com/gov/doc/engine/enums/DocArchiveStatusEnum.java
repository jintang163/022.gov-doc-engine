package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum DocArchiveStatusEnum {

    ARCHIVED("archived", "已归档"),
    TRANSFERRED("transferred", "已移交"),
    DESTROYED("destroyed", "已销毁");

    private final String code;
    private final String name;

    DocArchiveStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocArchiveStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
