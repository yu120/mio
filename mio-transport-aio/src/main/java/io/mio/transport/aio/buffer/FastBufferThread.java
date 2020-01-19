package io.mio.transport.aio.buffer;

/**
 * FastBufferThread
 *
 * @author lry
 */
public class FastBufferThread extends Thread {

    /**
     * 索引标识
     */
    private final int index;

    public FastBufferThread(Runnable target, String name, int index) {
        super(target, name + index);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
