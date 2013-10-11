package com.frugs.filesync.local.system;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.nio.file.Files.write;

public class FileWriter {
    public File writeBytesToFile(byte[] bytes) throws IOException {
        File tempFile = new File(".fileSync/" + UUID.randomUUID());
        tempFile.getParentFile().mkdirs();
        write(tempFile.toPath(), bytes);
        return tempFile;
    }
}
