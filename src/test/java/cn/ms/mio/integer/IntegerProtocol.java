package cn.ms.mio.integer;

import java.nio.ByteBuffer;

import cn.ms.mio.protocol.Protocol;
import cn.ms.mio.transport.support.MioSession;

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
