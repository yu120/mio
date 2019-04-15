package io.mio.config;

import io.mio.commons.URLParamType;
import io.mio.commons.utils.NetUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Protocol Config
 *
 * @author lry
 */
@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class ProtocolConfig implements Serializable {

    /**
     * Protocol name
     */
    private String name = "mio";

    /**
     * Service ip address (when there are multiple network cards available)
     */
    private String host = NetUtils.getLocalHost();

    /**
     * Service port
     */
    private Integer port = 30000 + new Random(System.currentTimeMillis()).nextInt(10000);

    /**
     * IO thread pool's size (fixed size)
     */
    private Integer ioThreads = URLParamType.CPU_NUM.getIntValue() + 1;

    /**
     * Thread pool's core thread size (or fixed thread size)
     */
    private Integer threads = URLParamType.CPU_NUM.getIntValue() * 2;

    /**
     * Thread pool's queue length
     * <p>
     * queueCapacity = (coreSizePool/taskCostAvgTime)*maxResponseTime
     */
    private Integer queues = (int) ((threads / 0.1) * 1);

    /**
     * Thread pool's max thread size
     */
    private Integer maxThreads = threads * 10;

    /**
     * Max acceptable connections
     */
    private Integer accepts = 20000;

    /**
     * Serialize
     */
    private String serialize = "hessian2";

    /**
     * Heartbeat interval(ms)
     */
    private Long heartbeat = 5000L;

    /**
     * Status check,e.g: active、inactive
     */
    private String status = "active";

    /**
     * The customized parameters
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * minTasks/maxTasks ：每秒的任务数（基线），假设为500(minTasks)~1000(maxTasks)
     * taskCostAvgTimeSec：每个任务平均花费时间，假设为0.1s
     * maxResponseTime：系统允许容忍的最大响应时间，假设为1s
     */
    public void wrapperThreadPool(int maxTasks, int minTasks, double taskCostAvgTimeSec, double maxResponseTimeSec) {
        int actualThreads = (int) ((maxTasks - minTasks) * 0.2) + minTasks;
        this.threads = (int) (actualThreads * taskCostAvgTimeSec);
        this.queues = (int) (((double) threads / taskCostAvgTimeSec) * maxResponseTimeSec);
        this.maxThreads = (int) ((maxTasks - queues) * taskCostAvgTimeSec);
    }

}
