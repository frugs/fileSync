package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;
import com.frugs.filesync.local.LocalFileUpdater;
import com.frugs.filesync.local.LockingDiff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static com.frugs.filesync.domain.DiffMatchers.hasContent;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileUpdaterTest {
    @Mock private LockingDiff mockPreviousState;
    @Mock private com.frugs.filesync.local.FileUpdateFacade mockFileUpdateFacade;

    private LocalFileUpdater localFileUpdater;

    @Before
    public void setUp() {
        localFileUpdater = new LocalFileUpdater(mockPreviousState, mockFileUpdateFacade);
    }

    @Test
    public void updateLocalFiles_delegates_to_commandExecutor() throws Exception {
        when(mockFileUpdateFacade.getCurrentState()).thenReturn(aDiff().build());

        Diff updates = aDiff().withContent("updates").build();
        localFileUpdater.updateLocalFiles(updates);
        verify(mockFileUpdateFacade).applyDiff(updates);
    }

    @Test
    public void updateLocalFiles_updates_previous_state_to_current_state() throws Exception {
        when(mockFileUpdateFacade.getCurrentState()).thenReturn(aDiff().withContent("new diff").build());

        localFileUpdater.updateLocalFiles(aDiff().build());

        ArgumentCaptor<Diff> captor = ArgumentCaptor.forClass(Diff.class);
        InOrder inOrder = inOrder(mockPreviousState);
        inOrder.verify(mockPreviousState).lock();
        inOrder.verify(mockPreviousState).set(captor.capture());
        inOrder.verify(mockPreviousState).unlock();

        Diff updatedDiff = captor.getValue();
        assertThat(updatedDiff, hasContent("new diff"));
    }
}
