package cn.ms.mio.protocol;

import java.nio.ByteBuffer;

import cn.ms.mio.transport.support.MioSession;

public class MioProtocol implements Protocol<Message> {

	@Override
	public Message decode(ByteBuffer data, MioSession<Message> session) {
		Message message = new Message();
		byte[] bytes = new byte[data.remaining()];
		data.get(bytes);
		message.setData(bytes);
		data.clear();

		return message;
	}

	@Override
	public ByteBuffer encode(Message t, MioSession<Message> session) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(1 * 1024);
		byteBuffer.put(t.getData());
		
		return byteBuffer;
	}

}
