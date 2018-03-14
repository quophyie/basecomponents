package com.quantal.javashared.logger;

import com.godaddy.logging.Logger;
import com.quantal.javashared.dto.CommonLogFields;

import java.util.Arrays;

public interface QuantalLogger extends Logger {


    default RuntimeException throwing(Throwable t, Object... args) {
        args = Arrays.copyOf(args, args.length +1);
        Arrays.fill(args, args.length - 1, args.length, t );
        this.error(t.getMessage(), args);
          return new RuntimeException(t);
    }

    CommonLogFields getCommoFields();
    void setCommoFields(CommonLogFields commonLogFields);
}
