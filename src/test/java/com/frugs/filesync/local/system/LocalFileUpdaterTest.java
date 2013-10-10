package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;
import com.frugs.filesync.local.LockedDiff;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileUpdaterTest {
    @Mock private LockedDiff mockPreviousState;
    @Mock private SystemCommandExecutor mockSystemCommandExecutor;

    private LocalFileUpdater localFileUpdater;

    @Before
    public void setUp() {
        localFileUpdater = new LocalFileUpdater(mockPreviousState, mockSystemCommandExecutor);
    }

    @Test
    public void updateLocalFiles_delegates_to_commandExecutor() throws IOException {
        Diff updates = aDiff().withContent("updates").build();
        Diff previous = aDiff().withContent("previous").build();
        when(mockPreviousState.retrieve()).thenReturn(previous);
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("blah"));

        localFileUpdater.updateLocalFiles(updates);
        verify(mockSystemCommandExecutor).gitApply("updates");
    }

    @Test
    public void updateLocalFiles_updates_previous_state_to_current_state() throws IOException {
        when(mockPreviousState.retrieve()).thenReturn(aDiff().build());
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("combined"));
        localFileUpdater.updateLocalFiles(aDiff().build());

        ArgumentCaptor<Diff> captor = ArgumentCaptor.forClass(Diff.class);
        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).retrieve();
        inOrder.verify(mockPreviousState).set(captor.capture());
        inOrder.verify(mockPreviousState).putBack();

        Diff updatedDiff = captor.getValue();
        assertThat(updatedDiff.toString(), is("combined"));
    }
}
