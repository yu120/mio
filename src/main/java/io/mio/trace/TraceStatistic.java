package io.mio.trace;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Global  Trace Statistic
 *
 * @author lry
 */
@Slf4j
public enum TraceStatistic {

    // ===

    INSTANCE;

    private static final Map<String, LongAdder[]> REQUESTS = new ConcurrentHashMap<>();

    private static final String STATISTIC = "MioRequest Statistic[{}]:" +
            "[0-49ms->{}][50-199ms->{}][200-499ms->{}][500-2999ms->{}][3000ms+->{}]";

    private static final int SECTION2 = 50;
    private static final int SECTION3 = 200;
    private static final int SECTION4 = 500;
    private static final int SECTION5 = 3000;

    private ScheduledExecutorService dumpScheduled;

    /**
     * 启动线程，每分钟打印日志，重置统计区间的取值
     */
    public void initialize(TraceProperties properties) {
        dumpScheduled = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setNameFormat("micro-trace").setDaemon(true).build());
        dumpScheduled.scheduleAtFixedRate(() -> {
            try {
                TraceStatistic.INSTANCE.dump();
            } catch (Exception e) {
                //do nothing
                //catch all exception
            }
        }, 5000L, properties.getDumpPeriodSec() * 1000L, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录统计信息
     *
     * @param requestURI 请求的完整url
     * @param duration   请求的响应延时
     */
    public synchronized void put(String requestURI, int duration) {
        LongAdder[] sectionArr = REQUESTS.get(requestURI);
        if (sectionArr == null) {
            sectionArr = new LongAdder[5];
            sectionArr[0] = new LongAdder();
            sectionArr[1] = new LongAdder();
            sectionArr[2] = new LongAdder();
            sectionArr[3] = new LongAdder();
            sectionArr[4] = new LongAdder();
            REQUESTS.put(requestURI, sectionArr);
        }

        if (duration < SECTION2) {
            sectionArr[0].increment();
        } else if (duration < SECTION3) {
            sectionArr[1].increment();
        } else if (duration < SECTION4) {
            sectionArr[2].increment();
        } else if (duration < SECTION5) {
            sectionArr[3].increment();
        } else {
            sectionArr[4].increment();
        }
    }

    private synchronized void dump() {
        if (REQUESTS.isEmpty()) {
            return;
        }

        log.info("*********************************** Begin dump request statistic info ***********************************");
        for (Map.Entry<String, LongAdder[]> entry : REQUESTS.entrySet()) {
            LongAdder[] sectionArr = entry.getValue();
            if (sectionArr == null) {
                return;
            }

            long num1 = sectionArr[0].sumThenReset();
            long num2 = sectionArr[1].sumThenReset();
            long num3 = sectionArr[2].sumThenReset();
            long num4 = sectionArr[3].sumThenReset();
            long num5 = sectionArr[4].sumThenReset();
            if (num5 > 0) {
                log.warn(STATISTIC, entry.getKey(), num1, num2, num3, num4, num5);
            } else {
                log.info(STATISTIC, entry.getKey(), num1, num2, num3, num4, num5);
            }
        }
        log.info("*********************************** End dump request statistic info ***********************************");

        // dump完成后清理map记录
        REQUESTS.clear();
    }

    public void destroy() {
        if (dumpScheduled != null) {
            dumpScheduled.shutdown();
        }
        REQUESTS.clear();
    }

}
