package com.gov.doc.engine;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DeptRecommendAlgorithmTest {

    private static JiebaSegmenter jieba;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好",
            "自己", "这", "他", "她", "们", "那", "与", "及", "等", "为", "中", "对", "从",
            "而", "但", "其", "之", "或", "将", "于", "由", "被", "把", "让", "向", "比",
            "以", "至", "所", "能", "可", "该", "此", "些", "个", "某", "每", "各",
            "关于", "加强", "做好", "开展", "推进", "进行", "进一步"
    ));

    @BeforeAll
    static void init() {
        jieba = new JiebaSegmenter();
    }

    @Test
    void testJiebaSegmentation() {
        String title = "关于加强财政预算管理工作的通知";
        Set<String> words = jiebaSegment(title);
        System.out.println("标题分词结果: " + title + " => " + words);

        assertTrue(words.contains("财政"), "应包含'财政'");
        assertTrue(words.contains("预算"), "应包含'预算'");
        assertTrue(words.contains("管理"), "应包含'管理'");
        assertTrue(words.contains("工作"), "应包含'工作'");
        assertTrue(words.contains("通知"), "应包含'通知'");
        assertFalse(words.contains("的"), "不应包含停用词'的'");
        assertFalse(words.contains("关于"), "不应包含停用词'关于'");
        assertFalse(words.contains("加强"), "不应包含停用词'加强'");
    }

    @Test
    void testKeywordSegmentation() {
        String keyword = "财政,预算,绩效";
        Set<String> words = splitAndSegment(keyword);
        System.out.println("关键词分词结果: " + keyword + " => " + words);

        assertTrue(words.contains("财政"), "应包含'财政'");
        assertTrue(words.contains("预算"), "应包含'预算'");
        assertTrue(words.contains("绩效"), "应包含'绩效'");
    }

    @Test
    void testSimilarityBetweenTitles() {
        String title1 = "关于加强财政预算绩效管理的通知";
        String title2 = "关于加强财政预算管理工作的通知";
        String title3 = "关于推进财政信息化建设的通知";
        String title4 = "关于深化预算管理制度改革的批复";

        Set<String> words1 = jiebaSegment(title1);
        Set<String> words2 = jiebaSegment(title2);
        Set<String> words3 = jiebaSegment(title3);
        Set<String> words4 = jiebaSegment(title4);

        double sim12 = jaccardSimilarity(words1, words2);
        double sim13 = jaccardSimilarity(words1, words3);
        double sim14 = jaccardSimilarity(words1, words4);

        System.out.println("分词结果:");
        System.out.println("  标题1: " + title1 + " => " + words1);
        System.out.println("  标题2: " + title2 + " => " + words2);
        System.out.println("  标题3: " + title3 + " => " + words3);
        System.out.println("  标题4: " + title4 + " => " + words4);
        System.out.println("相似度:");
        System.out.println("  1vs2 (高度相似): " + sim12);
        System.out.println("  1vs3 (中度相似): " + sim13);
        System.out.println("  1vs4 (低度相似): " + sim14);

        assertTrue(sim12 > sim13, "标题1与2的相似度应高于1与3");
        assertTrue(sim13 > sim14, "标题1与3的相似度应高于1与4");
        assertTrue(sim12 > 0.3, "高度相似标题的相似度应大于0.3");
    }

    @Test
    void testKeywordSimilarity() {
        Set<String> kw1 = splitAndSegment("财政,预算,绩效");
        Set<String> kw2 = splitAndSegment("财政,预算,管理");
        Set<String> kw3 = splitAndSegment("财政,信息化,建设");

        double sim12 = jaccardSimilarity(kw1, kw2);
        double sim13 = jaccardSimilarity(kw1, kw3);

        System.out.println("关键词分词:");
        System.out.println("  kw1: " + kw1);
        System.out.println("  kw2: " + kw2);
        System.out.println("  kw3: " + kw3);
        System.out.println("  1vs2: " + sim12);
        System.out.println("  1vs3: " + sim13);

        assertTrue(sim12 > sim13, "财政预算绩效 vs 财政预算管理 的相似度应高于 vs 财政信息化建设");
    }

    @Test
    void testFullRecommendationScenario() {
        String[] historicalTitles = {
                "关于加强财政预算管理工作的通知",
                "关于开展年度财政审计检查的通知",
                "关于加强财政资金监管的通知",
                "关于深化预算管理制度改革的批复",
                "关于开展财政专项审计的通知",
                "关于调整财政管理体制的函",
                "关于加强预算执行管理的通知",
                "关于做好财政决算工作的通知",
                "关于推进财政信息化建设的通知"
        };

        String[] historicalKeywords = {
                "财政,预算,管理",
                "财政,审计,检查",
                "财政,资金,监管",
                "预算,管理,改革",
                "财政,审计,专项",
                "财政,体制,调整",
                "预算,执行,管理",
                "财政,决算",
                "财政,信息化,建设"
        };

        String[] historicalDocTypes = {
                "通知", "通知", "通知", "批复", "通知", "函", "通知", "通知", "通知"
        };

        String[] historicalDepts = {
                "1002", "1002", "1002", "1003", "1002", "1003", "1002", "1002", "1005"
        };

        String[] deptNames = {
                "财政审计科", "财政审计科", "财政审计科", "政策法规科",
                "财政审计科", "政策法规科", "财政审计科", "财政审计科", "信息技术科"
        };

        String targetTitle = "关于加强财政预算绩效管理的通知";
        String targetDocType = "通知";
        String targetKeyword = "财政,预算,绩效";
        Set<String> targetTitleWords = jiebaSegment(targetTitle);
        Set<String> targetKwWords = splitAndSegment(targetKeyword);

        System.out.println("\n===== 完整推荐场景测试 =====");
        System.out.println("目标公文: " + targetTitle);
        System.out.println("目标文种: " + targetDocType);
        System.out.println("目标关键词: " + targetKeyword);
        System.out.println("目标标题分词: " + targetTitleWords);
        System.out.println("目标关键词分词: " + targetKwWords);

        Map<String, List<double[]>> deptScores = new HashMap<>();
        Map<String, String> deptNameMap = new HashMap<>();

        double TITLE_SIM_WEIGHT = 0.4;
        double KEYWORD_SIM_WEIGHT = 0.3;
        double DOC_TYPE_SCORE = 0.3;

        for (int i = 0; i < historicalTitles.length; i++) {
            if (!targetDocType.equals(historicalDocTypes[i])) {
                System.out.printf("  历史[%d] '%s' 文种=%s ≠ %s 跳过%n",
                        i + 1, historicalTitles[i], historicalDocTypes[i], targetDocType);
                continue;
            }

            Set<String> histTitleWords = jiebaSegment(historicalTitles[i]);
            Set<String> histKwWords = splitAndSegment(historicalKeywords[i]);

            double titleSim = jaccardSimilarity(targetTitleWords, histTitleWords);
            double kwSim = jaccardSimilarity(targetKwWords, histKwWords);
            double docTypeScore = 1.0;
            double weightSum = TITLE_SIM_WEIGHT + KEYWORD_SIM_WEIGHT + DOC_TYPE_SCORE;
            double totalSim = (titleSim * TITLE_SIM_WEIGHT + kwSim * KEYWORD_SIM_WEIGHT + docTypeScore * DOC_TYPE_SCORE) / weightSum;

            String deptId = historicalDepts[i];
            deptNameMap.put(deptId, deptNames[i]);
            deptScores.computeIfAbsent(deptId, k -> new ArrayList<>()).add(new double[]{totalSim, titleSim, kwSim});

            System.out.printf("  历史[%d] '%s' kw='%s' => 标题=%.3f 关键词=%.3f 文种=1.000 综合=%.3f => %s%n",
                    i + 1, historicalTitles[i], historicalKeywords[i],
                    titleSim, kwSim, totalSim, deptNames[i]);
        }

        Map<String, Double> deptAvgSim = new HashMap<>();
        for (Map.Entry<String, List<double[]>> entry : deptScores.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(arr -> arr[0]).average().orElse(0.0);
            deptAvgSim.put(entry.getKey(), avg);
        }

        int maxCount = deptScores.values().stream().mapToInt(List::size).max().orElse(1);

        System.out.println("\n推荐结果:");
        List<Map.Entry<String, List<double[]>>> sorted = deptScores.entrySet().stream()
                .sorted((a, b) -> {
                    double freqA = (double) a.getValue().size() / maxCount;
                    double freqB = (double) b.getValue().size() / maxCount;
                    double scoreA = freqA * 0.5 + deptAvgSim.get(a.getKey()) * 0.3 + 0.5 * 0.2;
                    double scoreB = freqB * 0.5 + deptAvgSim.get(b.getKey()) * 0.3 + 0.5 * 0.2;
                    return Double.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());

        for (int rank = 0; rank < Math.min(3, sorted.size()); rank++) {
            Map.Entry<String, List<double[]>> entry = sorted.get(rank);
            String deptId = entry.getKey();
            int count = entry.getValue().size();
            double freqScore = (double) count / maxCount;
            double simScore = deptAvgSim.get(deptId);
            double totalScore = freqScore * 0.5 + simScore * 0.3 + 0.5 * 0.2;

            System.out.printf("  #%d %s (id=%s): 办理%d次, 频率分=%.3f, 相似度分=%.3f, 综合分=%.3f%n",
                    rank + 1, deptNameMap.get(deptId), deptId, count, freqScore, simScore, totalScore);
        }

        String topDeptId = sorted.get(0).getKey();
        assertEquals("1002", topDeptId, "财政审计科(id=1002)应排在第一位");
        assertTrue(deptAvgSim.get("1002") > 0.3, "财政审计科的平均相似度应大于0.3");
    }

    private Set<String> jiebaSegment(String text) {
        Set<String> words = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) return words;
        List<SegToken> tokens = jieba.process(text, JiebaSegmenter.SegMode.SEARCH);
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
        if (keywordStr == null || keywordStr.isEmpty()) return words;
        String[] parts = keywordStr.split("[,，、;；\\s]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.length() <= 4) {
                if (trimmed.length() >= 2 && !STOP_WORDS.contains(trimmed)) {
                    words.add(trimmed);
                }
            } else {
                List<SegToken> tokens = jieba.process(trimmed, JiebaSegmenter.SegMode.SEARCH);
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
}
