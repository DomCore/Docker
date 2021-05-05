package com.universalna.nsds.component.content;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.universalna.nsds.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@Component
@Profile("!IT")
class AzureBlobClient implements BlobClient {

    @Autowired
    private CloudBlobContainer cloudBlobContainer;

    @Override
    public InputStream getContent(final String reference) {
        try {
            return cloudBlobContainer
                    .getBlockBlobReference(reference)
                    .openInputStream();
        } catch (StorageException | URISyntaxException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    public void putContent(final String id, final InputStream content) {
        try {
            final BlobOutputStream blobOutputStream = cloudBlobContainer.getBlockBlobReference(id).openOutputStream();
            content.transferTo(blobOutputStream);
            blobOutputStream.close();
        } catch (StorageException | IOException | URISyntaxException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    public Long getContentLength(final String id) {
        try {
            final CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(id);
            blob.downloadAttributes();
            return blob.getProperties().getLength();
        } catch (StorageException | URISyntaxException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteContent(final String id) {
        try {
            cloudBlobContainer.getBlockBlobReference(id).deleteIfExists();
        } catch (StorageException | URISyntaxException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }
}
