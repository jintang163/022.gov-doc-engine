package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocIncomingDTO;
import com.gov.doc.engine.dto.DocIncomingQueryDTO;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.enums.DocIncomingStatusEnum;
import com.gov.doc.engine.mapper.DocIncomingMapper;
import com.gov.doc.engine.service.DocIncomingService;
import com.gov.doc.engine.vo.DocIncomingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocIncomingServiceImpl extends ServiceImpl<DocIncomingMapper, DocIncoming> implements DocIncomingService {

    @Autowired
    private DocIncomingMapper docIncomingMapper;

    @Override
    public PageResult<DocIncomingVO> pageList(DocIncomingQueryDTO queryDTO) {
        LambdaQueryWrapper<DocIncoming> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(DocIncoming::getDocTitle, keyword)
                    .or()
                    .like(DocIncoming::getIncomingNo, keyword)
                    .or()
                    .like(DocIncoming::getSourceUnit, keyword)
                    .or()
                    .like(DocIncoming::getSourceDocNumber, keyword)
                    .or()
                    .like(DocIncoming::getKeyword, keyword));
        }
        if (StringUtils.hasText(queryDTO.getSource())) {
            queryWrapper.eq(DocIncoming::getSource, queryDTO.getSource());
        }
        if (StringUtils.hasText(queryDTO.getSourceUnit())) {
            queryWrapper.like(DocIncoming::getSourceUnit, queryDTO.getSourceUnit());
        }
        if (StringUtils.hasText(queryDTO.getDocType())) {
            queryWrapper.eq(DocIncoming::getDocType, queryDTO.getDocType());
        }
        if (StringUtils.hasText(queryDTO.getSecurityLevel())) {
            queryWrapper.eq(DocIncoming::getSecurityLevel, queryDTO.getSecurityLevel());
        }
        if (StringUtils.hasText(queryDTO.getUrgencyLevel())) {
            queryWrapper.eq(DocIncoming::getUrgencyLevel, queryDTO.getUrgencyLevel());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(DocIncoming::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getReceivedDateStart())) {
            queryWrapper.ge(DocIncoming::getReceivedDate, queryDTO.getReceivedDateStart());
        }
        if (StringUtils.hasText(queryDTO.getReceivedDateEnd())) {
            queryWrapper.le(DocIncoming::getReceivedDate, queryDTO.getReceivedDateEnd());
        }

        queryWrapper.orderByDesc(DocIncoming::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        Page<DocIncoming> page = new Page<>(pageNum, pageSize);
        Page<DocIncoming> resultPage = docIncomingMapper.selectPage(page, queryWrapper);

        java.util.List<DocIncomingVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public DocIncomingVO getDetail(Long id) {
        DocIncoming incoming = docIncomingMapper.selectById(id);
        if (incoming == null) {
            throw new RuntimeException("收文记录不存在");
        }
        return convertToVO(incoming);
    }

    @Override
    public DocIncomingVO register(DocIncomingDTO dto) {
        return doRegister(dto, "manual");
    }

    @Override
    public DocIncomingVO registerFromApi(DocIncomingDTO dto) {
        return doRegister(dto, "api");
    }

    private DocIncomingVO doRegister(DocIncomingDTO dto, String source) {
        UserContext currentUser = UserContext.getCurrentUser();

        DocIncoming incoming = new DocIncoming();
        BeanUtils.copyProperties(dto, incoming);
        incoming.setIncomingNo(BizNoGenerator.generateIncomingNo());
        incoming.setSource(source);
        incoming.setStatus(DocIncomingStatusEnum.REGISTERED.getCode());
        incoming.setRegistrantId(String.valueOf(currentUser.getUserId()));
        incoming.setRegistrantName(currentUser.getRealName());
        incoming.setRegistrantDeptId(String.valueOf(currentUser.getDeptId()));
        incoming.setRegistrantDeptName(currentUser.getDeptName());
        incoming.setUnitCode(currentUser.getUnitCode());
        incoming.setUnitName(currentUser.getUnitName());
        if (incoming.getCopies() == null) {
            incoming.setCopies(1);
        }

        docIncomingMapper.insert(incoming);
        return convertToVO(incoming);
    }

    private static final Map<String, String> RECEIVED_METHOD_NAMES = new HashMap<>();

    static {
        RECEIVED_METHOD_NAMES.put("mail", "邮件");
        RECEIVED_METHOD_NAMES.put("courier", "专递");
        RECEIVED_METHOD_NAMES.put("fax", "传真");
        RECEIVED_METHOD_NAMES.put("email", "电子");
        RECEIVED_METHOD_NAMES.put("in_person", "当面");
    }

    private DocIncomingVO convertToVO(DocIncoming incoming) {
        DocIncomingVO vo = new DocIncomingVO();
        BeanUtils.copyProperties(incoming, vo);
        vo.setStatusName(DocIncomingStatusEnum.getNameByCode(incoming.getStatus()));
        vo.setSourceName("manual".equals(incoming.getSource()) ? "手动录入" : "接口接入");
        vo.setReceivedMethodName(RECEIVED_METHOD_NAMES.getOrDefault(incoming.getReceivedMethod(), incoming.getReceivedMethod()));
        return vo;
    }
}
