package hb.kg.system.bean.report;

import java.util.Map;

import lombok.Data;

/**
 * 法规分布报告
 */
@Data
public class LawDistReport {
    private Map<String, Integer> timeMap; // 法规的时间分布，对应法规的createTime
    private Map<String, Integer> visitMap; // 法规的访问数量分布，对应visitNum
    private Map<String, Integer> validMap; // 法规有效性分布，对应valid
    private Map<String, Integer> typeMap; // 法规的类型分布，对应excelType
    private Map<String, Integer> attachMap; // 相关法规分布，对应attaches
    private Map<String, Integer> annexesMap; // 法规的附件分布，对应annexes
    private Map<String, Integer> contentTokMap; // 法规的高频词分布，对应contents
}
