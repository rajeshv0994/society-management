package com.societyapp.application.model;

import java.io.InputStream;
import java.util.Objects;

/** Stream-based binary content that does not expose an adapter's filesystem representation. */
public record BinaryContent(InputStream stream, long length) {
    public BinaryContent {
        Objects.requireNonNull(stream, "stream");
        if (length < 0) throw new IllegalArgumentException("length must not be negative");
    }
}
