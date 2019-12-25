package io.mio.transport.aio.handler;

import io.mio.transport.aio.NetFilter;
import io.mio.transport.aio.support.AioMioSession;
import io.mio.transport.aio.support.EventState;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 读写事件回调处理类
 *
 * @param <T>
 * @author lry
 */
@Slf4j
public class ReadCompletionHandler<T> implements CompletionHandler<Integer, AioMioSession<T>>, Runnable {

    /**
     * 读回调资源信号量
     */
    private Semaphore semaphore;
    /**
     * 读会话缓存队列
     */
    private ConcurrentLinkedQueue<AioMioSession<T>> cacheAioMioSessionQueue;

    private volatile boolean needNotify = true;
    private ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private boolean running = true;

    public ReadCompletionHandler() {
    }

    public ReadCompletionHandler(final Semaphore semaphore) {
        this.semaphore = semaphore;
        this.cacheAioMioSessionQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void completed(final Integer result, final AioMioSession<T> aioSession) {
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
        cacheAioMioSessionQueue.offer(aioSession);
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
        if (cacheAioMioSessionQueue.isEmpty() || !semaphore.tryAcquire()) {
            return;
        }
        AioMioSession<T> currentSession = cacheAioMioSessionQueue.poll();
        if (currentSession == null) {
            semaphore.release();
            return;
        }

        AioMioSession<T> nextSession;
        while (currentSession != null) {
            nextSession = cacheAioMioSessionQueue.poll();
            if (nextSession == null) {
                currentSession.setReadSemaphore(semaphore);
            }
            completed0(currentSession.getLastReadSize(), currentSession);
            currentSession = nextSession;
        }
    }

    /**
     * 处理消息读回调事件
     *
     * @param result     已读消息字节数
     * @param aioSession 当前触发读回调的会话
     */
    private void completed0(final Integer result, final AioMioSession<T> aioSession) {
        try {
            // 接收到的消息进行预处理
            NetFilter<T> monitor = aioSession.getMessageProcessor();
            if (monitor != null) {
                monitor.afterRead(aioSession, result);
            }
            aioSession.readFromChannel(result == -1);
        } catch (Exception e) {
            failed(e, aioSession);
        }
    }

    @Override
    public void failed(Throwable exc, AioMioSession<T> aioSession) {
        try {
            aioSession.getMessageProcessor().stateEvent(aioSession, EventState.INPUT_EXCEPTION, exc);
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
                AioMioSession<T> aioSession = cacheAioMioSessionQueue.poll();
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
                log.error("Interrupted exception", e);
            }
        }
    }
}