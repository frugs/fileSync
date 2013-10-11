package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;

import java.io.File;
import java.io.IOException;

public class SystemCommandFacade {
    private final SystemCommandExecutor systemCommandExecutor;
    private final FileWriter fileWriter;

    public SystemCommandFacade(SystemCommandExecutor systemCommandExecutor, FileWriter fileWriter) {
        this.systemCommandExecutor = systemCommandExecutor;
        this.fileWriter = fileWriter;
    }

    public Diff gitDiffHead() throws IOException {
        return Diff.fromInputStream(systemCommandExecutor.gitDiffHead());
    }

    public Diff interDiff(Diff firstDiff, Diff secondDiff) throws IOException {
        File firstDiffFile = fileWriter.writeBytesToFile(firstDiff.toByteArray());
        File secondDiffFile = fileWriter.writeBytesToFile(secondDiff.toByteArray());

        Diff result =  Diff.fromInputStream(systemCommandExecutor.interDiff(firstDiffFile.getPath(), secondDiffFile.getPath()));
        firstDiffFile.delete();
        secondDiffFile.delete();

        return result;
    }

    public void gitApply(Diff diff) throws IOException {
        File diffFile = fileWriter.writeBytesToFile(diff.toByteArray());
        systemCommandExecutor.gitApply(diffFile.getPath());
        diffFile.delete();
    }
}
