package hb.kg.common.util.json.serializer;

public interface LabelFilter extends SerializeFilter {
    boolean apply(String label);
}
