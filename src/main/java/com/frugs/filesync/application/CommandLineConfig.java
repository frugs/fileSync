package com.frugs.filesync.application;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.frugs.filesync.FileSyncConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class CommandLineConfig implements FileSyncConfig {

    public static final int DEFAULT_PORT = 40987;

    @Parameter(required = true, description = "[host]", converter = InetSocketAddressConverter.class)
    private List<InetSocketAddress> remoteAddress = new ArrayList<>();

    @Parameter(names = { "-p", "--local-port" },
        required = false,
        description = "local port to listen for updates upon"
    )
    private int localPort = DEFAULT_PORT;

    @Parameter(names = { "-h", "--help" }, required = false, description = "displays this help")
    private Boolean help;

    public boolean hadHelpSwitch() {
        return help != null && help;
    }

    @Override public InetSocketAddress getRemoteHost() {
        return remoteAddress.get(0);
    }

    @Override public int getLocalPort() {
        return localPort;
    }

    public static class InetSocketAddressConverter implements IStringConverter<InetSocketAddress> {
        @Override public InetSocketAddress convert(String argument) {
            String[] remoteHostNameAndPort = argument.split(":");
            String remoteHostName = remoteHostNameAndPort[0];

            return remoteHostNameAndPort.length == 2 ?
                new InetSocketAddress(remoteHostName, parseInt(remoteHostNameAndPort[1])):
                new InetSocketAddress(remoteHostName, DEFAULT_PORT);
        }
    }
}
