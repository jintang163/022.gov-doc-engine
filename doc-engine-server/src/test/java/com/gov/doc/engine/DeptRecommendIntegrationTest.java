package com.gov.doc.engine;

import com.gov.doc.engine.dto.DeptRecommendDTO;
import com.gov.doc.engine.service.DeptRecommendService;
import com.gov.doc.engine.vo.DeptRecommendVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeptRecommendIntegrationTest {

    @Autowired
    private DeptRecommendService deptRecommendService;

    @Test
    void testRecommendWithFullParams() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle("关于加强财政预算绩效管理的通知");
        dto.setDocType("通知");
        dto.setKeyword("财政,预算,绩效");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 完整参数推荐结果 =====");
        System.out.println("输入：标题='" + dto.getDocTitle() + "', 文种='" + dto.getDocType() + "', 关键词='" + dto.getKeyword() + "'");
        System.out.println("推荐数量：" + result.size());

        assertFalse(result.isEmpty(), "应该返回至少1个推荐结果");
        assertTrue(result.size() <= 3, "最多返回3个推荐");

        for (int i = 0; i < result.size(); i++) {
            DeptRecommendVO vo = result.get(i);
            System.out.printf("  #%d %s (id=%s): 匹配度=%.2f (%s), 历史%d次, 最近=%s%n",
                    i + 1, vo.getDeptName(), vo.getDeptId(),
                    vo.getMatchScore(), vo.getMatchScoreText(),
                    vo.getMatchCount(), vo.getLastHandleTime());
            System.out.println("      原因：" + vo.getMatchReason());
        }

        DeptRecommendVO top = result.get(0);
        assertNotNull(top.getDeptId(), "部门ID不应为空");
        assertNotNull(top.getDeptName(), "部门名称不应为空");
        assertTrue(top.getMatchScore() > 0.0, "匹配度应大于0");
        assertTrue(top.getMatchCount() > 0, "历史办理次数应大于0");
        assertNotNull(top.getMatchReason(), "匹配原因不应为空");
    }

    @Test
    void testRecommendWithOnlyDocType() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocType("通知");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 仅文种推荐结果 =====");
        System.out.println("输入：文种='" + dto.getDocType() + "'");
        System.out.println("推荐数量：" + result.size());

        assertTrue(result.size() <= 3, "最多返回3个推荐");

        if (!result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                DeptRecommendVO vo = result.get(i);
                System.out.printf("  #%d %s: 匹配度=%.2f (%s), 历史%d次%n",
                        i + 1, vo.getDeptName(), vo.getMatchScore(),
                        vo.getMatchScoreText(), vo.getMatchCount());
            }
        }
    }

    @Test
    void testRecommendWithOnlyTitle() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle("关于加强财政预算管理工作的通知");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 仅标题推荐结果 =====");
        System.out.println("输入：标题='" + dto.getDocTitle() + "'");
        System.out.println("推荐数量：" + result.size());

        if (!result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                DeptRecommendVO vo = result.get(i);
                System.out.printf("  #%d %s: 匹配度=%.2f (%s), 历史%d次%n",
                        i + 1, vo.getDeptName(), vo.getMatchScore(),
                        vo.getMatchScoreText(), vo.getMatchCount());
            }
        }
    }

    @Test
    void testRecommendWithDifferentDocType() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle("关于深化预算管理制度改革的通知");
        dto.setDocType("批复");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 指定文种=批复 推荐结果 =====");
        System.out.println("输入：标题='" + dto.getDocTitle() + "', 文种='" + dto.getDocType() + "'");
        System.out.println("推荐数量：" + result.size());

        if (!result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                DeptRecommendVO vo = result.get(i);
                System.out.printf("  #%d %s: 匹配度=%.2f (%s), 历史%d次%n",
                        i + 1, vo.getDeptName(), vo.getMatchScore(),
                        vo.getMatchScoreText(), vo.getMatchCount());
            }
            if (result.get(0).getDeptName() != null && result.get(0).getDeptName().contains("政策法规")) {
                System.out.println("  => 批复文种推荐了政策法规科，符合历史数据");
            }
        }
    }

    @Test
    void testRecommendWithEmptyParams() {
        DeptRecommendDTO dto = new DeptRecommendDTO();

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 空参数推荐结果 =====");
        System.out.println("推荐数量：" + result.size());

        assertTrue(result.isEmpty(), "空参数应该返回空结果");
    }

    @Test
    void testRecommendScoreOrdering() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle("关于加强财政预算绩效管理的通知");
        dto.setDocType("通知");
        dto.setKeyword("财政,预算,绩效");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        if (result.size() >= 2) {
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).getMatchScore() >= result.get(i + 1).getMatchScore(),
                        "推荐结果应按匹配度降序排列");
            }
        }
    }

    @Test
    void testRecommendCanFillForm() {
        DeptRecommendDTO dto = new DeptRecommendDTO();
        dto.setDocTitle("关于加强财政预算绩效管理的通知");
        dto.setDocType("通知");
        dto.setKeyword("财政,预算,绩效");

        List<DeptRecommendVO> result = deptRecommendService.recommendDepts(dto);

        System.out.println("\n===== 表单填充验证 =====");

        assertFalse(result.isEmpty(), "应有推荐结果可填入表单");

        DeptRecommendVO top = result.get(0);
        assertNotNull(top.getDeptId(), "部门ID应可填入targetDeptId");
        assertNotNull(top.getDeptName(), "部门名称应可填入targetDeptName");

        System.out.printf("  可填入 targetDeptId = '%s'%n", top.getDeptId());
        System.out.printf("  可填入 targetDeptName = '%s'%n", top.getDeptName());
        System.out.printf("  匹配度 = %.2f (%s)%n", top.getMatchScore(), top.getMatchScoreText());

        assertTrue(top.getDeptId().matches("\\d+"), "部门ID应为数字字符串");
        assertFalse(top.getDeptName().trim().isEmpty(), "部门名称不应为空");
    }
}
