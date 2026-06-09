package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocAuditLogQueryDTO;
import com.gov.doc.engine.entity.DocAuditLog;
import com.gov.doc.engine.enums.PermissionActionEnum;
import com.gov.doc.engine.mapper.DocAuditLogMapper;
import com.gov.doc.engine.service.DocAuditLogService;
import com.gov.doc.engine.vo.DocAuditLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocAuditLogServiceImpl extends ServiceImpl<DocAuditLogMapper, DocAuditLog> implements DocAuditLogService {

    @Autowired
    private DocAuditLogMapper docAuditLogMapper;

    private static final Map<String, String> RESOURCE_TYPE_NAMES = new HashMap<>();

    static {
        RESOURCE_TYPE_NAMES.put("document", "公文");
        RESOURCE_TYPE_NAMES.put("template", "模板");
        RESOURCE_TYPE_NAMES.put("seal", "印章");
        RESOURCE_TYPE_NAMES.put("signature", "签章");
        RESOURCE_TYPE_NAMES.put("workflow", "流程");
        RESOURCE_TYPE_NAMES.put("archive", "档案");
        RESOURCE_TYPE_NAMES.put("borrow", "借阅");
        RESOURCE_TYPE_NAMES.put("incoming", "收文");
        RESOURCE_TYPE_NAMES.put("handling", "承办处理");
        RESOURCE_TYPE_NAMES.put("permission", "权限");
        RESOURCE_TYPE_NAMES.put("integrity", "完整性");
    }

    @Override
    public PageResult<DocAuditLogVO> pageList(DocAuditLogQueryDTO queryDTO) {
        LambdaQueryWrapper<DocAuditLog> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUserId())) {
            queryWrapper.eq(DocAuditLog::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getAction())) {
            queryWrapper.eq(DocAuditLog::getAction, queryDTO.getAction());
        }
        if (StringUtils.hasText(queryDTO.getResourceType())) {
            queryWrapper.eq(DocAuditLog::getResourceType, queryDTO.getResourceType());
        }
        if (StringUtils.hasText(queryDTO.getResourceId())) {
            queryWrapper.eq(DocAuditLog::getResourceId, queryDTO.getResourceId());
        }
        if (StringUtils.hasText(queryDTO.getSecurityLevel())) {
            queryWrapper.eq(DocAuditLog::getSecurityLevel, queryDTO.getSecurityLevel());
        }
        if (StringUtils.hasText(queryDTO.getResult())) {
            queryWrapper.eq(DocAuditLog::getResult, queryDTO.getResult());
        }
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            queryWrapper.ge(DocAuditLog::getCreateTime, queryDTO.getStartTime());
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            queryWrapper.le(DocAuditLog::getCreateTime, queryDTO.getEndTime());
        }

        queryWrapper.orderByDesc(DocAuditLog::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;
        Page<DocAuditLog> page = new Page<>(pageNum, pageSize);
        Page<DocAuditLog> resultPage = docAuditLogMapper.selectPage(page, queryWrapper);

        java.util.List<DocAuditLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public void log(String action, String resourceType, String resourceId, String resourceName,
                    String securityLevel, String description, String oldValue, String newValue,
                    String result, String errorMessage) {
        UserContext currentUser = UserContext.getCurrentUser();

        DocAuditLog auditLog = new DocAuditLog();
        auditLog.setLogNo(BizNoGenerator.generateAuditLogNo());
        auditLog.setUserId(String.valueOf(currentUser.getUserId()));
        auditLog.setUserName(currentUser.getRealName());
        auditLog.setDeptId(String.valueOf(currentUser.getDeptId()));
        auditLog.setDeptName(currentUser.getDeptName());
        auditLog.setAction(action);
        auditLog.setActionName(PermissionActionEnum.getNameByCode(action));
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setResourceName(resourceName);
        auditLog.setSecurityLevel(securityLevel);
        auditLog.setDescription(description);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setResult(result);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setCreateTime(LocalDateTime.now());

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }
        } catch (Exception ignored) {
        }

        docAuditLogMapper.insert(auditLog);
    }

    @Override
    public void logSuccess(String action, String resourceType, String resourceId, String resourceName, String description) {
        log(action, resourceType, resourceId, resourceName, null, description, null, null, "success", null);
    }

    @Override
    public void logFailure(String action, String resourceType, String resourceId, String resourceName, String errorMessage) {
        log(action, resourceType, resourceId, resourceName, null, null, null, null, "failure", errorMessage);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private DocAuditLogVO convertToVO(DocAuditLog auditLog) {
        DocAuditLogVO vo = new DocAuditLogVO();
        BeanUtils.copyProperties(auditLog, vo);
        vo.setActionName(PermissionActionEnum.getNameByCode(auditLog.getAction()));
        vo.setResourceTypeName(RESOURCE_TYPE_NAMES.getOrDefault(auditLog.getResourceType(), auditLog.getResourceType()));
        vo.setResultName("success".equals(auditLog.getResult()) ? "成功" : "失败");
        return vo;
    }
}
