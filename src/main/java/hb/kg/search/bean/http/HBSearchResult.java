package hb.kg.search.bean.http;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class HBSearchResult {
    private String query;
    private int totalSize;
    private int totalPage;
    private List<Map<String, Object>> searchResult;
}
