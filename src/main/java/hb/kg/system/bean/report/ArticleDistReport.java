package hb.kg.system.bean.report;

import java.util.Map;

import lombok.Data;

/**
 * 法规分布报告
 */
@Data
public class ArticleDistReport {
    private Map<String, Integer> createMap; // 创建时间分布，对应createTime
    private Map<String, Integer> publishTime; // 发布时间分布，对应publishTime
    private Map<String, Integer> visitMap; // 访问数量分布，对应visitNum
    private Map<String, Integer> authorMap; // 作者分布，对应author
    private Map<String, Integer> stateMap; // 状态分布，对应state
    private Map<String, Integer> labelMap; // 标签分布，对应label数量
    // FIXME 今后标签要做到支持更细的文章标签状况的统计
}
