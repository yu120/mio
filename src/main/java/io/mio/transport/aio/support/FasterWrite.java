package io.mio.transport.aio.support;

import io.mio.transport.aio.buffer.VirtualBuffer;

/**
 * 快速write
 *
 * @author lry
 */
public class FasterWrite {

    /**
     * 申请数据输出信号量
     *
     * @return true:申请成功,false:申请失败
     */
    public boolean tryAcquire() {
        return false;
    }

    /**
     * 执行数据输出
     *
     * @param buffer 待输出数据
     */
    public void write(VirtualBuffer buffer) {
    }

}
