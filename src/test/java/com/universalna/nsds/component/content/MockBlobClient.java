package com.universalna.nsds.component.content;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.universalna.nsds.TestConstants.FILE_CONTENT;

@Component
@Primary
public class MockBlobClient implements BlobClient {

    @Override
    public InputStream getContent(final String reference) {
        return new ByteArrayInputStream(FILE_CONTENT.get());
    }

    @Override
    public void putContent(final String id, final InputStream content) {
        // do nothing
    }

    @Override
    public Long getContentLength(final String id) {
        return (long) FILE_CONTENT.get().length;
    }

    @Override
    public void deleteContent(final String id) {
        // do nothing
    }
}
