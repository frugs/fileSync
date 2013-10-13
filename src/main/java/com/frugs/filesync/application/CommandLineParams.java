package com.frugs.filesync.application;

import com.beust.jcommander.Parameter;
import com.frugs.filesync.FileSyncConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class CommandLineParams {

    public static final int DEFAULT_PORT = 40987;

    @Parameter(required = true, description = "[host]")
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "-p", "--local-port" },
        required = false,
        description = "local port to listen for updates upon"
    )
    private int localPort = DEFAULT_PORT;

    @Parameter(names = { "-h", "--help" }, required = false, description = "displays this help")
    private Boolean help;

    public FileSyncConfig toConfig() {
        InetSocketAddress remoteHost;

        String[] remoteHostNameAndPort = parameters.get(0).split(":");
        String remoteHostName = remoteHostNameAndPort[0];

        if (remoteHostNameAndPort.length == 2) {
            int remotePort = parseInt(remoteHostNameAndPort[1]);
            remoteHost = new InetSocketAddress(remoteHostName, remotePort);
        } else {
            remoteHost = new InetSocketAddress(remoteHostName, DEFAULT_PORT);
        }

        return new FileSyncConfig(remoteHost, localPort);
    }

    public boolean hadHelpSwitch() {
        return help != null && help;
    }
}
