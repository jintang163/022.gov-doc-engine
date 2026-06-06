package com.gov.doc.engine.common;

import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String realName;
    private Long deptId;
    private String deptCode;
    private String deptName;
    private List<Long> roleIds;
    private List<String> roleCodes;
    private String unitCode;
    private String unitName;

    private static final String DEFAULT_USER_ID = "1";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_REAL_NAME = "系统管理员";
    private static final String DEFAULT_DEPT_ID = "1";
    private static final String DEFAULT_DEPT_CODE = "DEPT001";
    private static final String DEFAULT_DEPT_NAME = "办公室";
    private static final String DEFAULT_ROLE_IDS = "1,2,3";
    private static final String DEFAULT_ROLE_CODES = "ADMIN,USER,GUEST";
    private static final String DEFAULT_UNIT_CODE = "UNIT001";
    private static final String DEFAULT_UNIT_NAME = "XX省人民政府办公厅";

    public static UserContext getCurrentUser() {
        UserContext user = new UserContext();
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                user.setUserId(getLongHeader(request, "X-User-Id", DEFAULT_USER_ID));
                user.setUsername(getHeader(request, "X-Username", DEFAULT_USERNAME));
                user.setRealName(getHeader(request, "X-Real-Name", DEFAULT_REAL_NAME));
                user.setDeptId(getLongHeader(request, "X-Dept-Id", DEFAULT_DEPT_ID));
                user.setDeptCode(getHeader(request, "X-Dept-Code", DEFAULT_DEPT_CODE));
                user.setDeptName(getHeader(request, "X-Dept-Name", DEFAULT_DEPT_NAME));
                user.setRoleIds(getLongListHeader(request, "X-Role-Ids", DEFAULT_ROLE_IDS));
                user.setRoleCodes(getStringListHeader(request, "X-Role-Codes", DEFAULT_ROLE_CODES));
                user.setUnitCode(getHeader(request, "X-Unit-Code", DEFAULT_UNIT_CODE));
                user.setUnitName(getHeader(request, "X-Unit-Name", DEFAULT_UNIT_NAME));
            } else {
                setDefaultValues(user);
            }
        } catch (Exception e) {
            setDefaultValues(user);
        }
        
        return user;
    }

    private static void setDefaultValues(UserContext user) {
        user.setUserId(Long.parseLong(DEFAULT_USER_ID));
        user.setUsername(DEFAULT_USERNAME);
        user.setRealName(DEFAULT_REAL_NAME);
        user.setDeptId(Long.parseLong(DEFAULT_DEPT_ID));
        user.setDeptCode(DEFAULT_DEPT_CODE);
        user.setDeptName(DEFAULT_DEPT_NAME);
        user.setRoleIds(parseLongList(DEFAULT_ROLE_IDS));
        user.setRoleCodes(parseStringList(DEFAULT_ROLE_CODES));
        user.setUnitCode(DEFAULT_UNIT_CODE);
        user.setUnitName(DEFAULT_UNIT_NAME);
    }

    private static String getHeader(HttpServletRequest request, String headerName, String defaultValue) {
        String value = request.getHeader(headerName);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    private static Long getLongHeader(HttpServletRequest request, String headerName, String defaultValue) {
        String value = getHeader(request, headerName, defaultValue);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return Long.parseLong(defaultValue);
        }
    }

    private static List<Long> getLongListHeader(HttpServletRequest request, String headerName, String defaultValue) {
        String value = getHeader(request, headerName, defaultValue);
        return parseLongList(value);
    }

    private static List<String> getStringListHeader(HttpServletRequest request, String headerName, String defaultValue) {
        String value = getHeader(request, headerName, defaultValue);
        return parseStringList(value);
    }

    private static List<Long> parseLongList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toList());
    }

    private static List<String> parseStringList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean hasPermission(String permissionRoles, String permissionUsers, String permissionDepts) {
        boolean noPermissionConfig = (permissionRoles == null || permissionRoles.isEmpty())
                && (permissionUsers == null || permissionUsers.isEmpty())
                && (permissionDepts == null || permissionDepts.isEmpty());
        
        if (noPermissionConfig) {
            return true;
        }

        if (permissionUsers != null && !permissionUsers.isEmpty()) {
            List<String> userIds = parseStringList(permissionUsers);
            if (userIds.contains(String.valueOf(this.userId))) {
                return true;
            }
        }

        if (permissionRoles != null && !permissionRoles.isEmpty()) {
            List<String> roleIdList = parseStringList(permissionRoles);
            for (Long roleId : this.roleIds) {
                if (roleIdList.contains(String.valueOf(roleId))) {
                    return true;
                }
            }
        }

        if (permissionDepts != null && !permissionDepts.isEmpty()) {
            List<String> deptIds = parseStringList(permissionDepts);
            if (deptIds.contains(String.valueOf(this.deptId))) {
                return true;
            }
        }

        return false;
    }
}
