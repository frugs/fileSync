package com.frugs.filesync.domain;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Diff {
    private byte[] data;

    public Diff(byte[] bytes) {
        this.data = bytes;
    }

    public byte[] toByteArray() {
        return data;
    }

    public static Diff fromInputStream(InputStream inputStream) throws IOException {
        return new Diff(IOUtils.toByteArray(inputStream));
    }

    public static final Diff emptyDiff = new Diff("".getBytes());

    @Override public boolean equals(Object obj) {
        return obj instanceof Diff && Arrays.equals(((Diff) obj).data, this.data);
    }

    @Override public String toString() {
        return new String(data);
    }

    public boolean hasChanges() {
        return data.length != 0;
    }

    public boolean isEmpty() {
        return data.length == 0;
    }
}
