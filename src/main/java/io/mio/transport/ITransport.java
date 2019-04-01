package io.mio.transport;

import io.mio.ICallbackListener;
import io.mio.MioResponseFuture;
import io.mio.MioRequest;
import io.mio.MioResponse;

/**
 * Transport
 *
 * @author lry
 */
public class ITransport {

    public static MioResponse call(MioRequest request) {
        return null;
    }

    public static MioResponseFuture send(MioRequest request) {
        return null;
    }

    public static void callback(MioRequest request, ICallbackListener listener) {

    }

    public static void promise(MioRequest request, ICallbackListener listener) {

    }

}
