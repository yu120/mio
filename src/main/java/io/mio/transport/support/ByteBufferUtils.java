package io.mio.transport.support;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;

public class ByteBufferUtils {
	
	public static ByteBuffer composite(ByteBuffer byteBuffer1, ByteBuffer byteBuffer2) {
		int capacity = byteBuffer1.limit() - byteBuffer1.position() + byteBuffer2.limit() - byteBuffer2.position();
		ByteBuffer ret = ByteBuffer.allocate(capacity);

		ret.put(byteBuffer1);
		ret.put(byteBuffer2);

		ret.position(0);
		ret.limit(ret.capacity());
		return ret;
	}

	public static void copy(ByteBuffer src, int srcStartindex, ByteBuffer dest, int destStartIndex, int length) {
		System.arraycopy(src.array(), srcStartindex, dest.array(), destStartIndex, length);
	}

	/**
	 * @param src
	 * @param startindex 从0开始
	 * @param endindex
	 * @return
	 */
	public static ByteBuffer copy(ByteBuffer src, int startindex, int endindex) {
		int size = endindex - startindex;
		byte[] dest = new byte[size];
		System.arraycopy(src.array(), startindex, dest, 0, dest.length);
		ByteBuffer newByteBuffer = ByteBuffer.wrap(dest);
		return newByteBuffer;
	}

	public static int lineEnd(ByteBuffer buffer) throws RuntimeException {
		return lineEnd(buffer, Integer.MAX_VALUE);
	}

	public static int lineEnd(ByteBuffer buffer, int maxlength) throws RuntimeException {
		boolean canEnd = false;
		//		int startPosition = buffer.position();
		int count = 0;
		while (buffer.hasRemaining()) {
			byte b = buffer.get();
			count++;
			if (count > maxlength) {
				throw new RuntimeException("maxlength is " + maxlength);
			}
			if (b == '\r') {
				canEnd = true;
			} else if (b == '\n') {
				if (canEnd) {
					int endPosition = buffer.position();
					return endPosition - 2;
				}
			}
		}
		return -1;
	}

	public static byte[] readBytes(ByteBuffer buffer, int length) {
		byte[] ab = new byte[length];
		buffer.get(ab);
		return ab;
	}

	public static String readLine(ByteBuffer buffer, String charset) throws RuntimeException {
		return readLine(buffer, charset, Integer.MAX_VALUE);
	}

	public static String readLine(ByteBuffer buffer, String charset, Integer maxlength) throws RuntimeException {
		//		boolean canEnd = false;
		int startPosition = buffer.position();
		int endPosition = lineEnd(buffer, maxlength);

		if (endPosition > startPosition) {
			byte[] bs = new byte[endPosition - startPosition];
			System.arraycopy(buffer.array(), startPosition, bs, 0, bs.length);
			if (StringUtils.isNoneBlank(charset)) {
				try {
					return new String(bs, charset);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				return new String(bs);
			}

		} else if (endPosition == -1) {
			return null;
		} else if (endPosition == startPosition) {
			return "";
		}
		return null;
	}

	public static int readUB1(ByteBuffer buffer) {
		int ret = buffer.get() & 0xff;
		return ret;
	}

	public static int readUB2(ByteBuffer buffer) {
		int ret = buffer.get() & 0xff;
		ret |= (buffer.get() & 0xff) << 8;
		return ret;
	}

	public static int readUB2WithBigEdian(ByteBuffer buffer) {
		int ret = (buffer.get() & 0xff) << 8;
		ret |= buffer.get() & 0xff;
		return ret;
	}

	public static long readUB4(ByteBuffer buffer) {
		long ret = buffer.get() & 0xff;
		ret |= (long) (buffer.get() & 0xff) << 8;
		ret |= (long) (buffer.get() & 0xff) << 16;
		ret |= (long) (buffer.get() & 0xff) << 24;
		return ret;
	}

	public static long readUB4WithBigEdian(ByteBuffer buffer) {
		long ret = (long) (buffer.get() & 0xff) << 24;
		ret |= (long) (buffer.get() & 0xff) << 16;
		ret |= (long) (buffer.get() & 0xff) << 8;
		ret |= buffer.get() & 0xff;

		return ret;
	}

	public static final void writeUB2(ByteBuffer buffer, int i) {
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
	}

	public static final void writeUB2WithBigEdian(ByteBuffer buffer, int i) {
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i & 0xff));
	}

	public static final void writeUB4(ByteBuffer buffer, long l) {
		buffer.put((byte) (l & 0xff));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 24));
	}

	public static final void writeUB4WithBigEdian(ByteBuffer buffer, long l) {
		buffer.put((byte) (l >>> 24));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l & 0xff));
	}
	
}
