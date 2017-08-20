package cn.ms.mio.test;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Description:回调接口，顶层抽象，主要是设定两个泛型参数 </P>
 */
public interface Callback extends CompletionHandler<Integer, AsynchronousSocketChannel> {
	
	// 某种程度上说，AIO编程其实是attachment编程
	@Override
	void failed(Throwable exc, AsynchronousSocketChannel socketChannel);

	@Override
	void completed(Integer result, AsynchronousSocketChannel socketChannel);
	
}