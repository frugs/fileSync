package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.RemoteFileUpdateSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.logging.Logger;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileUpdatePollingServiceTest {
    @Mock private SystemCommandExecutor mockSystemCommandExecutor;
    @Mock private RemoteFileUpdateSender mockRemoteFileUpdateSender;
    @Mock private LockedDiff mockPreviousState;
    @Mock private Logger logger;
    private LocalFileUpdatePollingService localFileUpdatePollingService;

    @Before
    public void setUp() {
        localFileUpdatePollingService = new LocalFileUpdatePollingService(mockSystemCommandExecutor, mockPreviousState, mockRemoteFileUpdateSender, logger);
    }

    @Test
    public void pollForLocalFileUpdates_does_nothing_if_interDiff_is_empty() throws IOException {
        Diff unchangedDiff = aDiff()
            .withContent("no changes")
            .build();
        when(mockPreviousState.retrieve()).thenReturn(unchangedDiff);
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("no changes"));
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream(""));

        localFileUpdatePollingService.pollForLocalFileUpdates();

        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockPreviousState).putBack();
        verifyNoMoreInteractions(mockRemoteFileUpdateSender);
    }

    @Test
    public void pollForLocalFileUpdates_sends_updates_if_interDiff_hasChanges() throws IOException {
        when(mockPreviousState.retrieve()).thenReturn(aDiff().build());
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("Current Changes"));
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream("the difference"));

        localFileUpdatePollingService.pollForLocalFileUpdates();

        ArgumentCaptor<Diff> savedDiffCaptor = ArgumentCaptor.forClass(Diff.class);
        ArgumentCaptor<Diff> sentDiffCaptor = ArgumentCaptor.forClass(Diff.class);

        InOrder inOrder = inOrder(mockPreviousState, mockRemoteFileUpdateSender);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockRemoteFileUpdateSender).sendUpdates(sentDiffCaptor.capture());
        inOrder.verify(mockPreviousState).set(savedDiffCaptor.capture());
        inOrder.verify(mockPreviousState).putBack();

        Diff savedDiff = savedDiffCaptor.getValue();
        assertThat(savedDiff.toString(), is("Current Changes"));

        Diff sentDiff = sentDiffCaptor.getValue();
        assertThat(sentDiff.toString(), is("the difference"));
    }
}
