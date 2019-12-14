package io.mio.aio;

import io.mio.aio.support.AioMioSession;

import java.nio.ByteBuffer;

public class DemoProtocol implements Protocol<String> {

    @Override
    public String decode(ByteBuffer readBuffer, AioMioSession<String> session) {
        int remaining = readBuffer.remaining();
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark();
        int length = readBuffer.getInt();
        if (length > readBuffer.remaining()) {
            readBuffer.reset();
            return null;
        }
        byte[] b = new byte[length];
        readBuffer.get(b);
        readBuffer.mark();
        return new String(b);
    }

}
