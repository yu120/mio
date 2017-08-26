package cn.ms.mio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.neural.NURL;

public class MioClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MioClient.class);
	
	private AsynchronousSocketChannel socketChannel;
    
    public void start(NURL nurl){
        try {
        	socketChannel = AsynchronousSocketChannel.open();
            if (socketChannel.isOpen()) {
                socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                socketChannel.connect(new InetSocketAddress(nurl.getHost(), nurl.getPort()), null, new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void attachment) {
                    	try {
							logger.info("Successfully connected at : " + socketChannel.getRemoteAddress());
							countDownLatch.countDown();
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                    @Override
                    public void failed(Throwable exc, Void attachment) {
                    	logger.error("Connection cannot be established!");
                        throw new UnsupportedOperationException("Connection cannot be established!");
                    }
                });
                countDownLatch.await();
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public byte[] send(byte[] data) {
    	byte[] resData = null;
    	ByteBuffer duplicate = null;
    	ByteBuffer buffer = ByteBuffer.allocateDirect(2*1024);//2MB的缓存空间
    	
        try {        
            socketChannel.write(ByteBuffer.wrap(data)).get();
            Future<Integer> future = socketChannel.read(buffer);
            Integer result =future.get();
            if(result!=-1&&future.isDone()){
            	buffer.flip();
                duplicate = buffer.duplicate();//不能直接使用buffer
                resData = new byte[duplicate.remaining()];
                duplicate.get(resData);
            }
        } catch (InterruptedException e) {
        	e.printStackTrace();
        } catch (ExecutionException e) {
        	e.printStackTrace();
        } catch (CancellationException e) {
        	e.printStackTrace();
        } finally {
        	if (duplicate!=null) {
            	duplicate.clear();
            }
        	if (duplicate!=null) {
                buffer.clear();
            }
        }
        
		return resData;
    }
    
}
