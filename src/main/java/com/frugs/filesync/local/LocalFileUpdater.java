package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LockedDiff;
import com.frugs.filesync.local.system.SystemCommandExecutor;

import java.io.IOException;
import java.io.InputStream;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class LocalFileUpdater {
    private LockedDiff previousState;
    private SystemCommandExecutor systemCommandExecutor;

    public LocalFileUpdater(LockedDiff previousState, SystemCommandExecutor systemCommandExecutor) {
        this.previousState = previousState;
        this.systemCommandExecutor = systemCommandExecutor;
    }

    public void updateLocalFiles(Diff update) throws IOException {
        Diff previous = previousState.retrieve();
        systemCommandExecutor.gitApply(update.toString());

        InputStream updatedDiffInputStream = systemCommandExecutor.combineDiff(previous.toString(), update.toString());
        Diff updatedDiff = fromInputStream(updatedDiffInputStream);

        previousState.set(updatedDiff);
        previousState.putBack();
    }
}