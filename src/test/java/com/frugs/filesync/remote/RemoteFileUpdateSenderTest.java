package com.frugs.filesync.remote;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static java.net.InetAddress.getLocalHost;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RemoteFileUpdateSenderTest {

    private static final int port = 48939;
    private RemoteFileUpdateSender remoteFileUpdateReceiver;

    @Before
    public void setUp() throws Exception {
        remoteFileUpdateReceiver = new RemoteFileUpdateSender(getLocalHost(), port);
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
        remoteFileUpdateReceiver.sendUpdates(expectedDiff);

        Diff result = acceptTask.get();
        assertTrue(expectedDiff.equals(result));
    }
}
