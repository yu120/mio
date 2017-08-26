package cn.ms.mio.protocol;

import java.nio.ByteBuffer;

import cn.ms.mio.transport.support.MioSession;

/**
 * 消息传输采用的协议<br>
 * <br>
 * 框架本身的所有Socket链路复用同一个Protocol，请勿在其实现类的成员变量中存储特定链路的数据
 *
 * @author lry
 */
public interface Protocol<T> {

	/**
	 * 对于从Socket流中获取到的数据采用当前Protocol的实现类协议进行解析
	 *
	 * @param data
	 * @return 本次解码所成功解析的消息实例集合,返回null则表示解码未完成
	 */
	T decode(ByteBuffer data, MioSession<T> session);

	/**
	 * 将业务消息实体编码成ByteBuffer用于输出至对端<br>
	 * <b>切勿在encode中直接调用session.write,编码后的byteuffer需交由框架本身来输出</b>
	 * 
	 * @param t
	 * @param session
	 * @return
	 */
	ByteBuffer encode(T t, MioSession<T> session);

}
