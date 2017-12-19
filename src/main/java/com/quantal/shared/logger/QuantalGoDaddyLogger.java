package com.quantal.shared.logger;

import com.godaddy.logging.Logger;

public interface QuantalGoDaddyLogger extends Logger {

    default void throwing(Throwable t) {
        this.error(t.getMessage(),t);
        try {
            throw t;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
