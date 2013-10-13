package com.frugs.filesync;

import java.net.InetSocketAddress;

public class FileSyncConfig {

    public final InetSocketAddress remoteHost;
    public final int localPort;

    public FileSyncConfig(InetSocketAddress remoteHost, int localPort) {
        this.remoteHost = remoteHost;
        this.localPort = localPort;
    }
}
