package cn.ms.mio;

import cn.ms.mio.server.MioServer;
import cn.ms.mio.server.Processor;
import cn.ms.neural.NURL;

public class MioServerTest {

	public static void main(String[] args) {
		MioServer mioServer = new MioServer();
		NURL url = NURL.valueOf("mio://localhost:7777/test");
		mioServer.start(url, new Processor() {
			
			@Override
			public Message doProcessor(Message message) {
				System.out.println("服务端收到报文："+new String(message.getData()));
				return new Message().buildData("响应报文".getBytes());
			}
		});
	}
	
}
