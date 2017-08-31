package cn.ms.mio.transport;

/**
 * The Transport of Client or Server.
 * 
 * @author lry
 */
public interface Transport {

	/**
	 * The initialize transport.
	 * 
	 * @throws Exception
	 */
	void initialize() throws Exception;

	/**
	 * The startup transport.
	 * 
	 * @return
	 * @throws Exception
	 */
	boolean startup() throws Exception;

	/**
	 * The destroy transport
	 */
	void destroy();

}
