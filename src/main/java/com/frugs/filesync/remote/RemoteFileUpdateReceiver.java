package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class RemoteFileUpdateReceiver {

    private final int port;
    private final LocalFileUpdater localFileUpdater;
    private final Logger logger;

    public RemoteFileUpdateReceiver(int port, LocalFileUpdater localFileUpdater, Logger logger) {
        this.port = port;
        this.localFileUpdater = localFileUpdater;
        this.logger = logger;
    }

    public void acceptUpdates() throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        Diff update = Diff.fromInputStream(socket.getInputStream());

        logger.info("received updates, they are:\n" + update.toString());
        localFileUpdater.updateLocalFiles(update);

        serverSocket.close();
    }
}