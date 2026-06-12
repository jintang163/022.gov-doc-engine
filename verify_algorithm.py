# -*- coding: utf-8 -*-
"""
智能分办推荐算法 - 独立验证脚本
模拟Java版DeptRecommendStandaloneTest，不依赖jieba，手动指定分词结果来验证算法逻辑
"""
from datetime import datetime, timedelta
from collections import defaultdict

# ============ 常量配置 ============
STOP_WORDS = set([
    '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
    '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
    '自己', '这', '他', '她', '们', '那', '与', '及', '等', '为', '中', '对', '从',
    '而', '但', '其', '之', '或', '将', '于', '由', '被', '把', '让', '向', '比',
    '以', '至', '所', '能', '可', '该', '此', '些', '个', '某', '每', '各',
    '关于', '加强', '做好', '开展', '推进', '进行', '进一步', '工作', '通知',
    '有关', '事项', '问题', '意见', '方案', '办法', '规定', '制度'
])

TITLE_SIM_WEIGHT = 0.4
KEYWORD_SIM_WEIGHT = 0.3
DOC_TYPE_SCORE_WEIGHT = 0.3
FREQ_FINAL_WEIGHT = 0.5
SIM_FINAL_WEIGHT = 0.3
RECENCY_FINAL_WEIGHT = 0.2
TIME_DECAY_DAYS = 90
MIN_SIM_THRESHOLD = 0.05
MAX_RECOMMEND = 3

# ============ 模拟Jieba分词（手动对应Java JIEBA.process的SEARCH模式结果） ============
# 这里我们不依赖jieba库，直接用一个预定义的分词字典来模拟
SEG_DICT = {
    '关于加强财政预算绩效管理的通知': ['财政', '预算', '绩效', '管理'],
    '关于加强财政预算管理工作的通知': ['财政', '预算', '管理'],
    '关于开展年度财政审计检查的通知': ['开展', '年度', '财政', '审计', '检查'],
    '关于加强财政资金监管的通知': ['财政', '资金', '监管'],
    '关于深化预算管理制度改革的批复': ['预算', '管理', '制度', '改革'],
    '关于开展财政专项审计的通知': ['开展', '财政', '专项', '审计'],
    '关于调整财政管理体制的函': ['调整', '财政', '管理', '体制'],
    '关于加强预算执行管理的通知': ['预算', '执行', '管理'],
    '关于做好财政决算工作的通知': ['财政', '决算'],
    '关于开展经济责任审计的请示': ['开展', '经济', '责任', '审计'],
    '关于推进财政信息化建设的通知': ['推进', '财政', '信息化', '建设'],
    '关于召开2024年度财政预算编制工作会议的通知': ['召开', '年度', '财政', '预算', '编制', '会议'],
}


def jieba_segment(text):
    """模拟Java版jiebaSegment，返回分词集合（长度>=2且非停用词）"""
    if not text:
        return set()
    if text in SEG_DICT:
        words = [w for w in SEG_DICT[text] if len(w) >= 2 and w not in STOP_WORDS]
        return set(words)
    # 兜底：bigram切分
    words = set()
    for i in range(len(text) - 1):
        bigram = text[i:i + 2]
        if len(bigram) >= 2 and bigram not in STOP_WORDS:
            words.add(bigram)
    return words


def split_and_segment(keyword_str):
    """模拟Java版splitAndSegment，先按分隔符切分，再分词"""
    if not keyword_str:
        return set()
    import re
    parts = re.split(r'[,，、;；\s]+', keyword_str)
    words = set()
    for part in parts:
        part = part.strip()
        if not part:
            continue
        if len(part) <= 4:
            if len(part) >= 2 and part not in STOP_WORDS:
                words.add(part)
        else:
            words.update(jieba_segment(part))
    return words


def jaccard_similarity(s1, s2):
    """Jaccard相似度 = 交集大小 / 并集大小"""
    if not s1 or not s2:
        return 0.0
    inter = s1 & s2
    if not inter:
        return 0.0
    union = s1 | s2
    return len(inter) / len(union)


