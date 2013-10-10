package com.frugs.filesync.service;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdateService;
import com.frugs.filesync.remote.ExternalFileUpdateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static java.net.InetAddress.getLocalHost;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExternalFileUpdateServiceTest {
    private final static int port = 49803;
    @Mock private LocalFileUpdateService mockLocalFileUpdateService;

    private static InetAddress getRemoteAddress() {
        try {
            return getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private ExternalFileUpdateService externalFileUpdateService;

    @Before
    public void setUp() {
        externalFileUpdateService = new ExternalFileUpdateService(getRemoteAddress(), mockLocalFileUpdateService);
    }

    @Test
    public void acceptUpdates_shouldAcceptIncomingDiffStreams_andUpdateLocalFiles() throws IOException, InterruptedException {
        final Object waitHandle = new Object();
        Runnable acceptUpdates = new Runnable() {
            @Override public void run() {
                try {
                    externalFileUpdateService.acceptUpdates();
                } catch (IOException e) {
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
        verify(mockLocalFileUpdateService).updateLocalFiles(incomingDiff);
    }

    @Test
    public void sendUpdates_shouldSendDiffToRemote() throws ExecutionException, InterruptedException, IOException {
        Callable<Diff> accept = new Callable<Diff>() {
            @Override public Diff call() {
                try {
                    Socket socket = new ServerSocket(port).accept();
                    Diff diff = Diff.fromInputStream(socket.getInputStream());
                    socket.close();
                    return diff;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        FutureTask<Diff> acceptTask = new FutureTask<Diff>(accept);
        Executors.newCachedThreadPool().submit(acceptTask);

        Thread.sleep(1000);

        Diff expectedDiff = aDiff().build();
        externalFileUpdateService.sendUpdates(expectedDiff);

        Diff result = acceptTask.get();
        assertTrue(expectedDiff.equals(result));
    }
}
