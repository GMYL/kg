package hb.kg.common.util.json.serializer;

public interface PropertyPreFilter extends SerializeFilter {
    boolean apply(JSONSerializer serializer,
                  Object object,
                  String name);
}
