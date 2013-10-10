package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.system.SystemCommandExecutor;
import com.frugs.filesync.remote.ExternalFileUpdateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileUpdateServiceTest {
    @Mock private SystemCommandExecutor mockSystemCommandExecutor;
    @Mock private ExternalFileUpdateService mockExternalFileUpdateService;
    @Mock private LockedDiff mockPreviousState;
    private LocalFileUpdateService localFileUpdateService;

    @Before
    public void setUp() {
        localFileUpdateService = new LocalFileUpdateService(mockSystemCommandExecutor, mockExternalFileUpdateService, mockPreviousState);
    }

    @Test
    public void updateLocalFiles_delegates_to_commandExecutor() throws IOException {
        Diff updates = aDiff().withContent("updates").build();
        Diff previous = aDiff().withContent("previous").build();
        when(mockPreviousState.retrieve()).thenReturn(previous);
        when(mockSystemCommandExecutor.combineDiff("previous", "updates")).thenReturn(toInputStream("blah"));

        localFileUpdateService.updateLocalFiles(updates);
        verify(mockSystemCommandExecutor).gitApply("updates");
    }

    @Test
    public void updateLocalFiles_updates_previous_state_to_current_state() throws IOException {
        when(mockPreviousState.retrieve()).thenReturn(aDiff().build());
        when(mockSystemCommandExecutor.combineDiff(anyString(), anyString())).thenReturn(toInputStream("combined"));
        localFileUpdateService.updateLocalFiles(aDiff().build());

        ArgumentCaptor<Diff> captor = ArgumentCaptor.forClass(Diff.class);
        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockPreviousState).set(captor.capture());
        inOrder.verify(mockPreviousState).putBack();

        Diff updatedDiff = captor.getValue();
        assertThat(updatedDiff.toString(), is("combined"));
    }

    @Test
    public void pollForLocalFileUpdates_does_nothing_if_interDiff_is_empty() throws IOException {
        Diff unchangedDiff = aDiff()
            .withContent("no changes")
            .build();
        when(mockPreviousState.retrieve()).thenReturn(unchangedDiff);
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("no changes"));
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream(""));

        localFileUpdateService.pollForLocalFileUpdates();

        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockPreviousState).putBack();
        verifyNoMoreInteractions(mockExternalFileUpdateService);
    }

    @Test
    public void pollForLocalFileUpdates_sends_updates_if_interDiff_hasChanges() throws IOException {
        when(mockPreviousState.retrieve()).thenReturn(aDiff().build());
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("Current Changes"));
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream("the difference"));

        localFileUpdateService.pollForLocalFileUpdates();

        ArgumentCaptor<Diff> savedDiffCaptor = ArgumentCaptor.forClass(Diff.class);
        ArgumentCaptor<Diff> sentDiffCaptor = ArgumentCaptor.forClass(Diff.class);

        InOrder inOrder = inOrder(mockPreviousState, mockExternalFileUpdateService);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockExternalFileUpdateService).sendUpdates(sentDiffCaptor.capture());
        inOrder.verify(mockPreviousState).set(savedDiffCaptor.capture());
        inOrder.verify(mockPreviousState).putBack();

        Diff savedDiff = savedDiffCaptor.getValue();
        assertThat(savedDiff.toString(), is("Current Changes"));

        Diff sentDiff = sentDiffCaptor.getValue();
        assertThat(sentDiff.toString(), is("the difference"));
    }
}