def calc_recency(last_time, now):
    """时间衰减分：90天线性衰减，当天=1.0，>=90天=0.1"""
    if not last_time:
        return 0.3
    days = (now - last_time).days
    if days <= 0:
        return 1.0
    if days >= TIME_DECAY_DAYS:
        return 0.1
    decay = 1.0 - days / TIME_DECAY_DAYS
    return max(decay * 0.9 + 0.1, 0.1)


def format_score(s):
    if s >= 0.8:
        return '高度匹配'
    if s >= 0.6:
        return '较高匹配'
    if s >= 0.4:
        return '中度匹配'
    return '一般匹配'


def build_reason(doc_type, title, keyword, count, last_time, now, sim_score, freq_score):
    reasons = [f'历史办理 {count} 次同类公文']
    if last_time:
        days = (now - last_time).days
        if days == 0:
            reasons.append('最近办理：今天')
        elif days < 30:
            reasons.append(f'最近办理：{days} 天前')
        elif days < 365:
            reasons.append(f'最近办理：{days // 30} 个月前')
        else:
            reasons.append('最近办理：1 年多前')
    dims = []
    if title:
        dims.append('标题')
    if doc_type:
        dims.append('文种')
    if keyword:
        dims.append('关键词')
    if dims:
        reasons.append(f'匹配维度：{"、".join(dims)}')
    reasons.append(f'综合相似度{sim_score * 100:.0f}%，频率占比{freq_score * 100:.0f}%')
    return '；'.join(reasons)


# ============ 历史数据构建 ============
class HistoryDoc:
    def __init__(self, title, doc_type, keyword, dept_id, dept_name, handle_time):
        self.title = title
        self.doc_type = doc_type
        self.keyword = keyword
        self.dept_id = dept_id
        self.dept_name = dept_name
        self.handle_time = handle_time


def build_history_data():
    """模拟DatabaseInitializer中的测试数据 + StandaloneTest数据"""
    base = datetime(2025, 1, 10, 10, 0)
    return [
        HistoryDoc('关于加强财政预算管理工作的通知', '通知', '财政,预算,管理', '1002', '财政审计科', base),
        HistoryDoc('关于开展年度财政审计检查的通知', '通知', '财政,审计,检查', '1002', '财政审计科', base + timedelta(days=5)),
        HistoryDoc('关于加强财政资金监管的通知', '通知', '财政,资金,监管', '1002', '财政审计科', base + timedelta(days=20)),
        HistoryDoc('关于加强财政资金监管的通知', '通知', '财政,资金,监管', '1003', '政策法规科', base + timedelta(days=20)),
        HistoryDoc('关于深化预算管理制度改革的批复', '批复', '预算,管理,改革', '1003', '政策法规科', base + timedelta(days=45)),
        HistoryDoc('关于开展财政专项审计的通知', '通知', '财政,审计,专项', '1002', '财政审计科', base + timedelta(days=60)),
        HistoryDoc('关于调整财政管理体制的函', '函', '财政,体制,调整', '1003', '政策法规科', base + timedelta(days=80)),
        HistoryDoc('关于加强预算执行管理的通知', '通知', '预算,执行,管理', '1002', '财政审计科', base + timedelta(days=95)),
        HistoryDoc('关于做好财政决算工作的通知', '通知', '财政,决算', '1002', '财政审计科', base + timedelta(days=110)),
        HistoryDoc('关于开展经济责任审计的请示', '请示', '经济责任,审计', '1002', '财政审计科', base + timedelta(days=125)),
        HistoryDoc('关于推进财政信息化建设的通知', '通知', '财政,信息化,建设', '1005', '信息技术科', base + timedelta(days=140)),
    ]


