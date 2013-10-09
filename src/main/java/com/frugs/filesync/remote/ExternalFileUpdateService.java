package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdateService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ExternalFileUpdateService {

    private final static int port = 49803;
    private final InetAddress remoteAddress;
    private final LocalFileUpdateService localFileUpdateService;

    @Inject
    public ExternalFileUpdateService(InetAddress remoteAddress, LocalFileUpdateService localFileUpdateService) {
        this.remoteAddress = remoteAddress;
        this.localFileUpdateService = localFileUpdateService;
    }

    public void acceptUpdates() throws IOException {
        Socket socket = new ServerSocket(port).accept();
        Diff update = Diff.fromInputStream(socket.getInputStream());
        localFileUpdateService.updateLocalFiles(update);
        socket.close();
    }

    public void sendUpdates(Diff updates) throws IOException {
        Socket socket = new Socket(remoteAddress, port);
        socket.getOutputStream().write(updates.toByteArray());
        socket.close();
    }
}