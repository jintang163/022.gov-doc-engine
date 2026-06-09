package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum IntegrityStatusEnum {

    PENDING("pending", "待验证"),
    VERIFIED("verified", "已验证"),
    TAMPERED("tampered", "已篡改");

    private final String code;
    private final String name;

    IntegrityStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (IntegrityStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }
}
