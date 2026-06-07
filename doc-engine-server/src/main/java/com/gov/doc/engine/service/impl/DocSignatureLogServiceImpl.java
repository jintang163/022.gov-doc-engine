package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocSignatureLogQueryDTO;
import com.gov.doc.engine.entity.DocSignatureLog;
import com.gov.doc.engine.mapper.DocSignatureLogMapper;
import com.gov.doc.engine.service.DocSignatureLogService;
import com.gov.doc.engine.vo.DocSignatureLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocSignatureLogServiceImpl extends ServiceImpl<DocSignatureLogMapper, DocSignatureLog> implements DocSignatureLogService {

    @Autowired
    private DocSignatureLogMapper docSignatureLogMapper;

    @Override
    public void logOperation(String operationType, Long sealId, String sealName, Long documentId, String documentTitle,
                             Long signatureId, Long grantId, String operationDetail, Integer status, String errorMessage, String remark) {
        UserContext currentUser = UserContext.getCurrentUser();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        DocSignatureLog log = new DocSignatureLog();
        log.setOperationType(operationType);
        log.setSealId(sealId);
        log.setSealName(sealName);
        log.setDocumentId(documentId);
        log.setDocumentTitle(documentTitle);
        log.setSignatureId(signatureId);
        log.setGrantId(grantId);
        log.setOperationDetail(operationDetail);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setOperateTime(LocalDateTime.now());
        log.setOperatorId(currentUser.getUserId() != null ? String.valueOf(currentUser.getUserId()) : null);
        log.setOperatorName(currentUser.getRealName());
        log.setOperatorDeptId(currentUser.getDeptId() != null ? String.valueOf(currentUser.getDeptId()) : null);
        log.setOperatorDeptName(currentUser.getDeptName());
        log.setRemark(remark);

        docSignatureLogMapper.insert(log);
    }

    @Override
    public PageResult<DocSignatureLogVO> pageList(DocSignatureLogQueryDTO queryDTO) {
        LambdaQueryWrapper<DocSignatureLog> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOperationType())) {
            queryWrapper.eq(DocSignatureLog::getOperationType, queryDTO.getOperationType());
        }
        if (queryDTO.getSealId() != null) {
            queryWrapper.eq(DocSignatureLog::getSealId, queryDTO.getSealId());
        }
        if (queryDTO.getDocumentId() != null) {
            queryWrapper.eq(DocSignatureLog::getDocumentId, queryDTO.getDocumentId());
        }
        if (queryDTO.getSignatureId() != null) {
            queryWrapper.eq(DocSignatureLog::getSignatureId, queryDTO.getSignatureId());
        }
        if (StringUtils.hasText(queryDTO.getOperatorId())) {
            queryWrapper.eq(DocSignatureLog::getOperatorId, queryDTO.getOperatorId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(DocSignatureLog::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge(DocSignatureLog::getOperateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le(DocSignatureLog::getOperateTime, queryDTO.getEndTime());
        }

        queryWrapper.orderByDesc(DocSignatureLog::getOperateTime);

        Page<DocSignatureLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<DocSignatureLog> resultPage = docSignatureLogMapper.selectPage(page, queryWrapper);

        List<DocSignatureLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public DocSignatureLogVO getDetail(Long id) {
        DocSignatureLog log = docSignatureLogMapper.selectById(id);
        return log != null ? convertToVO(log) : null;
    }

    private DocSignatureLogVO convertToVO(DocSignatureLog log) {
        DocSignatureLogVO vo = new DocSignatureLogVO();
        BeanUtils.copyProperties(log, vo);
        vo.setOperationTypeName(getOperationTypeName(log.getOperationType()));
        vo.setStatusName(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
        return vo;
    }

    private String getOperationTypeName(String operationType) {
        if (operationType == null) {
            return "";
        }
        switch (operationType) {
            case "UPLOAD":
                return "上传";
            case "ASSIGN":
                return "分配";
            case "REVOKE":
                return "撤销";
            case "SIGN":
                return "签章";
            case "VERIFY":
                return "验章";
            default:
                return operationType;
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
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
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
