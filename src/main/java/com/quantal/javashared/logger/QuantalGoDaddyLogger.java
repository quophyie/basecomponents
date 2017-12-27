package com.quantal.javashared.logger;

import com.godaddy.logging.Logger;
import com.quantal.javashared.dto.CommonLogFields;

public interface QuantalGoDaddyLogger extends Logger {


    default RuntimeException throwing(Throwable t) {
        this.error(t.getMessage(),t);
          return new RuntimeException(t);
    }

    CommonLogFields getCommoFields();
    void setCommoFields(CommonLogFields commonLogFields);
}
