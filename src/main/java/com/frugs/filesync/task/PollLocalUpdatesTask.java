package com.frugs.filesync.task;

import com.frugs.filesync.local.LocalFileUpdatePollingService;

import java.io.IOException;

public class PollLocalUpdatesTask implements Runnable {
    private final LocalFileUpdatePollingService localFileUpdatePollingService;

    public PollLocalUpdatesTask(LocalFileUpdatePollingService localFileUpdatePollingService) {
        this.localFileUpdatePollingService = localFileUpdatePollingService;
    }

    @Override public void run() {
        try {
            localFileUpdatePollingService.pollForLocalFileUpdates();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
