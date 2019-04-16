package hb.kg.system.bean.report;

import java.util.Map;

import lombok.Data;

/**
 * 工作流分布报告
 */
@Data
public class WorkflowDistReport {
    private Map<String, Integer> stateMap; // 状态分布，对应statecurrent
    private Map<String, Integer> managerMap; // 问题负责组分布
}
