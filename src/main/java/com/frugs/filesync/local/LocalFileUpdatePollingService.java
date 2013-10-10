package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.FileUpdateFacade;
import com.frugs.filesync.remote.RemoteFileUpdateSender;

import java.io.IOException;
import java.util.logging.Logger;

public class LocalFileUpdatePollingService {
    private final LockedDiff previousState;
    private final FileUpdateFacade fileUpdateFacade;
    private final RemoteFileUpdateSender remoteFileUpdateSender;
    private final Logger logger;

    public LocalFileUpdatePollingService(LockedDiff previousState, FileUpdateFacade fileUpdateFacade, RemoteFileUpdateSender remoteFileUpdateSender, Logger logger) {
        this.previousState = previousState;
        this.fileUpdateFacade = fileUpdateFacade;
        this.remoteFileUpdateSender = remoteFileUpdateSender;
        this.logger = logger;
    }

    public void pollForLocalFileUpdates() throws IOException {
        Diff current = fileUpdateFacade.getCurrentState();
        Diff previous = previousState.retrieve();

        Diff interDiff = fileUpdateFacade.interDiff(previous, current);
        if (interDiff.hasChanges()) {
            logger.info("interdiff has changes, they are:\n" + interDiff.toString());
            remoteFileUpdateSender.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.putBack();
    }
}
