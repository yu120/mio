package cn.ms.mio;

import java.util.Scanner;

import cn.ms.mio.client.Client;
import cn.ms.mio.server.Server;

public class Test {
	// 测试主方法
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		// 运行服务器
		Server.main(args);;
		// 避免客户端先于服务器启动前执行代码
		Thread.sleep(100);
		// 运行客户端
		Client.main(args);
		System.out.println("请输入请求消息：");
		Scanner scanner = new Scanner(System.in);
		while (Client.sendMsg(scanner.nextLine()))
			;
	}
}