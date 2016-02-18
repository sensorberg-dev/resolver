package com.sensorberg.front.resolve.service;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Andreas Dörner on 03.02.16.
 */
@Slf4j
@Service
public class AzureQueueService {

    private static String topicName = "synclayout";
    private static String subscriptionName = "resolver1";


    private ServiceBusContract serviceBusContract;

    @PostConstruct
    private void init() {
        Configuration config =
                ServiceBusConfiguration.configureWithSASAuthentication(
                        "sensorbergservicebus",
                        "RootManageSharedAccessKey",
                        "Ul8KTFrgyjBlIUP/jtnffmc10pgyvQslBG6kCCIOkaY=",
                        ".servicebus.windows.net"
                );

        serviceBusContract = ServiceBusService.create(config);

        // Send test messages
        try {
            for (int i=0; i<10; i++) {
                BrokeredMessage message = new BrokeredMessage("MyMessage" + i);
                message.setProperty("MyProperty", i);
                serviceBusContract.sendTopicMessage(topicName, message);
            }

            // Create Subscription
            createSubscription();
        }
        catch (ServiceException e) {
            log.error("Error creating queue.", e);
        }
    }

    private void createSubscription() throws ServiceException {

        // check if subscription is already there
        ListSubscriptionsResult listSubscriptionsResult = serviceBusContract.listSubscriptions(topicName);

        boolean found = false;
        for (SubscriptionInfo subscriptionInfo : listSubscriptionsResult.getItems()) {
            if (subscriptionInfo.getName().equals(subscriptionName)){
                found = true;
                break;
            }
        }

        if (!found) {
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setName(subscriptionName);
            serviceBusContract.createSubscription(topicName, subscriptionInfo);
        }
    }

    //@Scheduled(fixedRate = 5000)
    private void readFromSubscription() {

        try {
            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
            opts.setReceiveMode(ReceiveMode.RECEIVE_AND_DELETE);

            while(true)  {
                ReceiveSubscriptionMessageResult resultQM =
                        serviceBusContract.receiveSubscriptionMessage(topicName, "subscription");

                BrokeredMessage message = resultQM.getValue();
                if (message != null && message.getMessageId() != null) {

                    log.info("MessageID: " + message.getMessageId());

                    // Display the queue message.

                    // NB: does not close inputStream, you can use IOUtils.closeQuietly for that
                    String theString = IOUtils.toString(message.getBody(), "UTF-8");

                    log.info("Body: " + theString);
                    log.info("Custom Property: " + message.getProperty("MyProperty"));
                    // Remove message from queue.
                    serviceBusContract.deleteMessage(message);
                } else {
                    log.info("Finishing up - no more messages.");
                    break;
                }
            }
        } catch (ServiceException e) {
            log.error("readFromSubscription: ", e);
        }
        catch (Exception e) {
            log.error("readFromSubscription: ", e);
        }
    }
}
