package io.mio.aio.buffer;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ByteBuffer内存池
 *
 * @author lry
 */
@Slf4j
public class BufferPagePool {

    /**
     * 守护线程在空闲时期回收内存资源
     */
    private static final ScheduledThreadPoolExecutor BUFFER_POOL_CLEAN =
            new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r, "BufferPoolClean");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * 内存页组
     */
    private BufferPage[] bufferPages;
    /**
     * 共享缓存页
     */
    private BufferPage sharedBufferPage;
    /**
     * 内存页游标
     */
    private AtomicInteger cursor = new AtomicInteger(0);
    /**
     * 线程游标
     */
    private AtomicInteger threadCursor = new AtomicInteger(0);
    private boolean enabled = true;
    /**
     * 内存回收任务
     */
    private ScheduledFuture future = BUFFER_POOL_CLEAN.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
            if (enabled) {
                log.debug("do buffer pool clean task");
                for (BufferPage bufferPage : bufferPages) {
                    bufferPage.tryClean();
                }
                if (sharedBufferPage != null) {
                    sharedBufferPage.tryClean();
                }
            } else {
                log.debug("release buffer pool");
                if (bufferPages != null) {
                    for (BufferPage page : bufferPages) {
                        page.release();
                    }
                    bufferPages = null;
                }
                if (sharedBufferPage != null) {
                    sharedBufferPage.release();
                    sharedBufferPage = null;
                }
                future.cancel(false);
            }
        }
    }, 500, 1000, TimeUnit.MILLISECONDS);

    /**
     * @param pageSize 内存页大小
     * @param pageNum  内存页个数
     * @param isDirect 是否使用直接缓冲区
     */
    public BufferPagePool(final int pageSize, final int pageNum, final boolean isDirect) {
        this(pageSize, pageNum, -1, isDirect);
    }

    /**
     * @param pageSize       内存页大小
     * @param pageNum        内存页个数
     * @param sharedPageSize 共享内存页大小
     * @param isDirect       是否使用直接缓冲区
     */
    public BufferPagePool(final int pageSize, final int pageNum, final int sharedPageSize, final boolean isDirect) {
        if (pageNum <= 0) {
            throw new IllegalArgumentException("pageNum must greater than 0");
        }
        if (sharedPageSize > 0) {
            sharedBufferPage = new BufferPage(null, null, sharedPageSize, isDirect);
        }
        bufferPages = new BufferPage[pageNum];
        for (int i = 0; i < pageNum; i++) {
            bufferPages[i] = new BufferPage(bufferPages, sharedBufferPage, pageSize, isDirect);
        }
    }

    /**
     * 申请FastBufferThread的线程对象,配合线程池申请会有更好的性能表现
     *
     * @param target Runnable
     * @param name   线程名
     * @return FastBufferThread线程对象
     */
    public Thread newThread(Runnable target, String name) {
        assertEnabled();
        return new FastBufferThread(target, name, threadCursor.getAndIncrement() % bufferPages.length);
    }

    /**
     * 申请内存页
     *
     * @return 缓存页对象
     */
    public BufferPage allocateBufferPage() {
        assertEnabled();
        //轮训游标，均衡分配内存页
        int index = cursor.getAndIncrement();
        if (index < 0) {
            cursor.set(0);
        }
        return bufferPages[index % bufferPages.length];
    }

    private void assertEnabled() {
        if (!enabled) {
            throw new IllegalStateException("buffer pool is disable");
        }
    }

    /**
     * 释放回收内存
     */
    public void release() {
        enabled = false;
    }

}