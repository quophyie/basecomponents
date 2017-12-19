package com.quantal.shared.logger;

import com.godaddy.logging.Logger;

public interface QuantalGoDaddyLogger extends Logger {

    default void throwing(Throwable t) throws Throwable {
        this.error(t.getMessage(),t);
        throw t;
    }

}
