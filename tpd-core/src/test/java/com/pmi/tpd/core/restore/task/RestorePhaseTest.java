package com.pmi.tpd.core.restore.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.core.restore.RestorePhase;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class RestorePhaseTest extends MockitoTestCase {

    public Path folder;

    @Mock
    private IRunnableTask task;

    @Mock
    private IRestoreState state;

    @InjectMocks
    private RestorePhase.Builder builder;

    @BeforeEach
    public void setUp(@TempDir final Path path) throws IOException {
        this.folder = path;
        when(state.getUnzippedBackupDirectory()).thenReturn(folder.toFile());

        // create some content
        Files.asCharSink(new File(folder.toFile(), "test.txt"), Charsets.UTF_8).write("test content");
    }

    @Test
    public void testCleanup() {
        final RestorePhase phase = builder.add(task, 100).build();

        phase.run();

        // verify that the unzipped backup directory has been deleted
        assertFalse(state.getUnzippedBackupDirectory().exists());
    }
}
