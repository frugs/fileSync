package com.frugs.filesync.local;

import com.frugs.filesync.domain.Diff;

import java.util.concurrent.locks.Lock;

public class LockedDiff {
    private final Lock lock;
    private Diff diff;

    public LockedDiff(Lock lock, Diff diff) {
        this.lock = lock;
        this.diff = diff;
    }

    public Diff retrieve() {
        lock.lock();
        return diff;
    }

    public void set(Diff newDiff) {
        lock.lock();
        diff = newDiff;
    }

    public void putBack() {
        lock.unlock();
    }
}
