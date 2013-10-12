package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteFileUpdateSender {

    private final InetSocketAddress remoteHost;
    private final Logger logger;

    public RemoteFileUpdateSender(InetSocketAddress remoteHost) {
        this.remoteHost = remoteHost;
        this.logger = LoggerFactory.getLogger(RemoteFileUpdateSender.class);
    }

    public void sendUpdates(Diff updates) throws IOException {
        logger.debug("sending updates");

        Socket socket = new Socket(remoteHost.getAddress(), remoteHost.getPort());
        socket.getOutputStream().write(updates.toByteArray());
        socket.close();
    }
}
