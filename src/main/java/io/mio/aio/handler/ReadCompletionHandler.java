package io.mio.aio.handler;

import io.mio.aio.EventState;
import io.mio.aio.NetFilter;
import io.mio.aio.support.TcpAioSession;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 读写事件回调处理类
 *
 * @param <T>
 * @author lry
 */
@Slf4j
public class ReadCompletionHandler<T> implements CompletionHandler<Integer, TcpAioSession<T>>, Runnable {

    private static final int LIFE_CYCLE = 1 << 8;

    /**
     * 读回调资源信号量
     */
    private Semaphore semaphore;

    /**
     * 读会话缓存队列
     */
    private ConcurrentLinkedQueue<TcpAioSession<T>> cacheAioSessionQueue;

    /**
     * 应该可以不用volatile
     */
    private boolean needNotify = true;
    /**
     * 同步锁
     */
    private ReentrantLock lock = new ReentrantLock();
    /**
     * 非空条件
     */
    private final Condition notEmpty = lock.newCondition();

    private boolean running = true;

    private LongAdder longAdder;

    public ReadCompletionHandler() {
    }

    public ReadCompletionHandler(final Semaphore semaphore) {
        this.semaphore = semaphore;
        this.cacheAioSessionQueue = new ConcurrentLinkedQueue<>();
        longAdder = new LongAdder();
    }


    @Override
    public void completed(final Integer result, final TcpAioSession<T> aioSession) {
        if (semaphore == null) {
            completed0(result, aioSession);
            return;
        }
        if (semaphore.tryAcquire()) {
            aioSession.setReadSemaphore(semaphore);
            completed0(result, aioSession);
            runRingBufferTask();
            return;
        }
        //线程资源不足,暂时积压任务
        aioSession.setLastReadSize(result);
        cacheAioSessionQueue.offer(aioSession);
        if (needNotify && lock.tryLock()) {
            try {
                needNotify = false;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 执行异步队列中的任务
     */
    private void runRingBufferTask() {
        if (cacheAioSessionQueue.isEmpty() || !semaphore.tryAcquire()) {
            return;
        }
        TcpAioSession<T> curSession = cacheAioSessionQueue.poll();
        if (curSession == null) {
            semaphore.release();
            return;
        }
        TcpAioSession<T> nextSession;
        longAdder.increment();
        long step = longAdder.sum();
        int i = LIFE_CYCLE;
        while (curSession != null) {
            nextSession = (i >> 1 > 0 || step == longAdder.sum())
                    ? cacheAioSessionQueue.poll() : null;
            if (nextSession == null) {
                curSession.setReadSemaphore(semaphore);
            }
            completed0(curSession.getLastReadSize(), curSession);
            curSession = nextSession;
        }
    }

    /**
     * 处理消息读回调事件
     *
     * @param result     已读消息字节数
     * @param aioSession 当前触发读回调的会话
     */
    private void completed0(final Integer result, final TcpAioSession<T> aioSession) {
        try {
            // 接收到的消息进行预处理
            NetFilter<T> monitor = aioSession.getServerConfig().getMonitor();
            if (monitor != null) {
                monitor.afterRead(aioSession, result);
            }
            aioSession.readFromChannel(result == -1);
        } catch (Exception e) {
            failed(e, aioSession);
        }
    }

    @Override
    public void failed(Throwable exc, TcpAioSession<T> aioSession) {
        try {
            aioSession.getServerConfig().getProcessor().stateEvent(aioSession, EventState.INPUT_EXCEPTION, exc);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        try {
            aioSession.close(false);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    /**
     * 停止内部线程
     */
    public void shutdown() {
        running = false;
        lock.lock();
        try {
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * watcher线程,当存在待处理的读回调事件时，或许可以激活空闲状态的IO线程组
     */
    @Override
    public void run() {
        while (running) {
            try {
                TcpAioSession<T> aioSession = cacheAioSessionQueue.poll();
                if (aioSession != null) {
                    completed0(aioSession.getLastReadSize(), aioSession);
                    synchronized (this) {
                        this.wait(100);
                    }
                    continue;
                }
                if (!lock.tryLock()) {
                    synchronized (this) {
                        this.wait(100);
                    }
                    continue;
                }
                try {
                    needNotify = true;
                    notEmpty.await();
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }
}