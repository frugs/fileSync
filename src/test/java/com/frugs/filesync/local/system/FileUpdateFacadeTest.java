package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static com.frugs.filesync.domain.Diff.emptyDiff;
import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static com.frugs.filesync.domain.DiffMatchers.hasContent;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileUpdateFacadeTest {
    @Mock private SystemCommandExecutor mockSystemCommandExecutor;

    private FileUpdateFacade fileUpdateFacade;

    @Before
    public void setUp() throws Exception {
        fileUpdateFacade = new FileUpdateFacade(mockSystemCommandExecutor);
    }

    @Test
    public void getCurrentState_delegates_to_commandExecutor_and_returns_result_as_diff() throws IOException {
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("result"));

        Diff result = fileUpdateFacade.getCurrentState();
        assertThat(result, hasContent("result"));
    }

    @Test
    public void applyDiff_delegates_to_commandExecutor() throws IOException {
        fileUpdateFacade.applyDiff(aDiff().withContent("content").build());
        verify(mockSystemCommandExecutor).gitApply("content");
    }

    @Test
    public void applyDiff_does_nothing_given_empty_diff() throws IOException {
        fileUpdateFacade.applyDiff(emptyDiff);
        verifyNoMoreInteractions(mockSystemCommandExecutor);
    }

    @Test
    public void interDiff_delegates_to_commandExecutor() throws IOException {
        Diff firstDiff = aDiff().withContent("firstDiff").build();
        Diff secondDiff = aDiff().withContent("secondDiff").build();
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream("interDiff"));

        Diff result = fileUpdateFacade.interDiff(firstDiff, secondDiff);
        assertThat(result, hasContent("interDiff"));
        verify(mockSystemCommandExecutor).interDiff("firstDiff", "secondDiff");
    }

    @Test
    public void interDiff_returns_secondDiff_given_firstDiff_isEmpty() throws IOException {
        Diff secondDiff = aDiff().withContent("secondDiff").build();

        Diff result = fileUpdateFacade.interDiff(emptyDiff, secondDiff);
        assertThat(result, hasContent("secondDiff"));
        verifyNoMoreInteractions(mockSystemCommandExecutor);
    }

    @Test
    public void interDiff_returns_firstDiff_given_secondDiff_isEmpty() throws IOException {
        Diff firstDiff = aDiff().withContent("firstDiff").build();

        Diff result = fileUpdateFacade.interDiff(firstDiff, emptyDiff);
        assertThat(result, hasContent("firstDiff"));
        verifyNoMoreInteractions(mockSystemCommandExecutor);
    }
}
