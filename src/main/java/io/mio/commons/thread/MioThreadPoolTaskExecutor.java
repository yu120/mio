package io.mio.commons.thread;

import org.slf4j.MDC;

import java.util.Map;

/**
 * 可以在每个任务之前设置子线程的MDC数据
 * <p/>
 * 在记录日志的时候，一般情况下我们会使用MDC来存储每个线程的特有参数，如身份信息等，以便更好的查询日志。
 * 但是Logback在最新的版本中因为性能问题，不会自动的将MDC的内存传给子线程。所以Logback建议在执行异步线程前
 * 先通过MDC.getCopyOfContextMap()方法将MDC内存获取出来，再传给线程。
 * 并在子线程的执行的最开始调用MDC.setContextMap(context)方法将父线程的MDC内容传给子线程。
 * <p>
 * https://logback.qos.ch/manual/mdc.html
 *
 * @author lry
 */
public class MioThreadPoolTaskExecutor extends StandardThreadExecutor {

    /**
     * 所有线程都会委托给这个execute方法，在这个方法中我们把父线程的MDC内容赋值给子线程
     * https://logback.qos.ch/manual/mdc.html#managedThreads
     *
     * @param runnable {@link Runnable}
     */
    @Override
    public void execute(Runnable runnable) {
        // 获取父线程MDC中的内容，必须在run方法之前，否则等异步线程执行的时候有可能MDC里面的值已经被清空了，这个时候就会返回null
        Map<String, String> context = MDC.getCopyOfContextMap();
        super.execute(() -> {
            try {
                // 将父线程的MDC内容传给子线程
                MDC.setContextMap(context);
                // 执行异步操作
                runnable.run();
            } finally {
                // 清空MDC内容
                MDC.clear();
            }
        });
    }

}
