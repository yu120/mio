package io.mio.commons;

import lombok.Getter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 微时间
 * <br>
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 *
 * @author lry
 */
@Getter
public enum SystemClock {

    // ====

    INSTANCE(1L);

    private final AtomicLong now;
    private ScheduledExecutorService scheduler;

    SystemClock(long period) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        }));

        this.now = new AtomicLong(System.currentTimeMillis());
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "system-clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    public long currentTimeMillis() {
        return now.get();
    }

}