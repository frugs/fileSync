package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.remote.RemoteFileUpdateSender;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class LocalFileUpdatePollingService {
    private final LockingDiff previousState;
    private final FileUpdateFacade fileUpdateFacade;
    private final RemoteFileUpdateSender remoteFileUpdateSender;
    private final Logger logger;

    public LocalFileUpdatePollingService(LockingDiff previousState, FileUpdateFacade fileUpdateFacade, RemoteFileUpdateSender remoteFileUpdateSender, Logger logger) {
        this.previousState = previousState;
        this.fileUpdateFacade = fileUpdateFacade;
        this.remoteFileUpdateSender = remoteFileUpdateSender;
        this.logger = logger;
    }

    public void pollForLocalFileUpdates() throws IOException, InterruptedException, TimeoutException {
        Diff current = fileUpdateFacade.getCurrentState();
        Diff previous = previousState.lockThenGet();

        Diff interDiff = fileUpdateFacade.interDiff(previous, current);
        if (interDiff.hasChanges()) {
            logger.info("interdiff has changes, they are:\n" + interDiff.toString());
            remoteFileUpdateSender.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.unlock();
    }
}
