package com.frugs.filesync.application;

import com.beust.jcommander.JCommander;
import com.frugs.filesync.task.PollLocalUpdatesTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileSync {

    public static void main(String[] args) {

        CommandLineParams commandLineParams = new CommandLineParams();
        JCommander commander = new JCommander(commandLineParams);
        commander.parse(args);

        if (commandLineParams.hadHelpSwitch()) {
            commander.usage();
            return;
        }

        try {
            FileSyncConfig config = commandLineParams.toConfig();
            FileSyncModule module = FileSyncModule.createModule(config);

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
