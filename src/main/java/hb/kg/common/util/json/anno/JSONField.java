package hb.kg.common.util.json.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import hb.kg.common.util.json.parser.Feature;
import hb.kg.common.util.json.serializer.SerializerFeature;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface JSONField {
    /**
     * config encode/decode ordinal
     * @since 1.1.42
     * @return
     */
    int ordinal() default 0;

    String name() default "";

    String format() default "";

    boolean serialize() default true;

    boolean deserialize() default true;

    SerializerFeature[] serialzeFeatures() default {};

    Feature[] parseFeatures() default {};

    String label() default "";

    /**
     * @since 1.2.12
     */
    boolean jsonDirect() default false;

    /**
     * Serializer class to use for serializing associated value.
     * @since 1.2.16
     */
    Class<?> serializeUsing() default Void.class;

    /**
     * Deserializer class to use for deserializing associated value.
     * @since 1.2.16
     */
    Class<?> deserializeUsing() default Void.class;

    /**
     * @since 1.2.21
     * @return the alternative names of the field when it is deserialized
     */
    String[] alternateNames() default {};

    /**
     * @since 1.2.31
     */
    boolean unwrapped() default false;
}
