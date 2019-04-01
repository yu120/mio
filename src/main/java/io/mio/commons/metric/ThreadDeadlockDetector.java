package io.mio.commons.metric;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for detecting deadlocked threads.
 *
 * @author lry
 */
public class ThreadDeadlockDetector {

    private static final int MAX_STACK_TRACE_DEPTH = 100;

    private final ThreadMXBean threads;

    /**
     * Creates a new detector using the given {@link ThreadMXBean}.
     */
    public ThreadDeadlockDetector() {
        this.threads = ManagementFactory.getThreadMXBean();
    }

    /**
     * Returns a set of diagnostic stack traces for any deadlocked threads. If no threads are
     * deadlocked, returns an empty set.
     *
     * @return stack traces for deadlocked threads or an empty set
     */
    public Set<String> getDeadlockedThreads() {
        final long[] ids = threads.findDeadlockedThreads();
        if (ids != null) {
            final Set<String> deadlocks = new HashSet<>();
            for (ThreadInfo info : threads.getThreadInfo(ids, MAX_STACK_TRACE_DEPTH)) {
                final StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement element : info.getStackTrace()) {
                    stackTrace.append("\t at ").append(element.toString()).append(String.format("%n"));
                }

                deadlocks.add(String.format("%s locked on %s (owned by %s):%n%s", info.getThreadName(),
                        info.getLockName(), info.getLockOwnerName(), stackTrace.toString()));
            }

            return Collections.unmodifiableSet(deadlocks);
        }

        return Collections.emptySet();
    }

}
