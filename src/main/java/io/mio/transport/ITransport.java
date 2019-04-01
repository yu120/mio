package io.mio.transport;

import io.mio.support.CallbackListener;
import io.mio.model.ResponseFuture;
import io.mio.model.Request;
import io.mio.model.Response;

/**
 * Transport
 *
 * @author lry
 */
public class ITransport {

    public static Response call(Request request) {
        return null;
    }

    public static ResponseFuture send(Request request) {
        return null;
    }

    public static void callback(Request request, CallbackListener listener) {

    }

    public static void promise(Request request, CallbackListener listener) {

    }

}
