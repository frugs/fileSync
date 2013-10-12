package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LockingDiff {
    private final Lock lock;
    private Diff diff;

    public LockingDiff(Lock lock, Diff diff) {
        this.lock = lock;
        this.diff = diff;
    }

    public void lock() throws InterruptedException, TimeoutException {
        boolean timedOut = !lock.tryLock(10, SECONDS);
        if (timedOut) {
            throw new TimeoutException("could not get lock, timed out");
        }
    }

    public Diff lockThenGet() throws InterruptedException, TimeoutException {
        boolean timedOut = !lock.tryLock(10, SECONDS);
        if (timedOut) {
            throw new TimeoutException("could not get lock, timed out");
        }

        return diff;
    }

    public void set(Diff newDiff) throws InterruptedException {
        diff = newDiff;
    }

    public void unlock() {
        lock.unlock();
    }
}