# ============ 核心推荐算法 ============
def recommend(target_title, target_doc_type, target_keyword, history):
    has_doc_type = bool(target_doc_type)
    has_title = bool(target_title)
    has_keyword = bool(target_keyword)

    if not has_doc_type and not has_title and not has_keyword:
        return []

    target_title_words = jieba_segment(target_title) if has_title else set()
    target_kw_words = split_and_segment(target_keyword) if has_keyword else set()

    # 第一步：先按文种过滤
    filtered = history
    if has_doc_type:
        filtered = [h for h in history if h.doc_type == target_doc_type]
        print(f"  先按文种'{target_doc_type}'过滤: {len(history)} → {len(filtered)} 条")

    dept_sim_scores = defaultdict(list)
    dept_name_map = {}
    dept_last_time_map = {}
    dept_count_map = defaultdict(int)

    for h in filtered:
        title_sim = 0.0
        if has_title and h.title:
            hist_title_words = jieba_segment(h.title)
            title_sim = jaccard_similarity(target_title_words, hist_title_words)

        kw_sim = 0.0
        if has_keyword and h.keyword:
            hist_kw_words = split_and_segment(h.keyword)
            kw_sim = jaccard_similarity(target_kw_words, hist_kw_words)

        doc_type_score = 1.0 if (has_doc_type and target_doc_type == h.doc_type) else 0.0

        sim_weight_sum = 0.0
        weighted_sim = 0.0
        if has_title:
            weighted_sim += title_sim * TITLE_SIM_WEIGHT
            sim_weight_sum += TITLE_SIM_WEIGHT
        if has_keyword:
            weighted_sim += kw_sim * KEYWORD_SIM_WEIGHT
            sim_weight_sum += KEYWORD_SIM_WEIGHT
        if has_doc_type:
            weighted_sim += doc_type_score * DOC_TYPE_SCORE_WEIGHT
            sim_weight_sum += DOC_TYPE_SCORE_WEIGHT
        total_sim = weighted_sim / sim_weight_sum if sim_weight_sum > 0 else 0.0

        if has_title and has_keyword and total_sim < MIN_SIM_THRESHOLD:
            continue

        dept_name_map[h.dept_id] = h.dept_name
        dept_sim_scores[h.dept_id].append((total_sim, title_sim, kw_sim))
        dept_count_map[h.dept_id] += 1

        existing = dept_last_time_map.get(h.dept_id)
        if existing is None or h.handle_time > existing:
            dept_last_time_map[h.dept_id] = h.handle_time

    if not dept_sim_scores:
        return []

    dept_avg_sim = {}
    for dept_id, scores in dept_sim_scores.items():
        dept_avg_sim[dept_id] = sum(s[0] for s in scores) / len(scores)

    max_count = max(dept_count_map.values(), default=1)
    now = datetime.now()

    recommend_list = []
    for dept_id in dept_sim_scores:
        count = dept_count_map[dept_id]
        freq_score = count / max_count
        sim_score = dept_avg_sim[dept_id]
        last_time = dept_last_time_map.get(dept_id)
        recency_score = calc_recency(last_time, now)

        match_score = (freq_score * FREQ_FINAL_WEIGHT
                       + sim_score * SIM_FINAL_WEIGHT
                       + recency_score * RECENCY_FINAL_WEIGHT)
        match_score = round(match_score, 2)

        reason = build_reason(
            target_doc_type, target_title, target_keyword,
            count, last_time, now, sim_score, freq_score
        )

        recommend_list.append({
            'deptId': dept_id,
            'deptName': dept_name_map[dept_id],
            'matchScore': match_score,
            'matchScoreText': format_score(match_score),
            'matchCount': count,
            'lastHandleTime': last_time.strftime('%Y-%m-%d %H:%M') if last_time else None,
            'matchReason': reason
        })

    recommend_list.sort(key=lambda x: x['matchScore'], reverse=True)
    return recommend_list[:MAX_RECOMMEND]


# ============ 测试 ============
def test_jieba_segmentation():
    print("【测试1】中文分词验证")
    title = '关于加强财政预算绩效管理的通知'
    words = jieba_segment(title)
    print(f"  标题: {title}")
    print(f"  分词结果: {sorted(words)}")
    passed = ('财政' in words and '预算' in words and '绩效' in words
              and '关于' not in words and '的' not in words and '加强' not in words)
    print(f"  结果: {'[PASS]' if passed else '[FAIL]'}")

    keyword = '财政,预算,绩效'
    kw_words = split_and_segment(keyword)
    print(f"  关键词: {keyword}")
    print(f"  分词结果: {sorted(kw_words)}")
    print()


