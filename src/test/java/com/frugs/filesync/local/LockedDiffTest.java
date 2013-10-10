package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.locks.Lock;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LockedDiffTest {
    @Mock private Lock mockLock;
    @Mock private Diff mockDiff;
    private LockedDiff lockedDiff;

    @Before
    public void setUp() {
        lockedDiff = new LockedDiff(mockLock, mockDiff);
    }

    @Test
    public void retrieve_waits_to_acquire_lock_before_returning_diff() {
        Diff result = lockedDiff.retrieve();
        verify(mockLock).lock();
        assertTrue(result == mockDiff);
    }

    @Test
    public void set_waits_to_acquire_lock_before_setting_diff() {
        Diff expectedDiff = aDiff().build();
        lockedDiff.set(expectedDiff);

        verify(mockLock).lock();
        assertTrue(lockedDiff.retrieve() == expectedDiff);
    }

    @Test
    public void putBack_releases_lock() {
        lockedDiff.putBack();
        verify(mockLock).unlock();
    }
}
