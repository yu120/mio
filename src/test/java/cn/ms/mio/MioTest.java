package cn.ms.mio;

import java.io.IOException;

import cn.ms.mio.protocol.Message;
import cn.ms.mio.transport.support.IProcessor;
import cn.ms.mio.transport.support.MioSession;

public class MioTest {

	public static void main(String[] args) {
		try {
			Mio.buildServer(new IProcessor<Message>() {
				@Override
				public void process(MioSession<Message> session, Message msg) throws Exception {
					String respMsg=new String(msg.getData());
			        System.out.println("接受到客户端数据：" + msg + " ,响应数据:" + (respMsg));
			        
			        Message message = new Message();
			        message.setData(respMsg.getBytes());
			        session.write(message);
				}
				
				@Override
				public void initSession(MioSession<Message> session) {
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
