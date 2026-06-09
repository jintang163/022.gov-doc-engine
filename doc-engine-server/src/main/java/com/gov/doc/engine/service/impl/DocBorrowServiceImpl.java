package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.BizNoGenerator;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocBorrowApplyDTO;
import com.gov.doc.engine.dto.DocBorrowApproveDTO;
import com.gov.doc.engine.dto.DocBorrowQueryDTO;
import com.gov.doc.engine.entity.DocArchive;
import com.gov.doc.engine.entity.DocBorrow;
import com.gov.doc.engine.enums.DocBorrowStatusEnum;
import com.gov.doc.engine.mapper.DocArchiveMapper;
import com.gov.doc.engine.mapper.DocBorrowMapper;
import com.gov.doc.engine.service.DocBorrowService;
import com.gov.doc.engine.vo.DocArchiveVO;
import com.gov.doc.engine.vo.DocBorrowVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocBorrowServiceImpl extends ServiceImpl<DocBorrowMapper, DocBorrow> implements DocBorrowService {

    @Autowired
    private DocBorrowMapper docBorrowMapper;

    @Autowired
    private DocArchiveMapper docArchiveMapper;

    @Override
    public PageResult<DocBorrowVO> pageList(DocBorrowQueryDTO queryDTO) {
        LambdaQueryWrapper<DocBorrow> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(DocBorrow::getCreateTime);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        Page<DocBorrow> page = new Page<>(pageNum, pageSize);
        Page<DocBorrow> resultPage = docBorrowMapper.selectPage(page, queryWrapper);

        List<DocBorrowVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public DocBorrowVO getDetail(Long id) {
        DocBorrow borrow = docBorrowMapper.selectById(id);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        return convertToVO(borrow);
    }

    @Override
    public DocBorrowVO applyBorrow(DocBorrowApplyDTO applyDTO) {
        Long archiveId = applyDTO.getArchiveId();
        DocArchive archive = docArchiveMapper.selectById(archiveId);
        if (archive == null) {
            throw new RuntimeException("归档记录不存在");
        }

        if (applyDTO.getEndDate().isBefore(applyDTO.getStartDate())) {
            throw new RuntimeException("结束日期不能早于开始日期");
        }

        if (applyDTO.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("开始日期不能早于今天");
        }

        UserContext currentUser = UserContext.getCurrentUser();

        DocBorrow borrow = new DocBorrow();
        borrow.setArchiveId(archiveId);
        borrow.setDocId(archive.getDocId());
        borrow.setBorrowNo(generateBorrowNo());
        borrow.setApplicantId(String.valueOf(currentUser.getUserId()));
        borrow.setApplicantName(currentUser.getRealName());
        borrow.setApplicantDeptId(String.valueOf(currentUser.getDeptId()));
        borrow.setApplicantDeptName(currentUser.getDeptName());
        borrow.setBorrowReason(applyDTO.getBorrowReason());
        borrow.setBorrowType(applyDTO.getBorrowType() != null ? applyDTO.getBorrowType() : "online");
        borrow.setStartDate(applyDTO.getStartDate());
        borrow.setEndDate(applyDTO.getEndDate());
        borrow.setStatus(DocBorrowStatusEnum.PENDING.getCode());
        borrow.setWatermarkText(currentUser.getRealName() + " " + currentUser.getDeptName() + " " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        borrow.setViewCount(0);
        borrow.setRemark(applyDTO.getRemark());

        docBorrowMapper.insert(borrow);
        return convertToVO(borrow);
    }

    @Override
    public void approveBorrow(DocBorrowApproveDTO approveDTO) {
        DocBorrow borrow = docBorrowMapper.selectById(approveDTO.getBorrowId());
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (!DocBorrowStatusEnum.PENDING.getCode().equals(borrow.getStatus())) {
            throw new RuntimeException("当前借阅记录状态不是待审批，无法审批");
        }

        UserContext currentUser = UserContext.getCurrentUser();

        LambdaUpdateWrapper<DocBorrow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocBorrow::getId, approveDTO.getBorrowId());

        if ("approved".equals(approveDTO.getApproveResult())) {
            updateWrapper.set(DocBorrow::getStatus, DocBorrowStatusEnum.APPROVED.getCode());
            LocalDate today = LocalDate.now();
            if (!today.isBefore(borrow.getStartDate()) && !today.isAfter(borrow.getEndDate())) {
                updateWrapper.set(DocBorrow::getStatus, DocBorrowStatusEnum.ACTIVE.getCode());
            }
        } else if ("rejected".equals(approveDTO.getApproveResult())) {
            updateWrapper.set(DocBorrow::getStatus, DocBorrowStatusEnum.REJECTED.getCode());
        } else {
            throw new RuntimeException("审批结果只能为approved或rejected");
        }

        updateWrapper.set(DocBorrow::getApproverId, String.valueOf(currentUser.getUserId()));
        updateWrapper.set(DocBorrow::getApproverName, currentUser.getRealName());
        updateWrapper.set(DocBorrow::getApproveTime, LocalDateTime.now());
        updateWrapper.set(DocBorrow::getApproveOpinion, approveDTO.getApproveOpinion());

        docBorrowMapper.update(null, updateWrapper);
    }

    @Override
    public void returnBorrow(Long borrowId) {
        DocBorrow borrow = docBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (!DocBorrowStatusEnum.ACTIVE.getCode().equals(borrow.getStatus())
                && !DocBorrowStatusEnum.OVERDUE.getCode().equals(borrow.getStatus())) {
            throw new RuntimeException("当前借阅记录状态不允许归还");
        }

        LambdaUpdateWrapper<DocBorrow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocBorrow::getId, borrowId)
                .set(DocBorrow::getStatus, DocBorrowStatusEnum.RETURNED.getCode())
                .set(DocBorrow::getReturnTime, LocalDateTime.now());

        docBorrowMapper.update(null, updateWrapper);
    }

    @Override
    public void incrementViewCount(Long borrowId) {
        DocBorrow borrow = docBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        LambdaUpdateWrapper<DocBorrow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocBorrow::getId, borrowId)
                .set(DocBorrow::getViewCount, borrow.getViewCount() + 1)
                .set(DocBorrow::getLastViewTime, LocalDateTime.now());

        docBorrowMapper.update(null, updateWrapper);
    }

    @Override
    public String getWatermarkedContent(Long borrowId) {
        DocBorrow borrow = docBorrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (!DocBorrowStatusEnum.ACTIVE.getCode().equals(borrow.getStatus())
                && !DocBorrowStatusEnum.APPROVED.getCode().equals(borrow.getStatus())) {
            throw new RuntimeException("当前借阅状态不允许查看内容");
        }

        incrementViewCount(borrowId);

        DocArchive archive = docArchiveMapper.selectById(borrow.getArchiveId());
        if (archive == null || archive.getDocContentSnapshot() == null) {
            return "";
        }

        String content = archive.getDocContentSnapshot();
        if (StringUtils.hasText(borrow.getWatermarkText())) {
            content = addWatermarkToHtml(content, borrow.getWatermarkText());
        }

        return content;
    }

    @Override
    public PageResult<DocBorrowVO> getMyBorrows(DocBorrowQueryDTO queryDTO) {
        UserContext currentUser = UserContext.getCurrentUser();
        if (queryDTO == null) {
            queryDTO = new DocBorrowQueryDTO();
        }
        queryDTO.setApplicantId(String.valueOf(currentUser.getUserId()));
        return pageList(queryDTO);
    }

    @Override
    public PageResult<DocBorrowVO> getPendingApprovals(DocBorrowQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new DocBorrowQueryDTO();
        }
        queryDTO.setStatus(DocBorrowStatusEnum.PENDING.getCode());
        return pageList(queryDTO);
    }

    private LambdaQueryWrapper<DocBorrow> buildQueryWrapper(DocBorrowQueryDTO queryDTO) {
        LambdaQueryWrapper<DocBorrow> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(DocBorrow::getBorrowNo, keyword)
                    .or()
                    .like(DocBorrow::getBorrowReason, keyword)
                    .or()
                    .like(DocBorrow::getApplicantName, keyword));
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(DocBorrow::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getApplicantId())) {
            queryWrapper.eq(DocBorrow::getApplicantId, queryDTO.getApplicantId());
        }
        if (queryDTO.getArchiveId() != null) {
            queryWrapper.eq(DocBorrow::getArchiveId, queryDTO.getArchiveId());
        }
        if (queryDTO.getDocId() != null) {
            queryWrapper.eq(DocBorrow::getDocId, queryDTO.getDocId());
        }
        if (StringUtils.hasText(queryDTO.getBorrowType())) {
            queryWrapper.eq(DocBorrow::getBorrowType, queryDTO.getBorrowType());
        }

        return queryWrapper;
    }

    private String generateBorrowNo() {
        return BizNoGenerator.generateBorrowNo();
    }

    private DocBorrowVO convertToVO(DocBorrow borrow) {
        DocBorrowVO vo = new DocBorrowVO();
        BeanUtils.copyProperties(borrow, vo);
        vo.setStatusName(DocBorrowStatusEnum.getNameByCode(borrow.getStatus()));
        vo.setBorrowTypeName("online".equals(borrow.getBorrowType()) ? "在线借阅" : "实体借阅");

        DocArchive archive = docArchiveMapper.selectById(borrow.getArchiveId());
        if (archive != null) {
            DocArchiveVO archiveVO = new DocArchiveVO();
            BeanUtils.copyProperties(archive, archiveVO);
            archiveVO.setStatusName(com.gov.doc.engine.enums.DocArchiveStatusEnum.getNameByCode(archive.getStatus()));
            archiveVO.setArchiveMethodName("auto".equals(archive.getArchiveMethod()) ? "自动归档" : "手动归档");
            vo.setArchive(archiveVO);
        }

        return vo;
    }

    private String addWatermarkToHtml(String htmlContent, String watermarkText) {
        String watermarkStyle = "<style>"
                + ".doc-watermark {"
                + "  position: fixed;"
                + "  top: 0; left: 0; right: 0; bottom: 0;"
                + "  pointer-events: none;"
                + "  z-index: 9999;"
                + "  overflow: hidden;"
                + "}"
                + ".doc-watermark-item {"
                + "  position: absolute;"
                + "  font-size: 18px;"
                + "  color: rgba(200, 0, 0, 0.15);"
                + "  transform: rotate(-30deg);"
                + "  white-space: nowrap;"
                + "  user-select: none;"
                + "  font-family: '宋体', serif;"
                + "}"
                + "</style>";

        StringBuilder watermarkDiv = new StringBuilder();
        watermarkDiv.append("<div class='doc-watermark'>");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 6; col++) {
                int top = row * 120 + 30;
                int left = col * 200 + (row % 2 == 0 ? 0 : 100);
                watermarkDiv.append("<div class='doc-watermark-item' style='top:")
                        .append(top).append("px;left:").append(left).append("px;'>")
                        .append(watermarkText).append("</div>");
            }
        }
        watermarkDiv.append("</div>");

        if (htmlContent.contains("</head>")) {
            return htmlContent.replace("</head>", watermarkStyle + "</head>" + watermarkDiv);
        }
        if (htmlContent.contains("<body")) {
            return htmlContent.replace("<body", watermarkStyle + "<body" + watermarkDiv);
        }
        return watermarkStyle + watermarkDiv + htmlContent;
    }
}
