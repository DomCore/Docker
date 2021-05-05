package com.universalna.nsds.component.content;

import java.io.InputStream;

interface BlobClient {

    InputStream getContent(final String reference);

    void putContent(final String id, final InputStream content);

    Long getContentLength(final String id);

    void deleteContent(final String id);
}
