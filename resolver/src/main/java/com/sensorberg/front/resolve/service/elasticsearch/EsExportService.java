package com.sensorberg.front.resolve.service.elasticsearch;

import java.util.UUID;

import org.elasticsearch.common.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sensorberg.front.resolve.service.AzureBlobStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Andreas Dörner on 01.03.16.
 */

@Service
@Slf4j
public class EsExportService {
    private static final int BLOB_SIZE = 10 * 1024 * 1024; // azure keeps the files around 10 MB in size
    private static final int BUF_SIZE = BLOB_SIZE + 6*1024*1024;  // no entry should be bigger than 6 MB

    DateTime beginningOfTime = DateTime.parse("2014-06-01");

    @Autowired
    EsReaderService esReaderService;

    @Autowired
    AzureBlobStorageService blobService;

//    /**
//     * Start the actual work.
//     * @param fromDate
//     * @param toDate
//     */
//    public void relocateEsEntries(DateTime fromDate, DateTime toDate) {
//        if(fromDate == null) {
//            fromDate = beginningOfTime;
//        }
//        if(toDate == null) {
//            toDate = DateTime.now();
//        }
//
//        int countAll = 0;
//        // generate periods
//        DateTime period = toDate;
//        long allStart = System.currentTimeMillis();
//        while(period.isAfter(fromDate)) {
//            Date to = period.toDate();
//            if (period.getMinuteOfHour() == 0 && period.getSecondOfMinute() == 0 && period.getMillisOfSecond() == 0) {
//                period = period.minusHours(1);
//            } else {
//                period = period.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
//            }
//            Date from = period.toDate();
//            long chunkStart = System.currentTimeMillis();
//            try {
//
//                log.info("Period:" + from.toString() + " - " + to.toString());
//
//                long taskStart = System.currentTimeMillis();
//
//                // read entries
//                EsEntryList esEntryList = esReaderService.readEntries(from, to);
//
//                log.debug("Time for reading entries to relocate: {}", System.currentTimeMillis() - taskStart);
//                log.info("Entries found: {}", esEntryList.getEntryList().size());
//                countAll += esEntryList.getEntryList().size();
//
//                if (esEntryList.getEntryList().size() > 0){
//                    taskStart = System.currentTimeMillis();
//
//                    // do relocation
//                    relocateSomeEntries(esEntryList, period);
//
//                    log.debug("Time for relocating entries: {}", System.currentTimeMillis() - taskStart);
//                }
//
//            } catch (Exception e) {
//                log.warn("problem when migrating period [from: {}, to: {}, reason: {}]", from, to, e.getMessage());
//            }
//            log.debug("Time for relocating the period: {}", System.currentTimeMillis() - chunkStart);
//        }
//        log.debug("Time for relocating all entries: {}", System.currentTimeMillis() - allStart);
//        log.info("Entries found for the full period: {}", countAll);
//    }
//
//    /**
//     * manipulate Data.
//     */
//    private void relocateSomeEntries(EsEntryList esEntryList, DateTime period)
//            throws IOException, InvalidKeyException, StorageException, URISyntaxException {
//
//        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
//
//        int idx = 0;
//        StringBuilder buf = new StringBuilder(BUF_SIZE);
//        String dirName = String.format("/%04d/%02d/%02d/%02d/", period.getYear(), period.getMonthOfYear(),
//                period.getDayOfMonth(), period.getHourOfDay());
//
//        for (Iterator<EsEntry> iter = esEntryList.getEntryList().iterator(); iter.hasNext(); ) {
//
//            EsEntry esEntry = iter.next();
//
//            Map<String, Object> rawData = esEntry.getRawData();
//            String messageId = esEntry.getId();
//
//            buf.append(AzureEventHubService.addMessageSourceAndId(gson.toJson(rawData), messageId));
//
//            if (buf.length() >= BLOB_SIZE) {
//                idx++;
//                blobService.writeBlob(buildFileName(dirName, idx), blobService.getContainerBeaconstatistic(), buf.toString());
//                buf = new StringBuilder(BUF_SIZE); // azure keeps the files around 10 MB in size
//            } else if (iter.hasNext()) {  // ONLY insert CR/LF between entries!
//                buf.append("\r\n");
//            }
//        }
//
//        if (buf.length() > 0) {
//            idx++;
//            blobService.writeBlob(buildFileName(dirName, idx), blobService.getContainerBeaconstatistic(), buf.toString());
//        }
//    }

    private String buildFileName(String dirName, int idx) {
        return dirName + blobService.getNodeId() + "_" + UUID.randomUUID().toString().replace("-", "") + "_" + idx;
    }
}

