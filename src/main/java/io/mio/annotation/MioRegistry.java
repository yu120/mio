package io.mio.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MioRegistry {

    String name() default "";

    String group() default "mio";

    String version() default "1.0.0";

}
