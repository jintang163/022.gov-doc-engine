package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.entity.DocSealGrant;
import com.gov.doc.engine.mapper.DocSealGrantMapper;
import com.gov.doc.engine.service.DocSealGrantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocSealGrantServiceImpl extends ServiceImpl<DocSealGrantMapper, DocSealGrant> implements DocSealGrantService {

    @Override
    public List<DocSealGrant> getGrantsBySealId(Long sealId) {
        LambdaQueryWrapper<DocSealGrant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocSealGrant::getSealId, sealId);
        wrapper.eq(DocSealGrant::getDeleted, 0);
        wrapper.orderByDesc(DocSealGrant::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<DocSealGrant> getGrantsByTarget(String grantType, String grantTargetId) {
        LambdaQueryWrapper<DocSealGrant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocSealGrant::getGrantType, grantType);
        wrapper.eq(DocSealGrant::getGrantTargetId, grantTargetId);
        wrapper.eq(DocSealGrant::getStatus, 1);
        wrapper.eq(DocSealGrant::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public boolean hasGrant(Long sealId, String grantType, String grantTargetId) {
        LambdaQueryWrapper<DocSealGrant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocSealGrant::getSealId, sealId);
        wrapper.eq(DocSealGrant::getGrantType, grantType);
        wrapper.eq(DocSealGrant::getGrantTargetId, grantTargetId);
        wrapper.eq(DocSealGrant::getStatus, 1);
        wrapper.eq(DocSealGrant::getDeleted, 0);

        DocSealGrant grant = getOne(wrapper);
        if (grant == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (grant.getGrantStartTime() != null && grant.getGrantStartTime().isAfter(now)) {
            return false;
        }
        if (grant.getGrantEndTime() != null && grant.getGrantEndTime().isBefore(now)) {
            return false;
        }
        if (grant.getSignLimit() != null && grant.getSignLimit() > 0
                && grant.getSignCount() != null && grant.getSignCount() >= grant.getSignLimit()) {
            return false;
        }

        return true;
    }
}
