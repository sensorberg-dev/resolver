package com.sensorberg.front.resolve.service
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageException
import com.microsoft.azure.storage.blob.*
import groovy.util.logging.Slf4j
import lombok.Getter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Service

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
/**
 * Created by Andreas Dörner on 07.01.16.
 */

@Slf4j
@Service
public class AzureBlobStorageService {

    @Value('${azure.blobstorage.connectionstring}')
    private String connectionString;

    @Value('${azure.blobstorage.container.beaconstatistic}')
    @Getter
    private String containerBeaconstatistic;

    @Value('${azure.blobstorage.nodeid}')
    @Getter
    private String nodeId;

    public boolean checkContainer(String containerName) {
        try {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);

            // Create the blob client.
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Get a reference to a container.
            // The container name must be lower case
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            return container.exists();
        } catch (Exception e) {
            log.error("Check container error {}", e.getMessage())
            return false;
        }
    }

    /**
     * We always create a new connection here, because i found now good way to check if the connection
     * is open or closed.
     * Since this code is only used for a migration of ELS to Azure, we can assume this is no problem.
     * This will not happen to often and is triggered manually.
     */
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

    public Health getHealth() {
        return checkContainer(containerBeaconstatistic)  ? new Health.Builder().up().build() : new Health.Builder().down().build();
    }
}

