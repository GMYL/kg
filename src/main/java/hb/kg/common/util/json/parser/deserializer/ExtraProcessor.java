package hb.kg.common.util.json.parser.deserializer;

public interface ExtraProcessor extends ParseProcess {
    void processExtra(Object object,
                      String key,
                      Object value);
}
