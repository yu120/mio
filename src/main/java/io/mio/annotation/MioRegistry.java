package io.mio.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MioRegistry {

    String protocol();

    String host();

    int port();

}
