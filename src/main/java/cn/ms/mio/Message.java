package cn.ms.mio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Message {

	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;
	private byte[] data;

	public Message buildInetSocketAddress(SocketAddress localAddress,
			SocketAddress remoteAddress) {
		this.localAddress = (InetSocketAddress) localAddress;
		this.remoteAddress = (InetSocketAddress) remoteAddress;
		return this;
	}

	public Message buildData(byte[] data) {
		this.data = data;
		return this;
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
