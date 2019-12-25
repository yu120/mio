package io.mio.commons.extension;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeReference
 *
 * @param <T>
 * @author lry
 */
public abstract class TypeReference<T> {

    private Class<T> type;

    @SuppressWarnings("unchecked")
    public TypeReference() {
        Type t = this.getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) t).getActualTypeArguments();
            this.type = (Class<T>) args[0];
        }
    }

    public Class<T> getType() {
        return type;
    }

}
