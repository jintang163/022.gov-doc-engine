package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.entity.DocSealGrant;

import java.util.List;

public interface DocSealGrantService extends IService<DocSealGrant> {

    List<DocSealGrant> getGrantsBySealId(Long sealId);

    List<DocSealGrant> getGrantsByTarget(String grantType, String grantTargetId);

    boolean hasGrant(Long sealId, String grantType, String grantTargetId);
}
