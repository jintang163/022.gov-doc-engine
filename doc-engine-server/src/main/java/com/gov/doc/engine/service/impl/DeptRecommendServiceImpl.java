package com.gov.doc.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gov.doc.engine.dto.DeptRecommendDTO;
import com.gov.doc.engine.entity.DocHandling;
import com.gov.doc.engine.entity.DocIncoming;
import com.gov.doc.engine.mapper.DocHandlingMapper;
import com.gov.doc.engine.mapper.DocIncomingMapper;
import com.gov.doc.engine.service.DeptRecommendService;
import com.gov.doc.engine.vo.DeptRecommendVO;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
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

    private static final JiebaSegmenter JIEBA = new JiebaSegmenter();

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "他", "她", "们", "那", "与", "及", "等", "为", "中", "对", "从",
            "而", "但", "其", "之", "或", "将", "于", "由", "被", "把", "让", "向", "比",
            "以", "至", "所", "能", "可", "该", "此", "些", "个", "某", "每", "各",
            "关于", "加强", "做好", "开展", "推进", "进行", "进一步", "工作", "通知",
            "有关", "事项", "问题", "意见", "方案", "办法", "规定", "制度"
    ));

    private static final double TITLE_SIM_WEIGHT = 0.4;
    private static final double KEYWORD_SIM_WEIGHT = 0.3;
    private static final double DOC_TYPE_SCORE_WEIGHT = 0.3;
    private static final double FREQ_FINAL_WEIGHT = 0.5;
    private static final double SIM_FINAL_WEIGHT = 0.3;
    private static final double RECENCY_FINAL_WEIGHT = 0.2;
    private static final int MAX_RECOMMEND = 3;
    private static final int TIME_DECAY_DAYS = 90;
    private static final double MIN_SIM_THRESHOLD = 0.05;

    @Override
    public List<DeptRecommendVO> recommendDepts(DeptRecommendDTO dto) {
        boolean hasDocType = StringUtils.hasText(dto.getDocType());
        boolean hasTitle = StringUtils.hasText(dto.getDocTitle());
        boolean hasKeyword = StringUtils.hasText(dto.getKeyword());

        if (!hasDocType && !hasTitle && !hasKeyword) {
            return Collections.emptyList();
        }

        Set<String> targetTitleWords = hasTitle ? jiebaSegment(dto.getDocTitle()) : Collections.emptySet();
        Set<String> targetKwWords = hasKeyword ? splitAndSegment(dto.getKeyword()) : Collections.emptySet();

        List<DocIncoming> candidateIncomings = filterCandidateIncomings(dto.getDocType(), hasDocType);
        if (candidateIncomings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, DocIncoming> incomingMap = candidateIncomings.stream()
                .collect(Collectors.toMap(DocIncoming::getId, inc -> inc));

        List<Long> incomingIds = new ArrayList<>(incomingMap.keySet());
        LambdaQueryWrapper<DocHandling> handlingWrapper = new LambdaQueryWrapper<>();
        handlingWrapper.in(DocHandling::getIncomingId, incomingIds)
                .isNotNull(DocHandling::getTargetDeptId)
                .ne(DocHandling::getTargetDeptId, "")
                .orderByDesc(DocHandling::getCreateTime);
        List<DocHandling> handlings = docHandlingMapper.selectList(handlingWrapper);

        if (handlings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<double[]>> deptSimScores = new HashMap<>();
        Map<String, String> deptNameMap = new HashMap<>();
        Map<String, LocalDateTime> deptLastTimeMap = new HashMap<>();
        Map<String, Integer> deptCountMap = new HashMap<>();

        for (DocHandling handling : handlings) {
            String deptId = handling.getTargetDeptId();
            if (!StringUtils.hasText(deptId)) continue;

            DocIncoming inc = incomingMap.get(handling.getIncomingId());
            if (inc == null) continue;

            double titleSim = 0.0;
            if (hasTitle && StringUtils.hasText(inc.getDocTitle())) {
                Set<String> histTitleWords = jiebaSegment(inc.getDocTitle());
                titleSim = jaccardSimilarity(targetTitleWords, histTitleWords);
            }

            double kwSim = 0.0;
            if (hasKeyword && StringUtils.hasText(inc.getKeyword())) {
                Set<String> histKwWords = splitAndSegment(inc.getKeyword());
                kwSim = jaccardSimilarity(targetKwWords, histKwWords);
            }

            double docTypeScore = hasDocType && dto.getDocType().equals(inc.getDocType()) ? 1.0 : 0.0;

            double simWeightSum = 0.0;
            double weightedSim = 0.0;
            if (hasTitle) {
                weightedSim += titleSim * TITLE_SIM_WEIGHT;
                simWeightSum += TITLE_SIM_WEIGHT;
            }
            if (hasKeyword) {
                weightedSim += kwSim * KEYWORD_SIM_WEIGHT;
                simWeightSum += KEYWORD_SIM_WEIGHT;
            }
            if (hasDocType) {
                weightedSim += docTypeScore * DOC_TYPE_SCORE_WEIGHT;
                simWeightSum += DOC_TYPE_SCORE_WEIGHT;
            }
            double totalSim = simWeightSum > 0 ? weightedSim / simWeightSum : 0.0;

            if (hasTitle && hasKeyword && totalSim < MIN_SIM_THRESHOLD) {
                continue;
            }

            deptNameMap.put(deptId, handling.getTargetDeptName());
            deptSimScores.computeIfAbsent(deptId, k -> new ArrayList<>()).add(new double[]{totalSim, titleSim, kwSim});
            deptCountMap.merge(deptId, 1, Integer::sum);

            LocalDateTime handleTime = handling.getCreateTime();
            if (handleTime != null) {
                LocalDateTime existing = deptLastTimeMap.get(deptId);
                if (existing == null || handleTime.isAfter(existing)) {
                    deptLastTimeMap.put(deptId, handleTime);
                }
            }
        }

        if (deptSimScores.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> deptAvgSim = new HashMap<>();
        for (Map.Entry<String, List<double[]>> entry : deptSimScores.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(arr -> arr[0]).average().orElse(0.0);
            deptAvgSim.put(entry.getKey(), avg);
        }

        int maxCount = deptCountMap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        LocalDateTime now = LocalDateTime.now();

        List<DeptRecommendVO> recommendList = new ArrayList<>();
        for (String deptId : deptSimScores.keySet()) {
            DeptRecommendVO vo = new DeptRecommendVO();
            vo.setDeptId(deptId);
            vo.setDeptName(deptNameMap.get(deptId));

            int count = deptCountMap.get(deptId);
            vo.setMatchCount(count);

            double freqScore = (double) count / maxCount;
            double simScore = deptAvgSim.get(deptId);
            LocalDateTime lastTime = deptLastTimeMap.get(deptId);
            double recencyScore = calculateRecencyScore(lastTime, now);
            vo.setLastHandleTime(lastTime != null ? lastTime.toString().replace("T", " ") : null);

            double matchScore = freqScore * FREQ_FINAL_WEIGHT
                    + simScore * SIM_FINAL_WEIGHT
                    + recencyScore * RECENCY_FINAL_WEIGHT;
            matchScore = Math.round(matchScore * 100.0) / 100.0;
            vo.setMatchScore(matchScore);
            vo.setMatchScoreText(formatScoreText(matchScore));

            vo.setMatchReason(buildMatchReason(dto, count, lastTime, now, simScore, freqScore));

            recommendList.add(vo);
        }

        recommendList.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        return recommendList.stream()
                .limit(MAX_RECOMMEND)
                .collect(Collectors.toList());
    }

    private List<DocIncoming> filterCandidateIncomings(String docType, boolean hasDocType) {
        LambdaQueryWrapper<DocIncoming> queryWrapper = new LambdaQueryWrapper<>();

        if (hasDocType) {
            queryWrapper.eq(DocIncoming::getDocType, docType);
        }

        queryWrapper.orderByDesc(DocIncoming::getCreateTime).last("LIMIT 500");
        return docIncomingMapper.selectList(queryWrapper);
    }

    private Set<String> jiebaSegment(String text) {
        Set<String> words = new LinkedHashSet<>();
        if (!StringUtils.hasText(text)) return words;
        List<SegToken> tokens = JIEBA.process(text, JiebaSegmenter.SegMode.SEARCH);
        for (SegToken token : tokens) {
            String word = token.word.trim();
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                words.add(word);
            }
        }
        if (words.isEmpty() && text.length() >= 2) {
            for (int i = 0; i <= text.length() - 2; i++) {
                String bigram = text.substring(i, i + 2);
                if (!STOP_WORDS.contains(bigram)) {
                    words.add(bigram);
                }
            }
        }
        return words;
    }

    private Set<String> splitAndSegment(String keywordStr) {
        Set<String> words = new LinkedHashSet<>();
        if (!StringUtils.hasText(keywordStr)) return words;
        String[] parts = keywordStr.split("[,，、;；\\s]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!StringUtils.hasText(trimmed)) continue;
            if (trimmed.length() <= 4) {
                if (trimmed.length() >= 2 && !STOP_WORDS.contains(trimmed)) {
                    words.add(trimmed);
                }
            } else {
                List<SegToken> tokens = JIEBA.process(trimmed, JiebaSegmenter.SegMode.SEARCH);
                for (SegToken token : tokens) {
                    String word = token.word.trim();
                    if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                        words.add(word);
                    }
                }
            }
        }
        return words;
    }

    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        if (intersection.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
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

    private String buildMatchReason(DeptRecommendDTO dto, int count, LocalDateTime lastTime,
                                    LocalDateTime now, double simScore, double freqScore) {
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

        reasons.add(String.format("综合相似度%.0f%%，频率占比%.0f%%", simScore * 100, freqScore * 100));

        return String.join("；", reasons);
    }
}
