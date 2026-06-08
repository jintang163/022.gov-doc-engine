package com.gov.doc.engine.enums;

import lombok.Getter;

import java.util.*;

@Getter
public enum DocStatusEnum {

    DRAFT("draft", "起草"),
    REVIEWING("reviewing", "审核中"),
    COUNTERSIGNING("countersigning", "会签中"),
    PENDING_SIGN("pending_sign", "待签发"),
    SIGNED("signed", "已签发"),
    DISTRIBUTING("distributing", "分发中"),
    ARCHIVED("archived", "归档"),
    ABOLISHED("abolished", "废止");

    private final String code;
    private final String name;

    private static final Map<String, Set<String>> TRANSITION_MAP = new HashMap<>();

    static {
        TRANSITION_MAP.put(DRAFT.code, Set.of(REVIEWING.code, ABOLISHED.code));
        TRANSITION_MAP.put(REVIEWING.code, Set.of(DRAFT.code, COUNTERSIGNING.code, PENDING_SIGN.code, ABOLISHED.code));
        TRANSITION_MAP.put(COUNTERSIGNING.code, Set.of(REVIEWING.code, PENDING_SIGN.code, ABOLISHED.code));
        TRANSITION_MAP.put(PENDING_SIGN.code, Set.of(REVIEWING.code, COUNTERSIGNING.code, SIGNED.code, ABOLISHED.code));
        TRANSITION_MAP.put(SIGNED.code, Set.of(DISTRIBUTING.code, ABOLISHED.code));
        TRANSITION_MAP.put(DISTRIBUTING.code, Set.of(ARCHIVED.code, ABOLISHED.code));
        TRANSITION_MAP.put(ARCHIVED.code, Set.of(ABOLISHED.code));
        TRANSITION_MAP.put(ABOLISHED.code, Collections.emptySet());
    }

    DocStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DocStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return code;
    }

    public static boolean canTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        Set<String> allowedTransitions = TRANSITION_MAP.get(fromStatus);
        return allowedTransitions != null && allowedTransitions.contains(toStatus);
    }

    public static Set<String> getAllowedTransitions(String status) {
        if (status == null) {
            return Collections.emptySet();
        }
        return TRANSITION_MAP.getOrDefault(status, Collections.emptySet());
    }

    public static boolean isValidStatus(String code) {
        for (DocStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
