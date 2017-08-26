package cn.ms.mio;

import java.io.IOException;

import cn.ms.mio.protocol.Message;
import cn.ms.mio.protocol.MioProtocol;
import cn.ms.mio.transport.MioClient;
import cn.ms.mio.transport.MioServer;
import cn.ms.mio.transport.support.IProcessor;

public class Mio {

	public static MioServer<Message> buildServer(IProcessor<Message> processor) {
		return new MioServer<Message>().setProtocol(new MioProtocol()).setProcessor(processor);
	}

	public static MioClient<Message> buildClient(IProcessor<Message> processor) throws IOException {
		return new MioClient<Message>().setProtocol(new MioProtocol()).setProcessor(processor);
	}
	
}
