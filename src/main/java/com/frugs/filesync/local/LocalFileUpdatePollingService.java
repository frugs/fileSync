package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.RemoteFileUpdateReceiver;
import com.frugs.filesync.remote.RemoteFileUpdateSender;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class LocalFileUpdatePollingService {
    private final SystemCommandExecutor systemCommandExecutor;
    private final LockedDiff previousState;
    private RemoteFileUpdateSender remoteFileUpdateSender;

    public LocalFileUpdatePollingService(SystemCommandExecutor systemCommandExecutor, LockedDiff previousState, RemoteFileUpdateSender remoteFileUpdateSender) {
        this.systemCommandExecutor = systemCommandExecutor;
        this.previousState = previousState;
        this.remoteFileUpdateSender = remoteFileUpdateSender;
    }

    public void pollForLocalFileUpdates() throws IOException {
        Diff current = fromInputStream(systemCommandExecutor.gitDiffHead());
        Diff previous = previousState.retrieve();

        InputStream interDiffInputStream = systemCommandExecutor.interDiff(previous.toString(), current.toString());
        Diff interDiff = fromInputStream(interDiffInputStream);

        if (interDiff.hasChanges()) {
            remoteFileUpdateSender.sendUpdates(interDiff);
            previousState.set(current);
        }
        previousState.putBack();
    }
}
