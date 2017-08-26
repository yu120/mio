package cn.ms.mio;

import cn.ms.mio.protocol.Message;
import cn.ms.mio.transport.MioClient;
import cn.ms.mio.transport.support.IProcessor;
import cn.ms.mio.transport.support.MioSession;

public class DemoMioClient {

	public static void main(String[] args) {
		try {
			MioClient<Message> mioClient = Mio.buildClient(new IProcessor<Message>() {
				@Override
				public void process(MioSession<Message> session, Message msg) throws Exception {
					 System.out.println("接受到服务端响应数据：" + new String(msg.getData()));
				}
			}).connect("localhost", 9999);
			mioClient.start();
			
			for (int i = 0; i < 10; i++) {
				Message msg = new Message();
				msg.setData(("测试报文"+i).getBytes());
				mioClient.getSession().write(msg);
			}
			
			Thread.sleep(3000);
	        mioClient.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
