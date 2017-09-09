package cn.ms.mio.transport.protocol;

import java.nio.ByteBuffer;

import cn.ms.mio.transport.MioSession;
import cn.ms.mio.transport.support.ByteBufferUtils;

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
