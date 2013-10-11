package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.frugs.filesync.domain.DiffBuilder.aDiff;
import static java.util.concurrent.TimeUnit.SECONDS;
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
    public void retrieve_waits_to_acquire_lock_before_returning_diff() throws Exception {
        Diff result = lockedDiff.retrieve();
        verify(mockLock).tryLock(10, SECONDS);
        assertTrue(result == mockDiff);
    }

    @Test
    public void set_waits_to_acquire_lock_before_setting_diff() throws Exception {
        Diff expectedDiff = aDiff().build();
        lockedDiff.set(expectedDiff);

        verify(mockLock).tryLock(10, SECONDS);
        assertTrue(lockedDiff.retrieve() == expectedDiff);
    }

    @Test
    public void putBack_releases_lock() {
        lockedDiff.putBack();
        verify(mockLock).unlock();
    }
}
