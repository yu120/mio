package cn.ms.mio.server;

import cn.ms.mio.Message;

public interface Processor {

	Message doProcessor(Message message);

}
