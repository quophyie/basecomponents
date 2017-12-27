package com.quantal.shared.dto;

import io.logz.sender.SenderStatusReporter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

@Data
@AllArgsConstructor
public class LogzioConfig {
    private String logzioToken;
    private String logzioType;
    private int drainTimeout;
    private int fsPercentThreshold;
    private File bufferDir;
    private String logzioUrl;
    private int socketTimeout;
    private int connectTimeout;
    private boolean debug;
    private SenderStatusReporter reporter;
    private ScheduledExecutorService tasksExecutor;
    private int gcPersistedQueueFilesIntervalSeconds;

    public LogzioConfig(){}
}
