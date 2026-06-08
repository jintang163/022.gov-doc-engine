package com.gov.doc.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gov.doc.engine.entity.WfProcessHistory;
import com.gov.doc.engine.vo.WfProcessHistoryVO;

import java.util.List;

public interface WfProcessHistoryService extends IService<WfProcessHistory> {

    List<WfProcessHistoryVO> getByProcessInstanceId(Long processInstanceId);

    void recordHistory(WfProcessHistory history);

    void completeHistory(Long processInstanceId, String nodeId, String operationType);

    WfProcessHistory getActiveHistory(Long processInstanceId, String nodeId);

    WfProcessHistoryVO convertToVO(WfProcessHistory history);
}
