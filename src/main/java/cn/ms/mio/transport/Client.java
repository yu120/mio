package cn.ms.mio.transport;

import javax.security.auth.callback.Callback;

import cn.ms.mio.common.Message;
import cn.ms.neural.extension.NSPI;

/**
 * The Transport of Client.
 * 
 * @author lry
 */
@NSPI
public interface Client extends Transport {

	/**
	 * The Request-Response Communication.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	Message request(Message message) throws Exception;

	/**
	 * The Callback Communication.
	 * 
	 * @param message
	 * @param callback
	 * @throws Exception
	 */
	void callback(Message message, Callback callback) throws Exception;

}
