package hb.kg.system.bean.report;

import java.util.Map;

import lombok.Data;

/**
 * 问题的分布报告
 */
@Data
public class QaDistReport {
    private Map<String, Integer> timeMap; // 问题的时间分布
    private Map<String, Integer> visitMap; // 问题的访问数量分布
    private Map<String, Integer> typeMap; // 问题的类型分布
    private Map<String, Integer> darkTitleMap; // 问题的暗标题数量分布
}
