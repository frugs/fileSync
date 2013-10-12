package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockingDiffTest {
    @Mock private Lock mockLock;
    @Mock private Diff mockDiff;
    private LockingDiff lockingDiff;

    @Before
    public void setUp() {
        lockingDiff = new LockingDiff(mockLock, mockDiff);
    }

    @Test
    public void lockThenGet_waits_to_acquire_lock_before_returning_diff() throws Exception {
        when(mockLock.tryLock(anyInt(), (TimeUnit) any())).thenReturn(true);

        Diff result = lockingDiff.lockThenGet();
        verify(mockLock).tryLock(10, SECONDS);
        assertTrue(result == mockDiff);
    }

    @Test(expected = TimeoutException.class)
    public void lockThenGet_throws_exception_on_timeout() throws Exception {
        when(mockLock.tryLock(anyInt(), (TimeUnit) any())).thenReturn(false);

        lockingDiff.lockThenGet();
    }

    @Test
    public void lock_tries_to_acquire_lock() throws Exception {
        when(mockLock.tryLock(anyInt(), (TimeUnit) any())).thenReturn(true);

        lockingDiff.lock();
        verify(mockLock).tryLock(10, SECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void lock_throws_exception_on_timeout() throws Exception {
        when(mockLock.tryLock(anyInt(), (TimeUnit) any())).thenReturn(false);

        lockingDiff.lock();
        verify(mockLock).tryLock(10, SECONDS);
    }

    @Test
    public void unlock_releases_lock() {
        lockingDiff.unlock();
        verify(mockLock).unlock();
    }
}
