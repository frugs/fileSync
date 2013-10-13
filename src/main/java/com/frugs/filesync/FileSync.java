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
import com.frugs.filesync.task.PollLocalUpdatesTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.frugs.filesync.FileSyncModule.createModule;
import static java.lang.Integer.parseInt;
import static java.net.InetAddress.getLocalHost;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileSync {

public static void main(String[] args) {

        //TODO fix this up a bit later, just trying to see whether it works
        try {
            InetAddress remoteAddress = getLocalHost();
            int localPort = parseInt(args[0]);
            int remotePort = parseInt(args[1]);
            InetSocketAddress remoteHost = new InetSocketAddress(remoteAddress, remotePort);

            FileSyncModule module = createModule(remoteHost, localPort);

            Runnable pollLocalUpdatesTask = new PollLocalUpdatesTask(module.localFileUpdatePollingService);
            startInSingleRepeatingThread(pollLocalUpdatesTask, 500, MILLISECONDS);

            while (true) {
                module.remoteFileUpdateReceiver.acceptUpdates();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ScheduledFuture startInSingleRepeatingThread(Runnable task, int repeating, TimeUnit unit) {
        return Executors.newScheduledThreadPool(1).scheduleAtFixedRate(task, 5000, repeating, unit);
    }
}
