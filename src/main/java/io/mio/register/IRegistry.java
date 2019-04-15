package io.mio.register;

import io.mio.commons.URL;

public interface IRegistry {

    void initialize(URL url);

    void register(URL url);

    void destroy();

}
