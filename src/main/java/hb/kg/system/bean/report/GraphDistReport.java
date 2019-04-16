package hb.kg.system.bean.report;

import java.util.Map;

import lombok.Data;

/**
 * 知识图谱分布报告
 */
@Data
public class GraphDistReport {
    private Map<String, Integer> natureMap; // 词性分布，对应node的nature
    private Map<String, Integer> nodeWeightMap; // 节点权重分布，对应node的weight
    private Map<String, Integer> linkWeightMap; // 边权重分布，对应link的weight
    private Map<String, Integer> linkTypeMap; // 边类型分布，对应link的type
}
