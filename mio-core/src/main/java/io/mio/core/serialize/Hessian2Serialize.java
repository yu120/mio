package io.mio.core.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.mio.core.extension.Extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Hessian 2 Serialize.
 * <p>
 * The need to serialize the object to achieve java.io.Serializable interface.
 *
 * @author lry
 */
@Extension("hessian2")
public class Hessian2Serialize implements Serialize {

    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            for (Object obj : objects) {
                out.writeObject(obj);
            }
        } else {
            out.writeObject(object);
        }

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

}
