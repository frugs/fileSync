package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteFileUpdateReceiver {

    private final int port;
    private final LocalFileUpdater localFileUpdater;

    public RemoteFileUpdateReceiver(int port, LocalFileUpdater localFileUpdater) {
        this.port = port;
        this.localFileUpdater = localFileUpdater;
    }

    public void acceptUpdates() throws IOException {
        Socket socket = new ServerSocket(port).accept();
        Diff update = Diff.fromInputStream(socket.getInputStream());
        localFileUpdater.updateLocalFiles(update);
        socket.close();
    }
}