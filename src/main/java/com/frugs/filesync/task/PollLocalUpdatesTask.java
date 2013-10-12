package com.frugs.filesync.task;

import com.frugs.filesync.local.LocalFileUpdatePollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollLocalUpdatesTask implements Runnable {
    private final LocalFileUpdatePollingService localFileUpdatePollingService;
    private final Logger logger;

    public PollLocalUpdatesTask(LocalFileUpdatePollingService localFileUpdatePollingService) {
        this.localFileUpdatePollingService = localFileUpdatePollingService;
        this.logger = LoggerFactory.getLogger(PollLocalUpdatesTask.class);
    }

    @Override public void run() {
        try {
            localFileUpdatePollingService.pollForLocalFileUpdates();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
