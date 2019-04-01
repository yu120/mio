package io.mio.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.mio.commons.extension.Extension;

import java.io.IOException;
import java.util.List;

/**
 * fastjson 序列化
 * <p>
 * 注:对于嵌套场景无法支持
 *
 * @author lry
 */
@Extension("fastjson")
public class FastJsonSerialize implements ISerialize {

    @Override
    public byte[] serialize(Object data) throws IOException {
        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.config(SerializerFeature.WriteClassName, true);
        serializer.write(data);
        return out.toBytes("UTF-8");
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        return JSON.parseObject(new String(data), clz);
    }

    @Override
    public byte[] serializeMulti(Object[] data) throws IOException {
        return serialize(data);
    }

    @Override
    public Object[] deserializeMulti(byte[] data, Class<?>[] classes) throws IOException {
        List<Object> list = JSON.parseArray(new String(data), classes);
        if (list != null) {
            return list.toArray();
        }
        return null;
    }

}
