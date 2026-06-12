package com.gov.doc.engine;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class DeptRecommendStandaloneTest {

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
    private static final int TIME_DECAY_DAYS = 90;

    static class HistoryDoc {
        String title;
        String docType;
        String keyword;
        String deptId;
        String deptName;
        LocalDateTime handleTime;

        HistoryDoc(String title, String docType, String keyword, String deptId, String deptName, LocalDateTime handleTime) {
            this.title = title;
            this.docType = docType;
            this.keyword = keyword;
            this.deptId = deptId;
            this.deptName = deptName;
            this.handleTime = handleTime;
        }
    }

    static class RecommendResult {
        String deptId;
        String deptName;
        double matchScore;
        String matchScoreText;
        int matchCount;
        String lastHandleTime;
        String matchReason;

        @Override
        public String toString() {
            return String.format("  部门: %s (id=%s) | 匹配度: %.2f (%s) | 历史%d次 | 最近: %s%n    原因: %s",
                    deptName, deptId, matchScore, matchScoreText, matchCount, lastHandleTime, matchReason);
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  智能分办推荐算法 - 独立验证测试");
        System.out.println("========================================");

        System.out.println("\n【测试1】中文分词验证");
        testJiebaSegmentation();

        System.out.println("\n【测试2】Jaccard相似度验证");
        testSimilarity();

        System.out.println("\n【测试3】完整推荐场景（基于历史测试数据）");
        testFullRecommendation();

        System.out.println("\n【测试4】表单填充验证");
        testFormFill();
    }

    private static void testJiebaSegmentation() {
        String title = "关于加强财政预算绩效管理的通知";
        Set<String> words = jiebaSegment(title);
        System.out.println("  标题: " + title);
        System.out.println("  分词结果: " + words);

        boolean pass = words.contains("财政") && words.contains("预算")
                && words.contains("绩效") && !words.contains("关于")
                && !words.contains("的") && !words.contains("加强");
        System.out.println("  结果: " + (pass ? "✅ PASS" : "❌ FAIL"));

        String keyword = "财政,预算,绩效";
        Set<String> kwWords = splitAndSegment(keyword);
        System.out.println("  关键词: " + keyword);
        System.out.println("  分词结果: " + kwWords);
    }

    private static void testSimilarity() {
        String t1 = "关于加强财政预算绩效管理的通知";
        String t2 = "关于加强财政预算管理工作的通知";
        String t3 = "关于推进财政信息化建设的通知";
        String t4 = "关于深化预算管理制度改革的批复";

        Set<String> w1 = jiebaSegment(t1);
        Set<String> w2 = jiebaSegment(t2);
        Set<String> w3 = jiebaSegment(t3);
        Set<String> w4 = jiebaSegment(t4);

        double s12 = jaccardSimilarity(w1, w2);
        double s13 = jaccardSimilarity(w1, w3);
        double s14 = jaccardSimilarity(w1, w4);

        System.out.printf("  '%s' vs '%s' = %.3f%n", t1, t2, s12);
        System.out.printf("  '%s' vs '%s' = %.3f%n", t1, t3, s13);
        System.out.printf("  '%s' vs '%s' = %.3f%n", t1, t4, s14);

        boolean pass = s12 > s13 && s13 > s14 && s12 > 0.3;
        System.out.println("  结果: " + (pass ? "✅ PASS (相似度排序正确)" : "❌ FAIL"));
    }

    private static void testFullRecommendation() {
        List<HistoryDoc> history = buildHistoryData();

        String targetTitle = "关于加强财政预算绩效管理的通知";
        String targetDocType = "通知";
        String targetKeyword = "财政,预算,绩效";

        System.out.println("  目标公文: " + targetTitle);
        System.out.println("  目标文种: " + targetDocType);
        System.out.println("  目标关键词: " + targetKeyword);
        System.out.println("  历史数据: " + history.size() + " 条");

        List<RecommendResult> results = recommend(targetTitle, targetDocType, targetKeyword, history);

        System.out.println("\n  --- 推荐结果 ---");
        for (int i = 0; i < results.size(); i++) {
            System.out.println("  #" + (i + 1) + " " + results.get(i));
        }

        boolean pass = !results.isEmpty()
                && results.size() <= 3
                && "1002".equals(results.get(0).deptId)
                && "财政审计科".equals(results.get(0).deptName);
        System.out.println("\n  结果: " + (pass ? "✅ PASS (财政审计科排名第一，符合预期)" : "❌ FAIL"));

        if (results.size() >= 2) {
            boolean ordered = results.get(0).matchScore >= results.get(1).matchScore;
            if (results.size() >= 3) {
                ordered = ordered && results.get(1).matchScore >= results.get(2).matchScore;
            }
            System.out.println("  排序验证: " + (ordered ? "✅ PASS (按匹配度降序)" : "❌ FAIL"));
        }
    }

    private static void testFormFill() {
        List<HistoryDoc> history = buildHistoryData();
        List<RecommendResult> results = recommend(
                "关于加强财政预算绩效管理的通知", "通知", "财政,预算,绩效", history);

        System.out.println("  模拟前端「一键采纳」操作...");

        if (!results.isEmpty()) {
            RecommendResult top = results.get(0);
            String formDeptId = top.deptId;
            String formDeptName = top.deptName;

            System.out.printf("  填入 targetDeptId = '%s'%n", formDeptId);
            System.out.printf("  填入 targetDeptName = '%s'%n", formDeptName);
            System.out.printf("  匹配度 = %.2f (%s)%n", top.matchScore, top.matchScoreText);

            boolean pass = formDeptId != null && formDeptId.matches("\\d+")
                    && formDeptName != null && !formDeptName.trim().isEmpty();
            System.out.println("  结果: " + (pass ? "✅ PASS (表单字段可正确填充)" : "❌ FAIL"));
        } else {
            System.out.println("  结果: ❌ FAIL (无推荐结果)");
        }
    }

    private static List<HistoryDoc> buildHistoryData() {
        List<HistoryDoc> list = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2025, 1, 10, 10, 0);
        list.add(new HistoryDoc("关于加强财政预算管理工作的通知", "通知", "财政,预算,管理", "1002", "财政审计科", base));
        list.add(new HistoryDoc("关于开展年度财政审计检查的通知", "通知", "财政,审计,检查", "1002", "财政审计科", base.plusDays(5)));
        list.add(new HistoryDoc("关于加强财政资金监管的通知", "通知", "财政,资金,监管", "1002", "财政审计科", base.plusDays(20)));
        list.add(new HistoryDoc("关于加强财政资金监管的通知", "通知", "财政,资金,监管", "1003", "政策法规科", base.plusDays(20)));
        list.add(new HistoryDoc("关于深化预算管理制度改革的批复", "批复", "预算,管理,改革", "1003", "政策法规科", base.plusDays(45)));
        list.add(new HistoryDoc("关于开展财政专项审计的通知", "通知", "财政,审计,专项", "1002", "财政审计科", base.plusDays(60)));
        list.add(new HistoryDoc("关于调整财政管理体制的函", "函", "财政,体制,调整", "1003", "政策法规科", base.plusDays(80)));
        list.add(new HistoryDoc("关于加强预算执行管理的通知", "通知", "预算,执行,管理", "1002", "财政审计科", base.plusDays(95)));
        list.add(new HistoryDoc("关于做好财政决算工作的通知", "通知", "财政,决算", "1002", "财政审计科", base.plusDays(110)));
        list.add(new HistoryDoc("关于开展经济责任审计的请示", "请示", "经济责任,审计", "1002", "财政审计科", base.plusDays(125)));
        list.add(new HistoryDoc("关于推进财政信息化建设的通知", "通知", "财政,信息化,建设", "1005", "信息技术科", base.plusDays(140)));
        return list;
    }

    private static List<RecommendResult> recommend(String targetTitle, String targetDocType,
                                                    String targetKeyword, List<HistoryDoc> history) {
        boolean hasDocType = targetDocType != null && !targetDocType.isEmpty();
        boolean hasTitle = targetTitle != null && !targetTitle.isEmpty();
        boolean hasKeyword = targetKeyword != null && !targetKeyword.isEmpty();

        Set<String> targetTitleWords = hasTitle ? jiebaSegment(targetTitle) : Collections.emptySet();
        Set<String> targetKwWords = hasKeyword ? splitAndSegment(targetKeyword) : Collections.emptySet();

        List<HistoryDoc> filtered = history;
        if (hasDocType) {
            filtered = history.stream()
                    .filter(h -> targetDocType.equals(h.docType))
                    .collect(Collectors.toList());
            System.out.printf("  先按文种'%s'过滤: %d → %d 条%n", targetDocType, history.size(), filtered.size());
        }

        Map<String, List<double[]>> deptSimScores = new HashMap<>();
        Map<String, String> deptNameMap = new HashMap<>();
        Map<String, LocalDateTime> deptLastTimeMap = new HashMap<>();
        Map<String, Integer> deptCountMap = new HashMap<>();

        for (HistoryDoc h : filtered) {
            double titleSim = 0.0;
            if (hasTitle && h.title != null) {
                Set<String> histTitleWords = jiebaSegment(h.title);
                titleSim = jaccardSimilarity(targetTitleWords, histTitleWords);
            }

            double kwSim = 0.0;
            if (hasKeyword && h.keyword != null) {
                Set<String> histKwWords = splitAndSegment(h.keyword);
                kwSim = jaccardSimilarity(targetKwWords, histKwWords);
            }

            double docTypeScore = hasDocType && targetDocType.equals(h.docType) ? 1.0 : 0.0;

            double simWeightSum = 0.0;
            double weightedSim = 0.0;
            if (hasTitle) { weightedSim += titleSim * TITLE_SIM_WEIGHT; simWeightSum += TITLE_SIM_WEIGHT; }
            if (hasKeyword) { weightedSim += kwSim * KEYWORD_SIM_WEIGHT; simWeightSum += KEYWORD_SIM_WEIGHT; }
            if (hasDocType) { weightedSim += docTypeScore * DOC_TYPE_SCORE_WEIGHT; simWeightSum += DOC_TYPE_SCORE_WEIGHT; }
            double totalSim = simWeightSum > 0 ? weightedSim / simWeightSum : 0.0;

            deptNameMap.put(h.deptId, h.deptName);
            deptSimScores.computeIfAbsent(h.deptId, k -> new ArrayList<>()).add(new double[]{totalSim, titleSim, kwSim});
            deptCountMap.merge(h.deptId, 1, Integer::sum);

            LocalDateTime existing = deptLastTimeMap.get(h.deptId);
            if (existing == null || h.handleTime.isAfter(existing)) {
                deptLastTimeMap.put(h.deptId, h.handleTime);
            }
        }

        Map<String, Double> deptAvgSim = new HashMap<>();
        for (Map.Entry<String, List<double[]>> e : deptSimScores.entrySet()) {
            deptAvgSim.put(e.getKey(), e.getValue().stream().mapToDouble(a -> a[0]).average().orElse(0.0));
        }

        int maxCount = deptCountMap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        LocalDateTime now = LocalDateTime.now();

        List<RecommendResult> recommendList = new ArrayList<>();
        for (String deptId : deptSimScores.keySet()) {
            RecommendResult vo = new RecommendResult();
            vo.deptId = deptId;
            vo.deptName = deptNameMap.get(deptId);
            int count = deptCountMap.get(deptId);
            vo.matchCount = count;
            double freqScore = (double) count / maxCount;
            double simScore = deptAvgSim.get(deptId);
            LocalDateTime lastTime = deptLastTimeMap.get(deptId);
            double recencyScore = calcRecency(lastTime, now);
            vo.lastHandleTime = lastTime != null ? lastTime.toString().replace("T", " ") : null;

            double matchScore = freqScore * FREQ_FINAL_WEIGHT + simScore * SIM_FINAL_WEIGHT + recencyScore * RECENCY_FINAL_WEIGHT;
            vo.matchScore = Math.round(matchScore * 100.0) / 100.0;
            vo.matchScoreText = formatScore(vo.matchScore);
            vo.matchReason = buildReason(targetDocType, targetTitle, targetKeyword, count, lastTime, now, simScore, freqScore);

            recommendList.add(vo);
        }

        recommendList.sort((a, b) -> Double.compare(b.matchScore, a.matchScore));
        return recommendList.stream().limit(3).collect(Collectors.toList());
    }

    private static Set<String> jiebaSegment(String text) {
        Set<String> words = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) return words;
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
                if (!STOP_WORDS.contains(bigram)) words.add(bigram);
            }
        }
        return words;
    }

    private static Set<String> splitAndSegment(String keywordStr) {
        Set<String> words = new LinkedHashSet<>();
        if (keywordStr == null || keywordStr.isEmpty()) return words;
        String[] parts = keywordStr.split("[,，、;；\\s]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.length() <= 4) {
                if (trimmed.length() >= 2 && !STOP_WORDS.contains(trimmed)) words.add(trimmed);
            } else {
                List<SegToken> tokens = JIEBA.process(trimmed, JiebaSegmenter.SegMode.SEARCH);
                for (SegToken token : tokens) {
                    String word = token.word.trim();
                    if (word.length() >= 2 && !STOP_WORDS.contains(word)) words.add(word);
                }
            }
        }
        return words;
    }

    private static double jaccardSimilarity(Set<String> s1, Set<String> s2) {
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(s1);
        inter.retainAll(s2);
        if (inter.isEmpty()) return 0.0;
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        return (double) inter.size() / union.size();
    }

    private static double calcRecency(LocalDateTime lastTime, LocalDateTime now) {
        if (lastTime == null) return 0.3;
        long days = ChronoUnit.DAYS.between(lastTime, now);
        if (days <= 0) return 1.0;
        if (days >= TIME_DECAY_DAYS) return 0.1;
        return Math.max((1.0 - (double) days / TIME_DECAY_DAYS) * 0.9 + 0.1, 0.1);
    }

    private static String formatScore(double s) {
        if (s >= 0.8) return "高度匹配";
        if (s >= 0.6) return "较高匹配";
        if (s >= 0.4) return "中度匹配";
        return "一般匹配";
    }

    private static String buildReason(String docType, String title, String keyword,
                                      int count, LocalDateTime lastTime, LocalDateTime now,
                                      double simScore, double freqScore) {
        List<String> reasons = new ArrayList<>();
        reasons.add("历史办理 " + count + " 次同类公文");
        if (lastTime != null) {
            long days = ChronoUnit.DAYS.between(lastTime, now);
            if (days == 0) reasons.add("最近办理：今天");
            else if (days < 30) reasons.add("最近办理：" + days + " 天前");
            else if (days < 365) reasons.add("最近办理：" + (days / 30) + " 个月前");
            else reasons.add("最近办理：1 年多前");
        }
        List<String> dims = new ArrayList<>();
        if (title != null && !title.isEmpty()) dims.add("标题");
        if (docType != null && !docType.isEmpty()) dims.add("文种");
        if (keyword != null && !keyword.isEmpty()) dims.add("关键词");
        if (!dims.isEmpty()) reasons.add("匹配维度：" + String.join("、", dims));
        reasons.add(String.format("综合相似度%.0f%%，频率占比%.0f%%", simScore * 100, freqScore * 100));
        return String.join("；", reasons);
    }
}
