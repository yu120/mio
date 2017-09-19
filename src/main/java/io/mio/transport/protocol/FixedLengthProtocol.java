package io.mio.transport.protocol;

import io.mio.transport.MioSession;
import io.mio.transport.support.ByteBufferUtils;

import java.nio.ByteBuffer;

public class FixedLengthProtocol<T> implements Protocol<T> {

	@SuppressWarnings("unchecked")
	@Override
	public T decode(ByteBuffer data, MioSession<T> session) {
		System.out.println("ddddddddd");
		byte[] bytes = ByteBufferUtils.readBytes(data, data.remaining());
		return (T)bytes;
	}

	@Override
	public ByteBuffer encode(T t, MioSession<T> session) {
		System.out.println("eeeeee");
		ByteBuffer sendBuffer=ByteBuffer.wrap((byte[])t);
		return sendBuffer;
	}
	
}
