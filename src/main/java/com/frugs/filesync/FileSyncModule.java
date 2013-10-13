package com.frugs.filesync;

import com.frugs.filesync.local.FileUpdateFacade;
import com.frugs.filesync.local.LocalFileUpdatePollingService;
import com.frugs.filesync.local.LocalFileUpdater;
import com.frugs.filesync.local.LockingDiff;
import com.frugs.filesync.local.system.FileWriter;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.local.system.SystemCommandFacade;
import com.frugs.filesync.remote.RemoteFileUpdateReceiver;
import com.frugs.filesync.remote.RemoteFileUpdateSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class FileSyncModule {
    public final RemoteFileUpdateReceiver remoteFileUpdateReceiver;
    public final LocalFileUpdatePollingService localFileUpdatePollingService;

    private static final Logger logger = LoggerFactory.getLogger(FileSyncModule.class);

    private FileSyncModule(RemoteFileUpdateReceiver remoteFileUpdateReceiver, LocalFileUpdatePollingService localFileUpdatePollingService) {
        this.remoteFileUpdateReceiver = remoteFileUpdateReceiver;
        this.localFileUpdatePollingService = localFileUpdatePollingService;
    }

    public static FileSyncModule createModule(FileSyncConfig config) throws IOException {

        logger.debug("configured to listen on port: " + config.getLocalPort());
        logger.debug("configured send updates to: " + config.getRemoteHost());

        SystemCommandExecutor systemCommandExecutor = new SystemCommandExecutor();
        FileWriter fileWriter = new FileWriter();
        SystemCommandFacade systemCommandFacade = new SystemCommandFacade(systemCommandExecutor, fileWriter);

        FileUpdateFacade fileUpdateFacade = new FileUpdateFacade(systemCommandFacade);
        LockingDiff lockedDiff = new LockingDiff(new ReentrantLock(), systemCommandFacade.gitDiffHead());

        LocalFileUpdater localFileUpdater = new LocalFileUpdater(lockedDiff, fileUpdateFacade);
        RemoteFileUpdateSender remoteFileUpdateSender = new RemoteFileUpdateSender(config.getRemoteHost());

        LocalFileUpdatePollingService localFileUpdatePollingService = new LocalFileUpdatePollingService(lockedDiff, fileUpdateFacade, remoteFileUpdateSender);
        RemoteFileUpdateReceiver remoteFileUpdateReceiver = new RemoteFileUpdateReceiver(config.getLocalPort(), localFileUpdater);

        return new FileSyncModule(remoteFileUpdateReceiver, localFileUpdatePollingService);
    }
}
