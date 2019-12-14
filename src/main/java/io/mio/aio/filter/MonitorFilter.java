package io.mio.aio.filter;

import io.mio.aio.support.AioMioSession;
import io.mio.aio.support.EventState;
import io.mio.aio.NetFilter;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 服务器运行状态监控插件
 *
 * @param <T>
 * @author lry
 */
@Slf4j
public final class MonitorFilter<T> implements Runnable, NetFilter<T> {

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r, "Mio-Timer");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * 任务执行频率
     */
    private int seconds = 0;
    /**
     * 当前周期内消息 流量监控
     */
    private LongAdder inFlow = new LongAdder();

    /**
     * 当前周期内消息 流量监控
     */
    private LongAdder outFlow = new LongAdder();

    /**
     * 当前周期内处理失败消息数
     */
    private LongAdder processFailNum = new LongAdder();

    /**
     * 当前周期内处理消息数
     */
    private LongAdder processMsgNum = new LongAdder();

    private LongAdder totalProcessMsgNum = new LongAdder();

    /**
     * 新建连接数
     */
    private LongAdder newConnect = new LongAdder();

    /**
     * 断链数
     */
    private LongAdder disConnect = new LongAdder();

    /**
     * 在线连接数
     */
    private long onlineCount;

    private LongAdder totalConnect = new LongAdder();

    private LongAdder readCount = new LongAdder();

    private LongAdder writeCount = new LongAdder();

    public MonitorFilter() {
        this(60);
    }

    public MonitorFilter(int seconds) {
        this.seconds = seconds;
        long mills = TimeUnit.SECONDS.toMillis(seconds);
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(this, mills, mills, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean preProcess(AioMioSession<T> session, T t) {
        processMsgNum.increment();
        totalProcessMsgNum.increment();
        return true;
    }

    @Override
    public void stateEvent(EventState eventState, AioMioSession<T> session, Throwable throwable) {
        switch (eventState) {
            case PROCESS_EXCEPTION:
                processFailNum.increment();
                break;
            case NEW_SESSION:
                newConnect.increment();
                break;
            case SESSION_CLOSED:
                disConnect.increment();
                break;
            default:
                //ignore other state
                break;
        }
    }

    @Override
    public void run() {
        long curInFlow = getAndReset(inFlow);
        long curOutFlow = getAndReset(outFlow);
        long curDiscardNum = getAndReset(processFailNum);
        long curProcessMsgNum = getAndReset(processMsgNum);
        long connectCount = getAndReset(newConnect);
        long disConnectCount = getAndReset(disConnect);
        onlineCount += connectCount - disConnectCount;
        log.info("inflow:" + curInFlow * 1.0 / (1024 * 1024) + "(MB)"
                + ",outflow:" + curOutFlow * 1.0 / (1024 * 1024) + "(MB)"
                + ",process fail:" + curDiscardNum
                + ",process success:" + curProcessMsgNum
                + ",process total:" + totalProcessMsgNum.longValue()
                + ",read count:" + getAndReset(readCount) + ",write count:" + getAndReset(writeCount)
                + ",connect count:" + connectCount
                + ",disconnect count:" + disConnectCount
                + ",online count:" + onlineCount
                + ",connected total:" + getAndReset(totalConnect)
                + ",Requests/sec:" + curProcessMsgNum * 1.0 / seconds
                + ",Transfer/sec:" + (curInFlow * 1.0 / (1024 * 1024) / seconds) + "(MB)");
    }

    private long getAndReset(LongAdder longAdder) {
        long result = longAdder.longValue();
        longAdder.add(-result);
        return result;
    }

    @Override
    public boolean shouldAccept(AsynchronousSocketChannel channel) {
        return true;
    }

    @Override
    public void afterRead(AioMioSession<T> session, int readSize) {
        //出现result为0,说明代码存在问题
        if (readSize == 0) {
            log.error("readSize is 0");
        }
        inFlow.add(readSize);
    }

    @Override
    public void beforeRead(AioMioSession<T> session) {
        readCount.increment();
    }

    @Override
    public void afterWrite(AioMioSession<T> session, int writeSize) {
        outFlow.add(writeSize);
    }

    @Override
    public void beforeWrite(AioMioSession<T> session) {
        writeCount.increment();
    }

}
