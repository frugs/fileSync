package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
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

import static com.frugs.filesync.domain.Diff.emptyDiff;
import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileUpdatePollingServiceTest {
    @Mock private FileUpdateFacade mockFileUpdateFacade;
    @Mock private RemoteFileUpdateSender mockRemoteFileUpdateSender;
    @Mock private LockedDiff mockPreviousState;
    @Mock private Logger logger;
    private LocalFileUpdatePollingService localFileUpdatePollingService;

    @Before
    public void setUp() {
        localFileUpdatePollingService = new LocalFileUpdatePollingService(mockPreviousState, mockFileUpdateFacade, mockRemoteFileUpdateSender, logger);
    }

    @Test
    public void pollForLocalFileUpdates_does_nothing_if_interDiff_is_empty() throws Exception {
        when(mockFileUpdateFacade.interDiff((Diff) any(), (Diff) any())).thenReturn(emptyDiff);

        localFileUpdatePollingService.pollForLocalFileUpdates();

        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockPreviousState).putBack();
        verifyNoMoreInteractions(mockRemoteFileUpdateSender);
    }

    @Test
    public void pollForLocalFileUpdates_sends_updates_if_interDiff_hasChanges() throws Exception {
        Diff currentChanges = aDiff().withContent("Current Changes").build();
        Diff difference = aDiff().withContent("non-empty").build();
        when(mockFileUpdateFacade.getCurrentState()).thenReturn(currentChanges);
        when(mockFileUpdateFacade.interDiff((Diff) any(), (Diff) any())).thenReturn(difference);

        localFileUpdatePollingService.pollForLocalFileUpdates();

        ArgumentCaptor<Diff> savedDiffCaptor = ArgumentCaptor.forClass(Diff.class);
        ArgumentCaptor<Diff> sentDiffCaptor = ArgumentCaptor.forClass(Diff.class);

        InOrder inOrder = inOrder(mockPreviousState, mockRemoteFileUpdateSender);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockRemoteFileUpdateSender).sendUpdates(sentDiffCaptor.capture());
        inOrder.verify(mockPreviousState).set(savedDiffCaptor.capture());
        inOrder.verify(mockPreviousState).putBack();

        Diff savedDiff = savedDiffCaptor.getValue();
        assertThat(savedDiff, is(currentChanges));

        Diff sentDiff = sentDiffCaptor.getValue();
        assertThat(sentDiff, is(difference));
    }
}
