package com.frugs.filesync.service;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;
import com.frugs.filesync.remote.RemoteFileUpdateReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static java.net.InetAddress.getLocalHost;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteFileUpdateReceiverTest {
    private final static int port = 49803;
    @Mock private LocalFileUpdater mockLocalFileUpdater;
    @Mock private Logger logger;

    private RemoteFileUpdateReceiver remoteFileUpdateReceiver;

    @Before
    public void setUp() throws Exception {
        remoteFileUpdateReceiver = new RemoteFileUpdateReceiver(port, mockLocalFileUpdater, logger);
    }

    @Test
    public void acceptUpdates_shouldAcceptIncomingDiffStreams_andUpdateLocalFiles() throws Exception {
        final Object waitHandle = new Object();
        Runnable acceptUpdates = new Runnable() {
            @Override public void run() {
                try {
                    remoteFileUpdateReceiver.acceptUpdates();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                synchronized (waitHandle) {
                    waitHandle.notify();
                }
            }
        };
        new Thread(acceptUpdates).start();
        Thread.sleep(1000);

        Diff incomingDiff = aDiff().build();

        Socket socket = new Socket(getLocalHost(), port);
        socket.getOutputStream().write(incomingDiff.toByteArray());
        socket.close();

        synchronized (waitHandle) {
            waitHandle.wait();
        }
        verify(mockLocalFileUpdater).updateLocalFiles(incomingDiff);
    }
}
