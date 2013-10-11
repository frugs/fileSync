package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;

import java.io.IOException;
import java.util.logging.Logger;

public class LocalFileUpdater {
    private final LockedDiff previousState;
    private final FileUpdateFacade fileUpdateFacade;
    private final Logger logger;

    public LocalFileUpdater(LockedDiff previousState, FileUpdateFacade fileUpdateFacade, Logger logger) {
        this.previousState = previousState;
        this.fileUpdateFacade = fileUpdateFacade;
        this.logger = logger;
    }

    public void updateLocalFiles(Diff update) throws IOException, InterruptedException {
        logger.info("updating local files");
        previousState.retrieve();
        fileUpdateFacade.applyDiff(update);

        Diff updatedDiff = fileUpdateFacade.getCurrentState();
        previousState.set(updatedDiff);
        previousState.putBack();
    }
}
