package io.mio.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

/**
 * The Hessian 2 Serialize.
 * <p>
 * The need to serialize the object to achieve java.io.Serializable interface.
 *
 * @author lry
 */
public class Hessian2Serialize implements Serialize {

    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        out.writeObject(object);
        out.flush();
        return bos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        Hessian2Input input = new Hessian2Input(inputStream);
        return (T) input.readObject(clz);
    }

    @Override
    public byte[] serializeMulti(Object[] objects) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        for (Object object : objects) {
            out.writeObject(object);
        }
        out.flush();
        return bos.toByteArray();
    }

    @Override
    public Object[] deserializeMulti(byte[] bytes, Class<?>[] classes) throws IOException {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        Object[] objects = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            objects[i] = input.readObject(classes[i]);
        }
        return objects;
    }

}
