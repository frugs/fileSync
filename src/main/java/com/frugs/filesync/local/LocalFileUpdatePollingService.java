package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.RemoteFileUpdateSender;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class LocalFileUpdatePollingService {
    private final SystemCommandExecutor systemCommandExecutor;
    private final LockedDiff previousState;
    private final RemoteFileUpdateSender remoteFileUpdateSender;
    private final Logger logger;

    public LocalFileUpdatePollingService(SystemCommandExecutor systemCommandExecutor, LockedDiff previousState, RemoteFileUpdateSender remoteFileUpdateSender, Logger logger) {
        this.systemCommandExecutor = systemCommandExecutor;
        this.previousState = previousState;
        this.remoteFileUpdateSender = remoteFileUpdateSender;
        this.logger = logger;
    }

    public void pollForLocalFileUpdates() throws IOException {
        Diff current = fromInputStream(systemCommandExecutor.gitDiffHead());
        Diff previous = previousState.retrieve();

        InputStream interDiffInputStream = systemCommandExecutor.interDiff(previous.toString(), current.toString());
        Diff interDiff = fromInputStream(interDiffInputStream);

        if (interDiff.hasChanges()) {
            logger.info("interdiff has changes, they are:\n" + interDiff.toString());
            remoteFileUpdateSender.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.putBack();
    }
}
