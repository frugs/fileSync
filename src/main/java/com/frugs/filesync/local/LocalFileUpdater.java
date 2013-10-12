package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LocalFileUpdater {
    private final LockingDiff previousState;
    private final FileUpdateFacade fileUpdateFacade;
    private final Logger logger;

    public LocalFileUpdater(LockingDiff previousState, FileUpdateFacade fileUpdateFacade) {
        this.previousState = previousState;
        this.fileUpdateFacade = fileUpdateFacade;
        this.logger = LoggerFactory.getLogger(LocalFileUpdater.class);
    }

    public void updateLocalFiles(Diff update) throws IOException, InterruptedException, TimeoutException {
        logger.debug("updating local files");
        previousState.lock();
        fileUpdateFacade.applyDiff(update);

        Diff updatedDiff = fileUpdateFacade.getCurrentState();
        previousState.set(updatedDiff);
        previousState.unlock();
    }
}
