package hb.kg.common.util.json.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 一个放置到 {@link org.springframework.stereotype.Controller Controller}方法上的注解.
 * 设置返回对象针对某个类需要排除或者包括的字段
 * 例如：
 * <code>&#064;FastJsonView(exclude = {&#064;FastJsonFilter(clazz = JSON.class,props = {"data"})})</code>
 * </pre>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FastJsonView {
    FastJsonFilter[] include() default {};

    FastJsonFilter[] exclude() default {};
}
