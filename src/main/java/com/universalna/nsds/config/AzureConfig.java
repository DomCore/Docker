package com.universalna.nsds.config;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ICredentials;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.rest.v2.http.HttpPipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;

import static com.universalna.nsds.Profiles.STREAMING;

@Profile(STREAMING)
@Configuration
public class AzureConfig {

    //TODO:  определиться с секурити облака и методом аутентификации
    @Bean
    public ICredentials sharedKeyCredentials(@Value("${azure.account.name}") final String accountName,
                                             @Value("${azure.account.key}")  final String accountKey) throws InvalidKeyException {
        return new SharedKeyCredentials(accountName, accountKey);
    }

    @Bean
    public ContainerURL serviceURL(@Value("${azure.account.name}") final String accountName,
                                   final ICredentials credentials) throws MalformedURLException {
        final URL url = new URL("http://" + accountName + ".blob.core.windows.net");
        final HttpPipeline pipeline = StorageURL.createPipeline(credentials, new PipelineOptions());
        return new ServiceURL(url, pipeline).createContainerURL("nsds");
    }

    @Bean
    public CloudStorageAccount cloudStorageAccount(@Value("${azure.account.name}") final String accountName,
                                                   @Value("${azure.account.key}") final String accountKey) throws URISyntaxException, InvalidKeyException {
        final String protocol = "DefaultEndpointsProtocol=https;";
        final String name = "AccountName=%s;";
        final String key = "AccountKey=%s";

        final String storageConnectionString = String.format(protocol + name + key, accountName, accountKey);
        return CloudStorageAccount.parse(storageConnectionString);
    }

    @Bean
    public CloudBlobClient cloudBlobClient(final CloudStorageAccount cloudStorageAccount) {
        return cloudStorageAccount.createCloudBlobClient();
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer(@Value("${azure.account.container}") final String containerName,
                                                 final CloudBlobClient cloudBlobClient) throws URISyntaxException, StorageException {
        final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
        container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
        return container;
    }
}
