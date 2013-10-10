package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;

import java.io.IOException;

import static com.frugs.filesync.domain.Diff.fromInputStream;

public class FileUpdateFacade {
    private final SystemCommandExecutor systemCommandExecutor;

    public FileUpdateFacade(SystemCommandExecutor systemCommandExecutor) {
        this.systemCommandExecutor = systemCommandExecutor;
    }

    public Diff getCurrentState() throws IOException {
        return fromInputStream(systemCommandExecutor.gitDiffHead());
    }

    public Diff interDiff(Diff first, Diff second) throws IOException {
        Diff result;

        if (first.isEmpty()) {
            result = second;
        } else if (second.isEmpty()) {
            result = first;
        } else {
            result = fromInputStream(systemCommandExecutor.interDiff(first.toString(), second.toString()));
        }

        return result;
    }

    public void applyDiff(Diff diff) throws IOException {
        if (diff.hasChanges()) {
            systemCommandExecutor.gitApply(diff.toString());
        }
    }
}
