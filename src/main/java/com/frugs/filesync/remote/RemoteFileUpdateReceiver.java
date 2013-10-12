package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

public class RemoteFileUpdateReceiver {

    private final int port;
    private final LocalFileUpdater localFileUpdater;
    private final Logger logger;

    public RemoteFileUpdateReceiver(int port, LocalFileUpdater localFileUpdater) {
        this.port = port;
        this.localFileUpdater = localFileUpdater;
        this.logger = LoggerFactory.getLogger(LocalFileUpdater.class);
    }

    public void acceptUpdates() throws IOException, InterruptedException, TimeoutException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        Diff update = Diff.fromInputStream(socket.getInputStream());

        logger.debug("received updates, they are:\n" + update.toString());
        localFileUpdater.updateLocalFiles(update);

        serverSocket.close();
    }
}