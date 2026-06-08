package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.doc.engine.entity.DocStatusLog;
import com.gov.doc.engine.mapper.DocStatusLogMapper;
import com.gov.doc.engine.service.DocStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocStatusLogServiceImpl extends ServiceImpl<DocStatusLogMapper, DocStatusLog> implements DocStatusLogService {

    @Autowired
    private DocStatusLogMapper docStatusLogMapper;

    @Override
    public List<DocStatusLog> getByDocId(Long docId) {
        LambdaQueryWrapper<DocStatusLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocStatusLog::getDocId, docId);
        queryWrapper.orderByAsc(DocStatusLog::getOperationTime);
        return docStatusLogMapper.selectList(queryWrapper);
    }

    @Override
    public DocStatusLog getLatestStatus(Long docId) {
        LambdaQueryWrapper<DocStatusLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocStatusLog::getDocId, docId);
        queryWrapper.orderByDesc(DocStatusLog::getOperationTime);
        queryWrapper.last("LIMIT 1");
        return docStatusLogMapper.selectOne(queryWrapper);
    }
}