def test_similarity():
    print("【测试2】Jaccard相似度验证")
    t1 = '关于加强财政预算绩效管理的通知'
    t2 = '关于加强财政预算管理工作的通知'
    t3 = '关于推进财政信息化建设的通知'
    t4 = '关于深化预算管理制度改革的批复'

    w1 = jieba_segment(t1)
    w2 = jieba_segment(t2)
    w3 = jieba_segment(t3)
    w4 = jieba_segment(t4)

    s12 = jaccard_similarity(w1, w2)
    s13 = jaccard_similarity(w1, w3)
    s14 = jaccard_similarity(w1, w4)

    print(f"  '{t1}' vs '{t2}' = {s12:.3f}")
    print(f"  '{t1}' vs '{t3}' = {s13:.3f}")
    print(f"  '{t1}' vs '{t4}' = {s14:.3f}")

    passed = s12 > s13 > s14 and s12 > 0.3
    print(f"  结果: {'[PASS] (相似度排序正确)' if passed else '[FAIL]'}")
    print()


def test_full_recommendation():
    print("【测试3】完整推荐场景（基于历史测试数据）")
    history = build_history_data()
    target_title = '关于加强财政预算绩效管理的通知'
    target_doc_type = '通知'
    target_keyword = '财政,预算,绩效'

    print(f"  目标公文: {target_title}")
    print(f"  目标文种: {target_doc_type}")
    print(f"  目标关键词: {target_keyword}")
    print(f"  历史数据: {len(history)} 条")

    results = recommend(target_title, target_doc_type, target_keyword, history)

    print("\n  --- 推荐结果 ---")
    for i, r in enumerate(results):
        print(f"  #{i + 1} 部门: {r['deptName']} (id={r['deptId']}) | 匹配度: {r['matchScore']:.2f} ({r['matchScoreText']}) | 历史{r['matchCount']}次 | 最近: {r['lastHandleTime']}")
        print(f"      原因: {r['matchReason']}")

    passed = (len(results) > 0 and len(results) <= 3
              and results[0]['deptId'] == '1002'
              and results[0]['deptName'] == '财政审计科')
    print(f"\n  结果: {'[PASS] (财政审计科排名第一，符合预期)' if passed else '[FAIL]'}")

    if len(results) >= 2:
        ordered = results[0]['matchScore'] >= results[1]['matchScore']
        if len(results) >= 3:
            ordered = ordered and results[1]['matchScore'] >= results[2]['matchScore']
        print(f"  排序验证: {'[PASS] (按匹配度降序)' if ordered else '[FAIL]'}")
    print()


def test_form_fill():
    print("【测试4】表单填充验证")
    history = build_history_data()
    results = recommend('关于加强财政预算绩效管理的通知', '通知', '财政,预算,绩效', history)

    print("  模拟前端「一键采纳」操作...")
    if results:
        top = results[0]
        form_dept_id = top['deptId']
        form_dept_name = top['deptName']

        print(f"  填入 targetDeptId = '{form_dept_id}'")
        print(f"  填入 targetDeptName = '{form_dept_name}'")
        print(f"  匹配度 = {top['matchScore']:.2f} ({top['matchScoreText']})")

        passed = (form_dept_id is not None and form_dept_id.isdigit()
                  and form_dept_name is not None and form_dept_name.strip() != '')
        print(f"  结果: {'[PASS] (表单字段可正确填充)' if passed else '[FAIL]'}")
    else:
        print("  结果: [FAIL] (无推荐结果)")
    print()


def main():
    print("=" * 50)
    print("  智能分办推荐算法 - 独立验证测试 (Python版)")
    print("=" * 50)
    print()

    test_jieba_segmentation()
    test_similarity()
    test_full_recommendation()
    test_form_fill()

    print("=" * 50)
    print("  全部测试完成")
    print("=" * 50)


if __name__ == '__main__':
    main()
