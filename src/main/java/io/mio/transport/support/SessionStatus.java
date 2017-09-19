package io.mio.transport.support;

/**
 * 会话状态
 * 
 * @author lry
 */
public class SessionStatus {

	/**
	 * Session状态:已关闭
	 */
	public static final byte SESSION_STATUS_CLOSED = 1;

	/**
	 * Session状态:关闭中
	 */
	public static final byte SESSION_STATUS_CLOSING = 2;
	
	/**
	 * Session状态:正常
	 */
	public static final byte SESSION_STATUS_ENABLED = 3;

}
