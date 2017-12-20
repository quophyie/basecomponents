package com.quantal.shared.logger;

import com.godaddy.logging.Logger;

public interface QuantalGoDaddyLogger extends Logger {

    default Throwable throwing(Throwable t) {
        this.error(t.getMessage(),t);
          return new RuntimeException(t);
    }

}
