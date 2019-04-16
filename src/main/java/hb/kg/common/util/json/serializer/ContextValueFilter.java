package hb.kg.common.util.json.serializer;

public interface ContextValueFilter extends SerializeFilter {
    Object process(BeanContext context,
                   Object object,
                   String name,
                   Object value);
}
