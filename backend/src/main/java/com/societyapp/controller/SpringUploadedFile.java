package com.societyapp.controller;

import com.societyapp.application.port.out.UploadedFile;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

final class SpringUploadedFile implements UploadedFile {
    private final MultipartFile delegate;

    SpringUploadedFile(MultipartFile delegate) {
        this.delegate = delegate;
    }

    @Override
    public String originalFilename() {
        return delegate.getOriginalFilename();
    }

    @Override
    public String contentType() {
        return delegate.getContentType();
    }

    @Override
    public long size() {
        return delegate.getSize();
    }

    @Override
    public boolean empty() {
        return delegate.isEmpty();
    }

    @Override
    public InputStream openStream() throws IOException {
        return delegate.getInputStream();
    }
}
