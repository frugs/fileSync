package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class RemoteFileUpdateSender {

    private final InetAddress remoteAddress;
    private final int port;
    private final Logger logger;

    public RemoteFileUpdateSender(InetAddress remoteAddress, int port, Logger logger) {
        this.remoteAddress = remoteAddress;
        this.port = port;
        this.logger = logger;
    }

    public void sendUpdates(Diff updates) throws IOException {
        logger.info("sending updates");
        Socket socket = new Socket(remoteAddress, port);
        socket.getOutputStream().write(updates.toByteArray());
        socket.close();
    }
}
