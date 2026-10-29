package com.societyapp.application.port.out;

import java.io.IOException;
import java.io.InputStream;

public interface UploadedFile {
    String originalFilename();

    String contentType();

    long size();

    boolean empty();

    InputStream openStream() throws IOException;
}
