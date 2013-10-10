package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteFileUpdateReceiver {

    private final InetAddress remoteAddress;
    private final int port;
    private final LocalFileUpdater localFileUpdater;

    public RemoteFileUpdateReceiver(InetAddress remoteAddress, int port, LocalFileUpdater localFileUpdater) {
        this.remoteAddress = remoteAddress;
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