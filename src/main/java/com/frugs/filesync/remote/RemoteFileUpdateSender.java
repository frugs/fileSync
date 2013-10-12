package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteFileUpdateSender {

    private final InetAddress remoteAddress;
    private final int port;
    private final Logger logger;

    public RemoteFileUpdateSender(InetAddress remoteAddress, int port) {
        this.remoteAddress = remoteAddress;
        this.port = port;
        this.logger = LoggerFactory.getLogger(RemoteFileUpdateSender.class);
    }

    public void sendUpdates(Diff updates) throws IOException {
        logger.debug("sending updates");
        Socket socket = new Socket(remoteAddress, port);
        socket.getOutputStream().write(updates.toByteArray());
        socket.close();
    }
}
