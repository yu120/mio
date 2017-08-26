package cn.ms.mio;

import cn.ms.mio.client.MioClient;
import cn.ms.neural.NURL;

public class MioClientTest {

	public static void main(String[] args) {
		MioClient mioClient = new MioClient();
		mioClient.start(NURL.valueOf("mio://localhost:7777/test"));
		for (int i = 0; i < 100; i++) {
			mioClient.send("测试报文".getBytes());
		}
	}

}
