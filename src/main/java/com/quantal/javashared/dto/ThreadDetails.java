package com.quantal.javashared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ThreadDetails {

    @Builder.Default
    private long threadId = Thread.currentThread().getId();
    @Builder.Default
    private String threadName = Thread.currentThread().getName();
    @Builder.Default
    private ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    public ThreadDetails(){

    }
}
