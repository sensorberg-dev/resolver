package com.sensorberg.front.resolve.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Andreas Dörner on 07.01.16.
 */

@Slf4j
@Service
public class AzureBlobStorageService {

    @Value("${azure.blobstorage.connectionstring}")
    private String connectionString;

    @Value("${azure.blobstorage.container.beaconstatistic}")
    @Getter
    private String containerBeaconstatistic;

    @Value("${azure.blobstorage.nodeid}")
    @Getter
    private String nodeId;

    public void checkContainer(String containerName) throws URISyntaxException, InvalidKeyException, StorageException {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        // Create the container if it does not exist.
        boolean exists = container.exists();

        log.debug("Container {} result {}", containerName, exists);
    }

    /**
     * List all blobs in the container
     */
    public List<String> listBlobs(String containerName, String prefix)
            throws URISyntaxException, InvalidKeyException, StorageException {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Retrieve reference to a previously created container.
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        List<String> blobList = new ArrayList<>();

        // Loop over blobs within the container and output the URI to each of them.
        for (ListBlobItem blobItem : container.listBlobs(prefix, true)) {

            if (blobItem instanceof CloudBlob) {
                log.info("Blob: {}", blobItem.getUri());
            }

            if (blobItem instanceof CloudBlobDirectory) {
                log.info("Dir: {}", blobItem.getUri());
            }

            blobList.add(blobItem.getUri().toString());
        }
        return blobList;
    }

    /**
     * Download blob for date and hour
     */
    public void dowloadBlob(LocalDate date, String hour, String containerName)
            throws FileNotFoundException, StorageException, UnsupportedEncodingException, InvalidKeyException,
            URISyntaxException {

        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonthValue());
        String day = String.valueOf(date.getMonthValue());

        String blobNameBegin = "/" + year + "/" + month + "/" + day + "/" + hour;

        downloadBlobs(blobNameBegin, containerName);
    }

    /**
     * Download blobs
     */
    public List<String> downloadBlobs(String prefix, String containerName)
            throws URISyntaxException, InvalidKeyException, StorageException, FileNotFoundException,
            UnsupportedEncodingException {

        log.info("Checking blobs for {}", prefix);

        List<String> list = new ArrayList<>();

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Retrieve reference to a previously created container.
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        // Loop through each blob item in the container.
        for (ListBlobItem blobItem : container.listBlobs(prefix, true)) {
            // If the item is a blob, not a virtual directory.
            if (blobItem instanceof CloudBlob) {
                // Download the item and save it to a file with the same name.
                CloudBlob blob = (CloudBlob) blobItem;

                log.info("Reading blob {}", blob.getName());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                blob.download(stream);

                list.add(stream.toString());
            }
        }
        return list;
    }

    public void writeBlob(String targetFilenameWithPath, String containerName, String payload)
            throws URISyntaxException, InvalidKeyException, StorageException, IOException {

        log.info("Writing blob: {}", targetFilenameWithPath);

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Retrieve reference to a previously created container.
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        // Create or overwrite the blob with the payload.
        CloudBlockBlob blob = container.getBlockBlobReference(targetFilenameWithPath);

        InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        blob.upload(stream, stream.available());
    }
}

