package hb.kg.common.util.json.parser.deserializer;

public interface ExtraProcessable {
    void processExtra(String key,
                      Object value);
}
