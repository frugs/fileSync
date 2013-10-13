package com.frugs.filesync;

import java.net.InetSocketAddress;

public interface FileSyncConfig {
    public InetSocketAddress getRemoteHost();
    public int getLocalPort();
}
