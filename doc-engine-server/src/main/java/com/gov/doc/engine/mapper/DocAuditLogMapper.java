package com.gov.doc.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.doc.engine.entity.DocAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocAuditLogMapper extends BaseMapper<DocAuditLog> {
}
