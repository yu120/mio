package io.mio.register;

import java.util.regex.Pattern;

/**
 * Constants
 *
 * @author lry
 */
public class Constants {

    public static final String PROVIDER = "provider";

    public static final String CONSUMER = "consumer";

    public static final String REGISTER = "register";

    public static final String UNREGISTER = "unregister";

    public static final String SUBSCRIBE = "subscribe";

    public static final String UNSUBSCRIBE = "unsubscribe";

    public static final String CATEGORY_KEY = "category";

    public static final String PROVIDERS_CATEGORY = "providers";

    public static final String CONSUMERS_CATEGORY = "consumers";

    public static final String ROUTERS_CATEGORY = "routers";

    public static final String CONFIGURATORS_CATEGORY = "configurators";

    public static final String DEFAULT_CATEGORY = PROVIDERS_CATEGORY;

    public static final String ENABLED_KEY = "enabled";

    public static final String DISABLED_KEY = "disabled";

    public static final String VALIDATION_KEY = "validation";

    public static final String CACHE_KEY = "cache";

    public static final String DYNAMIC_KEY = "dynamic";

    public static final String MICRO_PROPERTIES_KEY = "micro.properties.file";

    public static final String DEFAULT_MICRO_PROPERTIES = "micro.properties";

    public static final String SENT_KEY = "sent";

    public static final boolean DEFAULT_SENT = false;

    public static final String REGISTRY_PROTOCOL = "registry";

    public static final String $INVOKE = "$invoke";

    public static final String $ECHO = "$echo";

    public static final int DEFAULT_IO_THREADS = Runtime.getRuntime()
            .availableProcessors() + 1;

    public static final String DEFAULT_PROXY = "javassist";

    public static final int DEFAULT_PAYLOAD = 8 * 1024 * 1024;

    public static final String DEFAULT_CLUSTER = "failover";

    public static final String DEFAULT_DIRECTORY = "micro";

    public static final String DEFAULT_LOADBALANCE = "random";

    public static final String DEFAULT_PROTOCOL = "micro";

    public static final String DEFAULT_EXCHANGER = "header";

    public static final String DEFAULT_TRANSPORTER = "netty";

    public static final String DEFAULT_REMOTING_SERVER = "netty";

    public static final String DEFAULT_REMOTING_CLIENT = "netty";

    public static final String DEFAULT_REMOTING_CODEC = "micro";

    public static final String DEFAULT_REMOTING_SERIALIZATION = "hessian2";

    public static final String DEFAULT_HTTP_SERVER = "servlet";

    public static final String DEFAULT_HTTP_CLIENT = "jdk";

    public static final String DEFAULT_HTTP_SERIALIZATION = "json";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int DEFAULT_WEIGHT = 100;

    public static final int DEFAULT_FORKS = 2;

    public static final String DEFAULT_THREAD_NAME = "Micro";

    public static final int DEFAULT_CORE_THREADS = 0;

    public static final int DEFAULT_THREADS = 200;

    public static final boolean DEFAULT_KEEP_ALIVE = true;

    public static final int DEFAULT_QUEUES = 0;

    public static final int DEFAULT_ALIVE = 60 * 1000;

    public static final int DEFAULT_CONNECTIONS = 0;

    public static final int DEFAULT_ACCEPTS = 0;

    public static final int DEFAULT_IDLE_TIMEOUT = 600 * 1000;

    public static final int DEFAULT_HEARTBEAT = 60 * 1000;

    public static final int DEFAULT_TIMEOUT = 1000;

    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    public static final int DEFAULT_REGISTRY_CONNECT_TIMEOUT = 5000;

    public static final int DEFAULT_RETRIES = 2;

    // default buffer size is 8k.
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static final int MAX_BUFFER_SIZE = 16 * 1024;

    public static final int MIN_BUFFER_SIZE = 1 * 1024;

    public static final String REMOVE_VALUE_PREFIX = "-";

    public static final String HIDE_KEY_PREFIX = ".";

    public static final String DEFAULT_KEY_PREFIX = "default.";

    public static final String DEFAULT_KEY = "default";

    public static final String LOADBALANCE_KEY = "loadbalance";

    public static final String ROUTER_KEY = "router";

    public static final String CLUSTER_KEY = "cluster";

    public static final String REGISTRY_KEY = "registry";

    public static final String MONITOR_KEY = "monitor";

    public static final String SIDE_KEY = "side";

    public static final String PROVIDER_SIDE = "provider";

    public static final String CONSUMER_SIDE = "consumer";

    public static final String DEFAULT_REGISTRY = "micro";

    public static final String BACKUP_KEY = "backup";

    public static final String DIRECTORY_KEY = "directory";

    public static final String DEPRECATED_KEY = "deprecated";

    public static final String ANYHOST_KEY = "anyhost";

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String LOCALHOST_VALUE = "127.0.0.1";

    public static final String APPLICATION_KEY = "application";

    public static final String LOCAL_KEY = "local";

    public static final String STUB_KEY = "stub";

    public static final String MOCK_KEY = "mock";

    public static final String PROTOCOL_KEY = "protocol";

    public static final String PROXY_KEY = "proxy";

    public static final String WEIGHT_KEY = "weight";

