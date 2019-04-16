package hb.kg.common.util.json.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by SongLing.Dong on 7/22/2017.
 * @see com.alibaba.fastjson.support.spring.JSONPResponseBodyAdvice
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface ResponseJSONP {
    /**
     * The parameter's name of the callback method.
     */
    String callback() default "callback";
}
