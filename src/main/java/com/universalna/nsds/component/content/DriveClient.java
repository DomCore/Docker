package com.universalna.nsds.component.content;

import java.io.InputStream;
import java.util.Optional;

interface DriveClient {

    String uploadToDrive(final InputStream content, final String originalFileName);

    Optional<InputStream> download(String fileStorageFileId);

    Optional<Long> getContentLength(String fileStorageFileId);

    Optional<String> createEditableUrl(String fileStorageFileId);

    void delete(String fileStorageFileId);
}
