package com.frugs.filesync.local.system;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static com.frugs.filesync.domain.DiffMatchers.hasContent;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandFacadeTest {

    @Mock private SystemCommandExecutor mockSystemCommandExecutor;
    @Mock private FileWriter mockFileWriter;
    private SystemCommandFacade systemCommandFacade;

    @Before
    public void setUp() throws Exception {
        systemCommandFacade = new SystemCommandFacade(mockSystemCommandExecutor, mockFileWriter);
    }

    @Test
    public void gitDiffHead_delegates_to_commandExecutor_and_converts_to_diff() throws IOException {
        when(mockSystemCommandExecutor.gitDiffHead()).thenReturn(toInputStream("expected content"));

        Diff result = systemCommandFacade.gitDiffHead();
        assertThat(result, hasContent("expected content"));
    }

    @Test
    public void interDiff_writes_diffs_to_files_then_delegates_to_commandExecutor_and_converts_to_diff() throws IOException {
        when(mockFileWriter.writeBytesToFile((byte[]) any()))
            .thenReturn(new File("firstDiffFile"))
            .thenReturn(new File("secondDiffFile"));
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream("interDiff"));

        Diff result = systemCommandFacade.interDiff(aDiff().build(), aDiff().build());

        assertThat(result, hasContent("interDiff"));
        verify(mockSystemCommandExecutor).interDiff("firstDiffFile", "secondDiffFile");
    }

    @Test
    public void interDiff_clears_temporary_files_when_done() throws IOException {
        File mockFirstFile = mock(File.class);
        File mockSecondFile = mock(File.class);

        when(mockFileWriter.writeBytesToFile((byte[]) any()))
            .thenReturn(mockFirstFile)
            .thenReturn(mockSecondFile);
        when(mockSystemCommandExecutor.interDiff(anyString(), anyString())).thenReturn(toInputStream(""));

        systemCommandFacade.interDiff(aDiff().build(), aDiff().build());

        InOrder inOrder = inOrder(mockSystemCommandExecutor, mockFirstFile, mockSecondFile);

        inOrder.verify(mockSystemCommandExecutor).interDiff(anyString(), anyString());
        inOrder.verify(mockFirstFile).delete();
        inOrder.verify(mockSecondFile).delete();
    }

    @Test
    public void gitApply_writes_diff_to_file_then_delegates_to_commandExecutor() throws IOException {
        when(mockFileWriter.writeBytesToFile((byte[]) any())).thenReturn(new File("diffFile"));

        systemCommandFacade.gitApply(aDiff().withContent("diff").build());
        verify(mockFileWriter).writeBytesToFile("diff".getBytes());
        verify(mockSystemCommandExecutor).gitApply("diffFile");
    }

    @Test
    public void gitApply_clears_temporary_files_when_done() throws IOException {
        File mockDiffFile = mock(File.class);
        when(mockFileWriter.writeBytesToFile((byte[]) any()))
            .thenReturn(mockDiffFile);

        systemCommandFacade.gitApply(aDiff().build());

        InOrder inOrder = inOrder(mockSystemCommandExecutor, mockDiffFile);
        inOrder.verify(mockSystemCommandExecutor).gitApply(anyString());
        inOrder.verify(mockDiffFile).delete();
    }
}
