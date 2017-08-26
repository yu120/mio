package cn.ms.mio.integer;

import cn.ms.mio.transport.MioClient;

public class IntegerClient {
    public static void main(String[] args) throws Exception {
        IntegerClientProcessor processor=new IntegerClientProcessor();
        MioClient<Integer> aioQuickClient=new MioClient<Integer>()
                .connect("localhost",8888)
                .setProtocol(new IntegerProtocol())
                .setProcessor(processor);
        aioQuickClient.start();
        for (int i = 0; i < 10; i++) {
        	processor.getSession().write(i);
		}
        Thread.sleep(1000);
        aioQuickClient.shutdown();

    }
}
