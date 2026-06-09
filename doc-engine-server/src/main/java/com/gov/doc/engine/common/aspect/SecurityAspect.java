package com.gov.doc.engine.common.aspect;

import com.gov.doc.engine.common.annotation.AuditLog;
import com.gov.doc.engine.common.annotation.RequirePermission;
import com.gov.doc.engine.service.DocAuditLogService;
import com.gov.doc.engine.service.DocPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class SecurityAspect {

    @Autowired
    private DocPermissionService permissionService;

    @Autowired
    private DocAuditLogService auditLogService;

    @Around("@annotation(com.gov.doc.engine.common.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String permissionCode = requirePermission.value();

        if (!permissionService.checkPermission(permissionCode)) {
            auditLogService.logFailure(
                    requirePermission.value().split(":").length > 1 ? requirePermission.value().split(":")[1] : requirePermission.value(),
                    "", "", "", "权限不足: " + permissionCode
            );
            throw new RuntimeException("权限不足，缺少权限: " + permissionCode);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(com.gov.doc.engine.common.annotation.AuditLog)")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        long startTime = System.currentTimeMillis();
        String result = "success";
        String errorMessage = null;
        Object returnValue = null;

        try {
            returnValue = joinPoint.proceed();
            return returnValue;
        } catch (Throwable e) {
            result = "failure";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                auditLogService.log(
                        auditLog.action(),
                        auditLog.resourceType(),
                        "", "", null,
                        auditLog.description(),
                        null, null,
                        result, errorMessage
                );
            } catch (Exception e) {
                log.warn("记录审计日志失败: {}", e.getMessage());
            }
        }
    }
}
