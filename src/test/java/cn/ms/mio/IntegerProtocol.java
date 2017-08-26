package cn.ms.mio;

import cn.ms.mio.protocol.Protocol;
import cn.ms.mio.transport.support.MioSession;

import java.nio.ByteBuffer;

/**
 * Created by seer on 2018/08/23.
 */
public class IntegerProtocol implements Protocol<Integer> {

    private static final int INT_LENGTH = 4;

    @Override
    public Integer decode(ByteBuffer data, MioSession<Integer> session) {
        if (data.remaining() < INT_LENGTH)
            return null;
        return data.getInt();
    }

    @Override
    public ByteBuffer encode(Integer s, MioSession<Integer> session) {
        ByteBuffer b = ByteBuffer.allocate(INT_LENGTH);
        b.putInt(s);
        return b;
    }
}
