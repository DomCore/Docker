package com.universalna.nsds.controller;

import com.universalna.nsds.model.File;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.IOException;
import java.io.OutputStream;

public interface Zipper {

    default void zipFiles(Iterable<File> files, final OutputStream outputStream) {
        try {
            final ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, outputStream);
            for (final File file : files) {
                final String fileName = file.getOriginalName();
                final ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
                archive.putArchiveEntry(entry);
                file.getContent().transferTo(archive);
                archive.closeArchiveEntry();
            }
            archive.finish();
            archive.close();
        } catch (IOException | ArchiveException e) {
            throw new RuntimeException(e);
        }
    }
}
