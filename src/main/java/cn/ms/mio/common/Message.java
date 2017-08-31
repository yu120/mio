package cn.ms.mio.common;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Message {
	
	private String id = UUID.randomUUID().toString();
	
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private boolean keepAlive = true;

	private Object data;
	private Map<String, Object> headers = new HashMap<String, Object>();

	private Throwable cause;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}
	
	
}
