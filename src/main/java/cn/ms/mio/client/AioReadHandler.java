package cn.ms.mio.client;

import java.io.IOException; 
import java.nio.ByteBuffer; 
import java.nio.channels.AsynchronousSocketChannel; 
import java.nio.channels.CompletionHandler; 
import java.nio.charset.CharacterCodingException; 
import java.nio.charset.Charset; 
import java.nio.charset.CharsetDecoder; 
 
public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> { 
    
	private AsynchronousSocketChannel socket; 
	private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	
    public AioReadHandler(AsynchronousSocketChannel socket) { 
        this.socket = socket; 
    } 

    @Override
    public void completed(Integer i, ByteBuffer attachment) { 
        if (i > 0) { 
        	attachment.flip(); 
            try { 
            	System.out.println("Client-Received Respone("+socket.getRemoteAddress().toString()+"):"+ decoder.decode(attachment));
                attachment.compact(); 
            } catch (CharacterCodingException e) { 
                e.printStackTrace(); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
            socket.read(attachment, attachment, this); 
        } else if (i == -1) { 
            try { 
            	System.out.println("Client->Server-Break:" + socket.getRemoteAddress().toString());
                attachment = null; 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
    } 
 
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) { 
        System.out.println(exc); 
    }
    
}