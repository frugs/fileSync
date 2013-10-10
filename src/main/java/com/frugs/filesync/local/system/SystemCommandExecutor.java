package com.frugs.filesync.local.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class SystemCommandExecutor {

    private final Logger logger;

    private static final Runtime currentRuntime = Runtime.getRuntime();

    public SystemCommandExecutor(Logger logger) {
        this.logger = logger;
    }

    public InputStream gitDiffHead() throws IOException {
        logger.info("performing git diff head");
        return execute("git diff HEAD");
    }

    public InputStream interDiff(String diff1, String diff2) throws IOException {
        logger.info("performing interdiff");
        return execute("interdiff <(" + diff1 + ") <(" + diff2 + ")");
    }

    public void gitApply(String diff) throws IOException {
        logger.info("applying diff");
        execute("echo " + diff + "| git apply");
    }

    private InputStream execute(String command) throws IOException {
        return currentRuntime.exec(command).getInputStream();
    }
}
