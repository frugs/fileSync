package com.frugs.filesync;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdatePollingService;
import com.frugs.filesync.local.LocalFileUpdater;
import com.frugs.filesync.local.LockedDiff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.RemoteFileUpdateReceiver;
import com.frugs.filesync.remote.RemoteFileUpdateSender;
import com.frugs.filesync.task.PollLocalUpdatesTask;
import javafx.concurrent.Task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.net.InetAddress.getLocalHost;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileSync {
    public static void main(String[] args) {

        //TODO fix this up a bit later, just trying to see whether it works
        try {
            InetAddress remoteAddress = getLocalHost();
            int port = 45903;

            SystemCommandExecutor systemCommandExecutor = new SystemCommandExecutor();
            LockedDiff lockedDiff = new LockedDiff(new ReentrantLock(), Diff.emptyDiff);

            LocalFileUpdater localFileUpdater = new LocalFileUpdater(lockedDiff, systemCommandExecutor);
            RemoteFileUpdateSender remoteFileUpdateSender = new RemoteFileUpdateSender(remoteAddress, port);

            LocalFileUpdatePollingService localFileUpdatePollingService = new LocalFileUpdatePollingService(systemCommandExecutor, lockedDiff, remoteFileUpdateSender);
            RemoteFileUpdateReceiver remoteFileUpdateReceiver = new RemoteFileUpdateReceiver(remoteAddress, port, localFileUpdater);

            Runnable pollLocalUpdatesTask = new PollLocalUpdatesTask(localFileUpdatePollingService);
            startInSingleRepeatingThread(pollLocalUpdatesTask, 500, MILLISECONDS);

            while (true) {
                remoteFileUpdateReceiver.acceptUpdates();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ScheduledFuture startInSingleRepeatingThread(Runnable task, int repeating, TimeUnit unit) {
        return Executors.newScheduledThreadPool(1).scheduleAtFixedRate(task, 5000, repeating, unit);
    }
}
