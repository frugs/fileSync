package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LockedDiff {
    private final Lock lock;
    private Diff diff;

    public LockedDiff(Lock lock, Diff diff) {
        this.lock = lock;
        this.diff = diff;
    }

    public Diff retrieve() throws InterruptedException {
        lock.tryLock(10, SECONDS);
        return diff;
    }

    public void set(Diff newDiff) throws InterruptedException {
        lock.tryLock(10, SECONDS);
        diff = newDiff;
    }

    public void putBack() {
        lock.unlock();
    }
}
