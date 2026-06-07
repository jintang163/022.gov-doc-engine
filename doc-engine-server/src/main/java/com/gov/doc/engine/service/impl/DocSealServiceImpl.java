package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.common.PageResult;
import com.gov.doc.engine.common.UserContext;
import com.gov.doc.engine.dto.DocSealCreateDTO;
import com.gov.doc.engine.dto.DocSealGrantDTO;
import com.gov.doc.engine.dto.DocSealQueryDTO;
import com.gov.doc.engine.dto.DocSealUpdateDTO;
import com.gov.doc.engine.entity.DocSeal;
import com.gov.doc.engine.entity.DocSealGrant;
import com.gov.doc.engine.mapper.DocSealGrantMapper;
import com.gov.doc.engine.mapper.DocSealMapper;
import com.gov.doc.engine.service.DocSealService;
import com.gov.doc.engine.util.SealImageUtil;
import com.gov.doc.engine.vo.DocSealGrantVO;
import com.gov.doc.engine.vo.DocSealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocSealServiceImpl extends ServiceImpl<DocSealMapper, DocSeal> implements DocSealService {

    @Autowired
    private DocSealMapper docSealMapper;

    @Autowired
    private DocSealGrantMapper docSealGrantMapper;

    @Override
    public PageResult<DocSealVO> pageList(DocSealQueryDTO queryDTO) {
        LambdaQueryWrapper<DocSeal> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getSealName())) {
            queryWrapper.like(DocSeal::getSealName, queryDTO.getSealName());
        }
        if (StringUtils.hasText(queryDTO.getSealType())) {
            queryWrapper.eq(DocSeal::getSealType, queryDTO.getSealType());
        }
        if (StringUtils.hasText(queryDTO.getSealCode())) {
            queryWrapper.like(DocSeal::getSealCode, queryDTO.getSealCode());
        }
        if (queryDTO.getOwnerUnitId() != null) {
            queryWrapper.eq(DocSeal::getOwnerUnitId, queryDTO.getOwnerUnitId());
        }
        if (queryDTO.getOwnerDeptId() != null) {
            queryWrapper.eq(DocSeal::getOwnerDeptId, queryDTO.getOwnerDeptId());
        }
        if (queryDTO.getOwnerUserId() != null) {
            queryWrapper.eq(DocSeal::getOwnerUserId, queryDTO.getOwnerUserId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(DocSeal::getStatus, queryDTO.getStatus());
        }
        queryWrapper.orderByDesc(DocSeal::getCreateTime);

        Page<DocSeal> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<DocSeal> resultPage = docSealMapper.selectPage(page, queryWrapper);

        List<DocSealVO> voList = resultPage.getRecords().stream()
                .map(this::convertToSealVO)
                .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public DocSealVO getDetail(Long id) {
        DocSeal seal = docSealMapper.selectById(id);
        if (seal == null) {
            return null;
        }
        return convertToSealVO(seal);
    }

    @Override
    public Long createSeal(DocSealCreateDTO createDTO) {
        UserContext currentUser = UserContext.getCurrentUser();

        DocSeal seal = new DocSeal();
        BeanUtils.copyProperties(createDTO, seal);

        if (seal.getOwnerUnitId() == null) {
            seal.setOwnerUnitId(currentUser.getDeptId());
            seal.setOwnerUnitName(currentUser.getUnitName());
        }
        if (seal.getOwnerDeptId() == null) {
            seal.setOwnerDeptId(currentUser.getDeptId());
            seal.setOwnerDeptName(currentUser.getDeptName());
        }
        if (seal.getOwnerUserId() == null) {
            seal.setOwnerUserId(currentUser.getUserId());
            seal.setOwnerUserName(currentUser.getRealName());
        }
        if (seal.getStatus() == null) {
            seal.setStatus(1);
        }

        docSealMapper.insert(seal);
        return seal.getId();
    }

    @Override
    public void updateSeal(DocSealUpdateDTO updateDTO) {
        if (updateDTO.getId() == null) {
            throw new RuntimeException("印章ID不能为空");
        }

        DocSeal existingSeal = docSealMapper.selectById(updateDTO.getId());
        if (existingSeal == null) {
            throw new RuntimeException("印章不存在");
        }

        DocSeal seal = new DocSeal();
        BeanUtils.copyProperties(updateDTO, seal);
        seal.setId(updateDTO.getId());
        docSealMapper.updateById(seal);
    }

    @Override
    public void deleteSeal(Long id) {
        DocSeal seal = docSealMapper.selectById(id);
        if (seal == null) {
            throw new RuntimeException("印章不存在");
        }

        if (seal.getSealImagePath() != null) {
            SealImageUtil.deleteSealImage(seal.getSealImagePath());
        }

        LambdaQueryWrapper<DocSealGrant> grantWrapper = new LambdaQueryWrapper<>();
        grantWrapper.eq(DocSealGrant::getSealId, id);
        docSealGrantMapper.delete(grantWrapper);

        docSealMapper.deleteById(id);
    }

    @Override
    public String uploadSealImage(Long sealId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的印章图片");
        }

        DocSeal seal = docSealMapper.selectById(sealId);
        if (seal == null) {
            throw new RuntimeException("印章不存在");
        }

        try {
            String oldImagePath = seal.getSealImagePath();
            String imagePath = SealImageUtil.saveSealImage(file, sealId);
            int[] dimensions = SealImageUtil.getImageDimensions(file);

            seal.setSealImagePath(imagePath);
            seal.setSealImageName(file.getOriginalFilename());
            seal.setSealWidth(dimensions[0]);
            seal.setSealHeight(dimensions[1]);
            docSealMapper.updateById(seal);

            if (oldImagePath != null && !oldImagePath.isEmpty()) {
                SealImageUtil.deleteSealImage(oldImagePath);
            }

            return imagePath;
        } catch (Exception e) {
            throw new RuntimeException("印章图片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getSealImage(Long sealId) {
        DocSeal seal = docSealMapper.selectById(sealId);
        if (seal == null) {
            throw new RuntimeException("印章不存在");
        }
        if (seal.getSealImagePath() == null || seal.getSealImagePath().isEmpty()) {
            throw new RuntimeException("该印章未上传图片");
        }

        try {
            return SealImageUtil.readSealImage(seal.getSealImagePath());
        } catch (Exception e) {
            throw new RuntimeException("读取印章图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void grantSeal(DocSealGrantDTO grantDTO) {
        DocSeal seal = docSealMapper.selectById(grantDTO.getSealId());
        if (seal == null) {
            throw new RuntimeException("印章不存在");
        }
        if (seal.getStatus() != 1) {
            throw new RuntimeException("只能对启用状态的印章进行授权");
        }

        LambdaQueryWrapper<DocSealGrant> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(DocSealGrant::getSealId, grantDTO.getSealId());
        existingWrapper.eq(DocSealGrant::getGrantType, grantDTO.getGrantType());
        existingWrapper.eq(DocSealGrant::getGrantTargetId, grantDTO.getGrantTargetId());
        existingWrapper.eq(DocSealGrant::getStatus, 1);
        Long count = docSealGrantMapper.selectCount(existingWrapper);
        if (count > 0) {
            throw new RuntimeException("该授权已存在");
        }

        DocSealGrant grant = new DocSealGrant();
        BeanUtils.copyProperties(grantDTO, grant);
        if (grant.getSignCount() == null) {
            grant.setSignCount(0);
        }
        if (grant.getStatus() == null) {
            grant.setStatus(1);
        }
        if (grant.getGrantStartTime() == null) {
            grant.setGrantStartTime(LocalDateTime.now());
        }

        docSealGrantMapper.insert(grant);
    }

    @Override
    public void revokeGrant(Long grantId) {
        DocSealGrant grant = docSealGrantMapper.selectById(grantId);
        if (grant == null) {
            throw new RuntimeException("授权记录不存在");
        }

        grant.setStatus(0);
        docSealGrantMapper.updateById(grant);
    }

    @Override
    public List<DocSealGrantVO> getSealGrants(Long sealId) {
        LambdaQueryWrapper<DocSealGrant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocSealGrant::getSealId, sealId);
        queryWrapper.orderByDesc(DocSealGrant::getCreateTime);
        List<DocSealGrant> grants = docSealGrantMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(grants)) {
            return new ArrayList<>();
        }

        return grants.stream()
                .map(this::convertToGrantVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocSealVO> listAvailableSeals() {
        UserContext currentUser = UserContext.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<DocSeal> sealWrapper = new LambdaQueryWrapper<>();
        sealWrapper.eq(DocSeal::getStatus, 1);
        sealWrapper.orderByDesc(DocSeal::getCreateTime);
        List<DocSeal> allSeals = docSealMapper.selectList(sealWrapper);

        if (CollectionUtils.isEmpty(allSeals)) {
            return new ArrayList<>();
        }

        List<Long> sealIds = allSeals.stream()
                .map(DocSeal::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<DocSealGrant> grantWrapper = new LambdaQueryWrapper<>();
        grantWrapper.in(DocSealGrant::getSealId, sealIds);
        grantWrapper.eq(DocSealGrant::getStatus, 1);
        grantWrapper.and(w -> w.isNull(DocSealGrant::getGrantEndTime)
                .or().ge(DocSealGrant::getGrantEndTime, now));
        List<DocSealGrant> validGrants = docSealGrantMapper.selectList(grantWrapper);

        List<DocSeal> availableSeals = new ArrayList<>();
        for (DocSeal seal : allSeals) {
            if (seal.getOwnerUserId() != null && seal.getOwnerUserId().equals(currentUser.getUserId())) {
                availableSeals.add(seal);
                continue;
            }

            boolean hasValidGrant = validGrants.stream()
                    .anyMatch(grant -> {
                        if (!grant.getSealId().equals(seal.getId())) {
                            return false;
                        }
                        if ("USER".equals(grant.getGrantType())
                                && grant.getGrantTargetId().equals(currentUser.getUserId())) {
                            return true;
                        }
                        if ("DEPT".equals(grant.getGrantType())
                                && grant.getGrantTargetId().equals(currentUser.getDeptId())) {
                            return true;
                        }
                        if ("ROLE".equals(grant.getGrantType())
                                && currentUser.getRoleIds() != null
                                && currentUser.getRoleIds().contains(grant.getGrantTargetId())) {
                            return true;
                        }
                        return false;
                    });

            if (hasValidGrant) {
                availableSeals.add(seal);
            }
        }

        return availableSeals.stream()
                .map(this::convertToSealVO)
                .collect(Collectors.toList());
    }

    private DocSealVO convertToSealVO(DocSeal seal) {
        DocSealVO vo = new DocSealVO();
        BeanUtils.copyProperties(seal, vo);

        if (seal.getSealType() != null) {
            switch (seal.getSealType()) {
                case "UNIT":
                    vo.setSealTypeName("单位印章");
                    break;
                case "DEPT":
                    vo.setSealTypeName("部门印章");
                    break;
                case "SIGNATURE":
                    vo.setSealTypeName("个人签名");
                    break;
                default:
                    vo.setSealTypeName("未知");
            }
        }

        if (seal.getStatus() != null) {
            switch (seal.getStatus()) {
                case 0:
                    vo.setStatusName("禁用");
                    break;
                case 1:
                    vo.setStatusName("启用");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }

        return vo;
    }

    private DocSealGrantVO convertToGrantVO(DocSealGrant grant) {
        DocSealGrantVO vo = new DocSealGrantVO();
        BeanUtils.copyProperties(grant, vo);

        DocSeal seal = docSealMapper.selectById(grant.getSealId());
        if (seal != null) {
            vo.setSealName(seal.getSealName());
        }

        if (grant.getGrantType() != null) {
            switch (grant.getGrantType()) {
                case "USER":
                    vo.setGrantTypeName("用户");
                    break;
                case "DEPT":
                    vo.setGrantTypeName("部门");
                    break;
                case "ROLE":
                    vo.setGrantTypeName("角色");
                    break;
                default:
                    vo.setGrantTypeName("未知");
            }
        }

        if (grant.getStatus() != null) {
            switch (grant.getStatus()) {
                case 0:
                    vo.setStatusName("已撤销");
                    break;
                case 1:
                    vo.setStatusName("有效");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }

        return vo;
    }
}
