package hb.kg.common.util.json.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import hb.kg.common.util.json.PropertyNamingStrategy;
import hb.kg.common.util.json.parser.Feature;
import hb.kg.common.util.json.serializer.SerializeFilter;
import hb.kg.common.util.json.serializer.SerializerFeature;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface JSONType {
    boolean asm() default true;

    String[] orders() default {};

    String[] includes() default {};

    String[] ignores() default {};

    SerializerFeature[] serialzeFeatures() default {};

    Feature[] parseFeatures() default {};

    boolean alphabetic() default true;

    Class<?> mappingTo() default Void.class;

    Class<?> builder() default Void.class;

    String typeName() default "";

    /**
     * @since 1.2.32
     */
    String typeKey() default "";

    /**
     * @since 1.2.11
     */
    Class<?>[] seeAlso() default {};

    /**
     * @since 1.2.14
     */
    Class<?> serializer() default Void.class;

    /**
     * @since 1.2.14
     */
    Class<?> deserializer() default Void.class;

    boolean serializeEnumAsJavaBean() default false;

    PropertyNamingStrategy naming() default PropertyNamingStrategy.CamelCase;

    /**
     * @since 1.2.49
     */
    Class<? extends SerializeFilter>[] serialzeFilters() default {};
}
