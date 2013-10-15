package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.remote.RemoteFileUpdateSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LocalFileUpdatePollingService {
    private final LockingDiff previousState;
    private final FileUpdateFacade fileUpdateFacade;
    private final RemoteFileUpdateSender remoteFileUpdateSender;
    private final Logger logger;

    public LocalFileUpdatePollingService(LockingDiff previousState, FileUpdateFacade fileUpdateFacade, RemoteFileUpdateSender remoteFileUpdateSender) {
        this.previousState = previousState;
        this.fileUpdateFacade = fileUpdateFacade;
        this.remoteFileUpdateSender = remoteFileUpdateSender;
        this.logger = LoggerFactory.getLogger(LocalFileUpdatePollingService.class);
    }

    public void pollForLocalFileUpdates() throws IOException, InterruptedException, TimeoutException {
        Diff previous = previousState.lockThenGet();
        Diff current = fileUpdateFacade.getCurrentState();

        Diff interDiff = fileUpdateFacade.interDiff(previous, current);
        if (interDiff.hasChanges()) {
            logger.debug("interdiff has changes, they are:\n" + interDiff.toString());
            remoteFileUpdateSender.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.unlock();
    }
}
