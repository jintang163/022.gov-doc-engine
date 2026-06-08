package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.dto.WfCountersignSignDTO;
import com.gov.doc.engine.entity.WfCountersign;
import com.gov.doc.engine.entity.WfCountersignItem;
import com.gov.doc.engine.vo.WfCountersignItemVO;
import com.gov.doc.engine.vo.WfCountersignVO;

import java.util.List;

public interface WfCountersignService extends IService<WfCountersign> {

    WfCountersignVO getDetail(Long id);

    WfCountersignVO getByProcessInstanceId(Long processInstanceId);

    List<WfCountersignItemVO> getCountersignItems(Long countersignId);

    void signCountersign(WfCountersignSignDTO signDTO);

    void startCountersign(WfCountersign countersign);

    void completeCountersignItem(WfCountersignItem item);

    void checkAndCompleteCountersign(Long countersignId);

    WfCountersignVO convertToVO(WfCountersign countersign);

    WfCountersignItemVO convertItemToVO(WfCountersignItem item);

    WfCountersignItem getPendingItemForUser(Long countersignId, String userId);
}