    public static final String FORKS_KEY = "forks";

    public static final String DEFAULT_THREADPOOL = "limited";

    public static final String DEFAULT_CLIENT_THREADPOOL = "cached";

    public static final String THREADPOOL_KEY = "threadpool";

    public static final String THREAD_NAME_KEY = "threadname";

    public static final String IO_THREADS_KEY = "iothreads";

    public static final String CORE_THREADS_KEY = "corethreads";

    public static final String THREADS_KEY = "threads";

    public static final String QUEUES_KEY = "queues";

    public static final String ALIVE_KEY = "alive";

    public static final String EXECUTES_KEY = "executes";

    public static final String BUFFER_KEY = "buffer";

    public static final String PAYLOAD_KEY = "payload";

    public static final String REFERENCE_FILTER_KEY = "reference.filter";

    public static final String INVOKER_LISTENER_KEY = "invoker.listener";

    public static final String SERVICE_FILTER_KEY = "service.filter";

    public static final String EXPORTER_LISTENER_KEY = "exporter.listener";

    public static final String ACCESS_LOG_KEY = "accesslog";

    public static final String ACTIVES_KEY = "actives";

    public static final String CONNECTIONS_KEY = "connections";

    public static final String ACCEPTS_KEY = "accepts";

    public static final String IDLE_TIMEOUT_KEY = "idle.timeout";

    public static final String HEARTBEAT_KEY = "heartbeat";

    public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";

    public static final String CONNECT_TIMEOUT_KEY = "connect.timeout";

    public static final String TIMEOUT_KEY = "timeout";

    public static final String RETRIES_KEY = "retries";

    public static final String PROMPT_KEY = "prompt";

    public static final String DEFAULT_PROMPT = "micro>";

    public static final String CODEC_KEY = "codec";

    public static final String SERIALIZATION_KEY = "serialization";

    // modified by lishen
    public static final String EXTENSION_KEY = "extension";

    // modified by lishen
    public static final String KEEP_ALIVE_KEY = "keepalive";

    // modified by lishen
    // TODO change to a better name
    public static final String OPTIMIZER_KEY = "optimizer";

    public static final String EXCHANGER_KEY = "exchanger";

    public static final String TRANSPORTER_KEY = "transporter";

    public static final String TRANSPORTER_DEV_VAL = "curator";

    public static final String SERVER_KEY = "server";

    public static final String CLIENT_KEY = "client";

    public static final String ID_KEY = "id";

    public static final String ASYNC_KEY = "async";

    public static final String RETURN_KEY = "return";

    public static final String TOKEN_KEY = "token";

    public static final String METHOD_KEY = "method";

    public static final String METHODS_KEY = "methods";

    public static final String CHARSET_KEY = "charset";

    public static final String RECONNECT_KEY = "reconnect";

    public static final String SEND_RECONNECT_KEY = "send.reconnect";

    public static final int DEFAULT_RECONNECT_PERIOD = 2000;

    public static final String SHUTDOWN_TIMEOUT_KEY = "shutdown.timeout";

    public static final int DEFAULT_SHUTDOWN_TIMEOUT = 1000 * 60 * 15;

    public static final String PID_KEY = "pid";

    public static final String TIMESTAMP_KEY = "timestamp";

    public static final String WARMUP_KEY = "warmup";

    public static final int DEFAULT_WARMUP = 10 * 60 * 1000;

    public static final String CHECK_KEY = "check";

    public static final String REGISTER_KEY = "register";

    public static final String SUBSCRIBE_KEY = "subscribe";

    public static final String GROUP_KEY = "group";

    public static final String PATH_KEY = "path";

    public static final String INTERFACE_KEY = "interface";

    public static final String GENERIC_KEY = "generic";

    public static final String FILE_KEY = "file";

    public static final String WAIT_KEY = "wait";

    public static final String CLASSIFIER_KEY = "classifier";

    public static final String VERSION_KEY = "version";

    public static final String REVISION_KEY = "revision";

    public static final String MICRO_VERSION_KEY = "micro";

    public static final String HESSIAN_VERSION_KEY = "hessian.version";

    public static final String DISPATCHER_KEY = "dispatcher";

    public static final String CHANNEL_HANDLER_KEY = "channel.handler";

    public static final String DEFAULT_CHANNEL_HANDLER = "default";

    public static final String ANY_VALUE = "*";

    public static final String COMMA_SEPARATOR = ",";

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    public static final String COUNT_PROTOCOL = "count";

    public static final String TRACE_PROTOCOL = "trace";

    public static final String EMPTY_PROTOCOL = "empty";

    public static final String ADMIN_PROTOCOL = "admin";

    public static final String PROVIDER_PROTOCOL = "provider";

    public static final String CONSUMER_PROTOCOL = "consumer";


    /**
     * 注册中心是否同步存储文件，默认异步
     */
    public static final String REGISTRY_FILESAVE_SYNC_KEY = "save.file";

    /**
     * 注册中心失败事件重试事件
     */
    public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * 重试周期
     */
    public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    /**
     * 注册中心自动重连时间
     */
    public static final String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";
    public static final int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;
    public static final String SESSION_TIMEOUT_KEY = "session";
    public static final int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

}