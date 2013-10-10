package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteFileUpdateSender {

    private InetAddress remoteAddress;
    private int port;

    public RemoteFileUpdateSender(InetAddress remoteAddress, int port) {
        this.remoteAddress = remoteAddress;
        this.port = port;
    }

    public void sendUpdates(Diff updates) throws IOException {
        Socket socket = new Socket(remoteAddress, port);
        socket.getOutputStream().write(updates.toByteArray());
        socket.close();
    }
}
