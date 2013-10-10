package com.frugs.filesync.domain;

public class DiffBuilder {
    private byte[] content = "arbitrary data".getBytes();

    public static DiffBuilder aDiff() {
        return new DiffBuilder();
    }

    public Diff build() {
        return new Diff(content);
    }

    public DiffBuilder withContent(String contentAsString) {
        this.content = contentAsString.getBytes();
        return this;
    }
}
