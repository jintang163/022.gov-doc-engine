package com.gov.doc.engine.enums;

import lombok.Getter;

@Getter
public enum SecurityLevelEnum {

    NORMAL("普通", 0),
    SECRET("秘密", 1),
    CONFIDENTIAL("机密", 2),
    TOP_SECRET("绝密", 3);

    private final String code;
    private final int level;

    SecurityLevelEnum(String code, int level) {
        this.code = code;
        this.level = level;
    }

    public static int getLevelByCode(String code) {
        if (code == null || code.isEmpty()) {
            return 0;
        }
        for (SecurityLevelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getLevel();
            }
        }
        return 0;
    }

    public static boolean canAccess(String userMaxLevel, String documentLevel) {
        int userLevel = getLevelByCode(userMaxLevel);
        int docLevel = getLevelByCode(documentLevel);
        return userLevel >= docLevel;
    }
}
