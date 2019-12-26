package io.mio.rpc;


import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Invocation
 *
 * @author lry
 */
@Data
public class Invocation implements Serializable {

    private String className;
    private String methodName;
    private Object[] arguments;
    private Map<String, Object> attachments;

    private transient Class<?> classType;
    private transient Class<?> returnType;
    private transient Class<?>[] parameterTypes;

    public Object invoke() {
        return null;
    }

}
