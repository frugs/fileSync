package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class LocalFileUpdater {
    private final LockedDiff previousState;
    private final SystemCommandExecutor systemCommandExecutor;
    private final Logger logger;

    public LocalFileUpdater(LockedDiff previousState, SystemCommandExecutor systemCommandExecutor, Logger logger) {
        this.previousState = previousState;
        this.systemCommandExecutor = systemCommandExecutor;
        this.logger = logger;
    }

    public void updateLocalFiles(Diff update) throws IOException {
        logger.info("updating local files\n" + update.toString());
        previousState.retrieve();
        systemCommandExecutor.gitApply(update.toString());

        InputStream updatedDiffInputStream = systemCommandExecutor.gitDiffHead();
        Diff updatedDiff = fromInputStream(updatedDiffInputStream);

        previousState.set(updatedDiff);
        previousState.putBack();
    }
}
