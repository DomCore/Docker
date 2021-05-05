package com.universalna.nsds.config;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ICredentials;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

//TODO: find a better solution for disabling Azure connection/integration within integration test
@Configuration
public class MockAzureConfig {

    @Bean
    public ICredentials sharedKeyCredentials(@Value("${azure.account.name}") final String accountName,
                                             @Value("${azure.account.key}")  final String accountKey) throws InvalidKeyException {
       return Mockito.mock(ICredentials.class);
    }

    @Bean
    public ContainerURL serviceURL(@Value("${azure.account.name}") final String accountName,
                                   final ICredentials credentials) throws MalformedURLException {
        return Mockito.mock(ContainerURL.class);
    }

    @Bean
    public CloudStorageAccount cloudStorageAccount(@Value("${azure.account.name}") final String accountName,
                                                   @Value("${azure.account.key}") final String accountKey) throws URISyntaxException, InvalidKeyException {
        return Mockito.mock(CloudStorageAccount.class);
    }

    @Bean
    public CloudBlobClient cloudBlobClient(final CloudStorageAccount cloudStorageAccount) {
        return Mockito.mock(CloudBlobClient.class);
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer(final CloudBlobClient cloudBlobClient) throws URISyntaxException, StorageException {
        return Mockito.mock(CloudBlobContainer.class);
    }
}
