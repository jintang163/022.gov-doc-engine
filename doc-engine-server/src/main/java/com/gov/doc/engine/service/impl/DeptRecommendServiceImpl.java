package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gov.doc.engine.dto.DeptRecommendDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocIncomingMapper;
import com.gov.doc.engine.service.DeptRecommendService;
import com.gov.doc.engine.vo.DeptRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeptRecommendServiceImpl implements DeptRecommendService {

    @Autowired
    private DocIncomingMapper docIncomingMapper;

    @Autowired
    private DocHandlingMapper docHandlingMapper;

    private static final double TITLE_WEIGHT = 0.4;
    private static final double DOC_TYPE_WEIGHT = 0.3;
    private static final double KEYWORD_WEIGHT = 0.3;
    private static final double FREQUENCY_WEIGHT = 0.5;
    private static final double RECENCY_WEIGHT = 0.5;
    private static final int MAX_RECOMMEND = 3;
    private static final int TIME_DECAY_DAYS = 90;

    @Override
    public List<DeptRecommendVO> recommendDepts(DeptRecommendDTO dto) {
        List<DocIncoming> similarIncomings = findSimilarIncomings(dto);
        if (similarIncomings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> incomingIds = similarIncomings.stream()
                .map(DocIncoming::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<DocHandling> handlingWrapper = new LambdaQueryWrapper<>();
        handlingWrapper.in(DocHandling::getIncomingId, incomingIds)
                .isNotNull(DocHandling::getTargetDeptId)
                .ne(DocHandling::getTargetDeptId, "")
                .orderByDesc(DocHandling::getCreateTime);
        List<DocHandling> handlings = docHandlingMapper.selectList(handlingWrapper);

        if (handlings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<DocHandling>> deptHandlingsMap = handlings.stream()
                .collect(Collectors.groupingBy(DocHandling::getTargetDeptId));

        List<DeptRecommendVO> recommendList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, List<DocHandling>> entry : deptHandlingsMap.entrySet()) {
            String deptId = entry.getKey();
            List<DocHandling> deptHandlings = entry.getValue();
            if (!StringUtils.hasText(deptId) || deptHandlings.isEmpty()) {
                continue;
            }

            DeptRecommendVO vo = new DeptRecommendVO();
            vo.setDeptId(deptId);
            vo.setDeptName(deptHandlings.get(0).getTargetDeptName());
            vo.setMatchCount(deptHandlings.size());

            LocalDateTime lastTime = deptHandlings.stream()
                    .map(DocHandling::getCreateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (lastTime != null) {
                vo.setLastHandleTime(lastTime.toString().replace("T", " "));
            }

            double frequencyScore = calculateFrequencyScore(deptHandlings.size(), handlings.size());
            double recencyScore = calculateRecencyScore(lastTime, now);
            double matchScore = frequencyScore * FREQUENCY_WEIGHT + recencyScore * RECENCY_WEIGHT;
            matchScore = Math.round(matchScore * 100.0) / 100.0;
            vo.setMatchScore(matchScore);
            vo.setMatchScoreText(formatScoreText(matchScore));

            vo.setMatchReason(buildMatchReason(dto, deptHandlings.size(), lastTime, now));

            recommendList.add(vo);
        }

        recommendList.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return recommendList.stream()
                .limit(MAX_RECOMMEND)
                .collect(Collectors.toList());
    }

    private List<DocIncoming> findSimilarIncomings(DeptRecommendDTO dto) {
        LambdaQueryWrapper<DocIncoming> queryWrapper = new LambdaQueryWrapper<>();

        boolean hasCondition = false;

        if (StringUtils.hasText(dto.getDocTitle())) {
            String[] titleKeywords = splitKeywords(dto.getDocTitle());
            if (titleKeywords.length > 0) {
                queryWrapper.and(w -> {
                    for (String kw : titleKeywords) {
                        if (StringUtils.hasText(kw) && kw.length() >= 2) {
                            w.or().like(DocIncoming::getDocTitle, kw);
                        }
                    }
                });
                hasCondition = true;
            }
        }

        if (StringUtils.hasText(dto.getDocType())) {
            queryWrapper.or().eq(DocIncoming::getDocType, dto.getDocType());
            hasCondition = true;
        }

        if (StringUtils.hasText(dto.getKeyword())) {
            String[] keywords = splitKeywords(dto.getKeyword());
            if (keywords.length > 0) {
                queryWrapper.or().and(w -> {
                    for (String kw : keywords) {
                        if (StringUtils.hasText(kw)) {
                            w.or().like(DocIncoming::getKeyword, kw);
                        }
                    }
                });
                hasCondition = true;
            }
        }

        if (!hasCondition) {
            return Collections.emptyList();
        }

        queryWrapper.orderByDesc(DocIncoming::getCreateTime).last("LIMIT 200");
        return docIncomingMapper.selectList(queryWrapper);
    }

    private String[] splitKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return new String[0];
        }
        return text.split("[,，、;；\\s]+");
    }

    private double calculateFrequencyScore(int deptCount, int totalCount) {
        if (totalCount == 0) return 0.0;
        double ratio = (double) deptCount / totalCount;
        return Math.min(ratio * 2, 1.0);
    }

    private double calculateRecencyScore(LocalDateTime lastTime, LocalDateTime now) {
        if (lastTime == null) return 0.3;
        long daysBetween = ChronoUnit.DAYS.between(lastTime, now);
        if (daysBetween <= 0) return 1.0;
        if (daysBetween >= TIME_DECAY_DAYS) return 0.1;
        double decay = 1.0 - (double) daysBetween / TIME_DECAY_DAYS;
        return Math.max(decay * 0.9 + 0.1, 0.1);
    }

    private String formatScoreText(double score) {
        if (score >= 0.8) return "高度匹配";
        if (score >= 0.6) return "较高匹配";
        if (score >= 0.4) return "中度匹配";
        return "一般匹配";
    }

    private String buildMatchReason(DeptRecommendDTO dto, int count, LocalDateTime lastTime, LocalDateTime now) {
        List<String> reasons = new ArrayList<>();
        reasons.add("历史办理 " + count + " 次同类公文");

        if (lastTime != null) {
            long days = ChronoUnit.DAYS.between(lastTime, now);
            if (days == 0) {
                reasons.add("最近办理：今天");
            } else if (days < 30) {
                reasons.add("最近办理：" + days + " 天前");
            } else if (days < 365) {
                reasons.add("最近办理：" + (days / 30) + " 个月前");
            } else {
                reasons.add("最近办理：1 年多前");
            }
        }

        List<String> matchedFields = new ArrayList<>();
        if (StringUtils.hasText(dto.getDocTitle())) matchedFields.add("标题");
        if (StringUtils.hasText(dto.getDocType())) matchedFields.add("文种");
        if (StringUtils.hasText(dto.getKeyword())) matchedFields.add("关键词");
        if (!matchedFields.isEmpty()) {
            reasons.add("匹配维度：" + String.join("、", matchedFields));
        }

        return String.join("；", reasons);
    }
}
