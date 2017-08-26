package cn.ms.mio;

import java.io.IOException;

import cn.ms.mio.protocol.Message;
import cn.ms.mio.transport.support.IProcessor;
import cn.ms.mio.transport.support.MioSession;

public class DemoMioServer {
	
	public static void main(String[] args) {
		try {
			Mio.buildServer(new IProcessor<Message>() {
				@Override
				public void process(MioSession<Message> session, Message msg) throws Exception {
			        System.out.println("接受到客户端数据：" + new String(msg.getData()));
			        
			        Message message = new Message();
			        message.setData(msg.getData());
			        session.write(message);
				}
			}).bind(9999).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
