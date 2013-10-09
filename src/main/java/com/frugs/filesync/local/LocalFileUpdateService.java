package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.ExternalFileUpdateService;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class LocalFileUpdateService {
    private final SystemCommandExecutor systemCommandExecutor;
    private final ExternalFileUpdateService externalFileUpdateService;
    private final LockedDiff previousState;

    @Inject
    public LocalFileUpdateService(SystemCommandExecutor systemCommandExecutor, ExternalFileUpdateService externalFileUpdateService, LockedDiff previousState) {
        this.systemCommandExecutor = systemCommandExecutor;
        this.externalFileUpdateService = externalFileUpdateService;
        this.previousState = previousState;
    }

    public void pollForLocalFileUpdates() throws IOException {
        Diff current = fromInputStream(systemCommandExecutor.gitDiffHead());
        Diff previous = previousState.retrieve();

        InputStream interDiffInputStream = systemCommandExecutor.interDiff(previous.toString(), current.toString());
        Diff interDiff = fromInputStream(interDiffInputStream);

        if (interDiff.hasChanges()) {
            externalFileUpdateService.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.putBack();
    }

    public void updateLocalFiles(Diff update) throws IOException {
        systemCommandExecutor.gitApply(update.toString());
    }
}
