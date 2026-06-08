package com.gov.doc.engine.service.impl;

import com.gov.doc.engine.entity.WfParticipant;
import com.gov.doc.engine.enums.WfParticipantTypeEnum;
import com.gov.doc.engine.service.WfParticipantResolverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
public class WfParticipantResolverServiceImpl implements WfParticipantResolverService {

    @Override
    public List<ResolvedParticipant> resolveParticipants(List<WfParticipant> participants, Map<String, Object> variables) {
        if (participants == null || participants.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResolvedParticipant> resolved = new ArrayList<>();
        for (WfParticipant participant : participants) {
            ResolvedParticipant rp = resolveParticipant(participant, variables);
            if (rp != null) {
                resolved.add(rp);
            }
        }
        return resolved;
    }

    @Override
    public ResolvedParticipant resolveParticipant(WfParticipant participant, Map<String, Object> variables) {
        if (participant == null) {
            return null;
        }

        String type = participant.getParticipantType();
        String value = participant.getParticipantValue();
        String name = participant.getParticipantName();

        if (WfParticipantTypeEnum.USER.getCode().equals(type)) {
            return new ResolvedParticipant(value, name, type, value, name);
        }

        if (WfParticipantTypeEnum.POST.getCode().equals(type)) {
            return resolveByPost(value, name, variables);
        }

        if (WfParticipantTypeEnum.DEPT.getCode().equals(type)) {
            return resolveByDept(value, name, variables);
        }

        if (WfParticipantTypeEnum.ROLE.getCode().equals(type)) {
            return resolveByRole(value, name, variables);
        }

        if (WfParticipantTypeEnum.EXPRESSION.getCode().equals(type)) {
            return resolveByExpression(value, name, variables);
        }

        log.warn("Unknown participant type: {}, using default resolution", type);
        return new ResolvedParticipant(value, name, type, value, name);
    }

    private ResolvedParticipant resolveByPost(String postId, String postName, Map<String, Object> variables) {
        String userId = getFromVariables(variables, "post_" + postId + "_userId");
        String userName = getFromVariables(variables, "post_" + postId + "_userName");

        if (userId == null) {
            userId = "post_user_" + postId;
            userName = (postName != null ? postName : "岗位") + "负责人";
            log.warn("Post {} user not found in variables, using placeholder: {}", postId, userId);
        }

        return new ResolvedParticipant(userId, userName, WfParticipantTypeEnum.POST.getCode(), postId, postName);
    }

    private ResolvedParticipant resolveByDept(String deptId, String deptName, Map<String, Object> variables) {
        String userId = getFromVariables(variables, "dept_" + deptId + "_userId");
        String userName = getFromVariables(variables, "dept_" + deptId + "_userName");

        if (userId == null) {
            userId = "dept_user_" + deptId;
            userName = (deptName != null ? deptName : "部门") + "负责人";
            log.warn("Dept {} user not found in variables, using placeholder: {}", deptId, userId);
        }

        return new ResolvedParticipant(userId, userName, WfParticipantTypeEnum.DEPT.getCode(), deptId, deptName);
    }

    private ResolvedParticipant resolveByRole(String roleId, String roleName, Map<String, Object> variables) {
        String userId = getFromVariables(variables, "role_" + roleId + "_userId");
        String userName = getFromVariables(variables, "role_" + roleId + "_userName");

        if (userId == null) {
            userId = "role_user_" + roleId;
            userName = (roleName != null ? roleName : "角色") + "用户";
            log.warn("Role {} user not found in variables, using placeholder: {}", roleId, userId);
        }

        return new ResolvedParticipant(userId, userName, WfParticipantTypeEnum.ROLE.getCode(), roleId, roleName);
    }

    private ResolvedParticipant resolveByExpression(String expression, String expressionDesc, Map<String, Object> variables) {
        if (expression == null || variables == null) {
            return new ResolvedParticipant("expression_user", "表达式用户",
                    WfParticipantTypeEnum.EXPRESSION.getCode(), expression, expressionDesc);
        }

        String cleanedExpr = expression.trim();
        if (cleanedExpr.startsWith("${") && cleanedExpr.endsWith("}")) {
            String varName = cleanedExpr.substring(2, cleanedExpr.length() - 1);
            Object value = variables.get(varName);
            if (value != null) {
                String userId = value.toString();
                String userName = getFromVariables(variables, varName + "_name");
                if (userName == null) {
                    userName = userId;
                }
                return new ResolvedParticipant(userId, userName,
                        WfParticipantTypeEnum.EXPRESSION.getCode(), expression, expressionDesc);
            }
        }

        log.warn("Expression {} cannot be resolved, using placeholder", expression);
        return new ResolvedParticipant("expression_user_" + System.currentTimeMillis(), "表达式用户",
                WfParticipantTypeEnum.EXPRESSION.getCode(), expression, expressionDesc);
    }

    private String getFromVariables(Map<String, Object> variables, String key) {
        if (variables == null || !StringUtils.hasText(key)) {
            return null;
        }
        Object value = variables.get(key);
        return value != null ? value.toString() : null;
    }
}
