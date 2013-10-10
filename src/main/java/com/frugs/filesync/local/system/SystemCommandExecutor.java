package com.frugs.filesync.local.system;

import java.io.IOException;
import java.io.InputStream;

public class SystemCommandExecutor {
    private static final Runtime currentRuntime = Runtime.getRuntime();

    public InputStream gitDiffHead() throws IOException {
        return execute("git diff HEAD");
    }

    public InputStream interDiff(String diff1, String diff2) throws IOException {
        return execute("interdiff <(" + diff1 + ") <(" + diff2 + ")");
    }

    public void gitApply(String diff) throws IOException {
        execute("echo " + diff + "| git apply");
    }

    public InputStream combineDiff(String diff1, String diff2) throws IOException {
        return execute("combinediff <(" + diff1 + ") <(" + diff2 + ")");
    }

    private InputStream execute(String command) throws IOException {
        return currentRuntime.exec(command).getInputStream();
    }
}
