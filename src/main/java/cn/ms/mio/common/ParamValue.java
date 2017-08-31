package cn.ms.mio.common;

public enum ParamValue {

	/** The host **/
	host("host", "0.0.0.0"),
	/** The min port **/
	minPort("minPort", 1024),
	/** The max port **/
	maxPort("maxPort", 65535),
	
	/** The serialize **/
	serialize("serialize", "hessian2"),
	serialization("serialization"),

	/** The transport layer implementation **/
	transport("transport", "netty"),

	/** The server needs to start hold **/
	hold("hold", false),

	/** netty Server Boss ThreadPool Size **/
	bossThread("bossThread", 0),
	/** The netty Server Worker ThreadPool Size **/
	workerThread("workerThread", 0),
	/** The future submit task pool thread size **/
	futureSubmitThread("futureSubmitThread", 16),

	/** server thread pool **/
	workerThreadPool("workerThreadPool", false),
	/** service min worker threads **/
	minWorkerThread("minWorkerThread", 20),
	/** service max worker threads **/
	maxWorkerThread("maxWorkerThread", 200),
	/** worker queue size **/
	workerQueueSize("workerQueueSize", 0),
	/** service shutdown timeout **/
	shutdownWorkerTimeout("shutdownWorkerTimeout", 60 * 1000),
	/** service shutdown timeout **/
	shutdownBossTimeout("shutdownBossTimeout", 70 * 1000),

	/** max server conn (all clients conn) **/
	maxServerConnection("maxServerConnection", 10 * 1000),
	/** The start gracefully **/
	startupGracefully("startupGracefully", 15 * 1000),

	sendAsync("sendAsync", true), lazyInit("lazyInit", false),
	/** pool conn manger stragy **/
	poolLifo("poolLifo", true),
	/** pool min conn number **/
	minClientConnection("minClientConnection", 2),
	/** pool max conn number **/
	maxClientConnection("maxClientConnection", 10),
	/** request timeout **/
	maxWaitTimeout("maxWaitTimeout", 2000),

	/** The http client send uri **/
	uri("uri", "/"),
	/** The http client send method **/
	httpMethod("httpMethod", "POST"),
	/** The http client send protocol version **/
	protocolVersion("protocolVersion", "HTTP/1.1"),
	/** The http server response status code **/
	httpResponseStatus("httpResponseStatus", 200),

	/** The version of service **/
	version("version", "1.0.0"),
	/** The group of service **/
	group("group", "def_group"),
	/** The data encoding and decoding **/
	codec("codec", "ms"),
	/** Maximum value of data length of TCP protocol **/
	frameDataLength("frameDataLength", 8), ;

	private String name;
	private String value;
	private long longValue;
	private int intValue;
	private boolean boolValue;

	ParamValue(String name) {
		this.name = name;
	}
	
	ParamValue(String name, String value) {
		this.name = name;
		this.value = value;
	}

	ParamValue(String name, long longValue) {
		this.name = name;
		this.value = String.valueOf(longValue);
		this.longValue = longValue;
	}

	ParamValue(String name, int intValue) {
		this.name = name;
		this.value = String.valueOf(intValue);
		this.intValue = intValue;
	}

	ParamValue(String name, boolean boolValue) {
		this.name = name;
		this.value = String.valueOf(boolValue);
		this.boolValue = boolValue;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int getIntValue() {
		return intValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public boolean getBooleanValue() {
		return boolValue;
	}

}
