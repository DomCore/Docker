package com.universalna.nsds.component.content;

import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.exception.IoExceptionHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

@Component
class MicrosoftOneDriveClient implements DriveClient, IoExceptionHandler {

    @Autowired
    private IGraphServiceClient driveClient;

    @Autowired
    private UUIDGenerator uuidGenerator;

    @Override
    public String uploadToDrive(final InputStream content, final String originalFileName) {
        final String randomFileName = uuidGenerator.generate().toString();
        final String fileName = randomFileName + "." + FilenameUtils.getExtension(originalFileName);
        DriveItem item = new DriveItem();
        item.name = fileName;
        item.file =  new com.microsoft.graph.models.extensions.File();
        final DriveItem savedItem = driveClient.me().drive().items().buildRequest().post(item);
        driveClient.me().drive().items(savedItem.id).content().buildRequest().put(tryIoOperation(() -> IOUtils.toByteArray(content)));
        return savedItem.id;
    }

    @Override
    public Optional<InputStream> download(final String fileStorageFileId) {
        return tryDrive(() ->driveClient.me().drive().items(fileStorageFileId).content().buildRequest().get());
    }

    @Override
    public Optional<Long> getContentLength(final String fileStorageFileId) {
       return tryDrive(() -> driveClient.me().drive().items(fileStorageFileId).buildRequest().get().size);
    }

    @Override
    public Optional<String> createEditableUrl(final String fileStorageFileId) {
        return tryDrive(() -> driveClient.me().drive().items(fileStorageFileId).createLink("edit", "organization").buildRequest().post().link.webUrl);
    }

    @Override
    public void delete(final String fileStorageFileId) {
        tryDrive((Supplier<Void>) () -> {
            driveClient.me().drive().items(fileStorageFileId).buildRequest().delete();
            return null;
        });
    }

    private <R> Optional<R> tryDrive(Supplier<R> driveOperation) {
        try {
            return Optional.ofNullable(driveOperation.get());
        } catch (GraphServiceException e) {
            if (StringUtils.contains(e.getMessage(), "itemNotFound")) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
