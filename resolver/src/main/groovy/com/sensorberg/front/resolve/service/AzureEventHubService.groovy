package com.sensorberg.front.resolve.service
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.jms.*
import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException
/**
 * Created by Andreas Dörner on 25.01.16.
 */

@Service
@Slf4j
public class AzureEventHubService {

    @Value('${connectionfactory.SBCF}')
    private String connectionString;

    @Value('${property.connectionfactory.SBCF.username}')
    private String user;

    @Value('${property.connectionfactory.SBCF.password}')
    private String password;

    @Value('${queue.EventHub}')
    private String eventHub;

    @Value('${queue.EventHub.maxRetries}')
    private int eventHubMaxRetries;

    private static String messageSourceValue = "\"messageSource\":\"RESOLVER\"";

    private static double MAX_MESSAGESIZE = 256D;

    private Session sendSession;
    private Gson gsonEncoder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    private MessageProducer sender;

    @Async
    public void sendAsyncObjectMessage(Object input) {
        sendJsonMessage(encodeMessageToJson(input));
    }

    public void sendSynchronousObjectMessage(Object input) {
        sendJsonMessage(encodeMessageToJson(input));
    }

    String encodeMessageToJson(Object input) {
        logContextInformation(input);
        return gsonEncoder.toJson(input);
    }

    /**
     * Convert into a JSON String to check length.
     * True if size is below max message size
     * @param input
     * @return
     */
    public static boolean checkObjectSize(Object input) {
        final byte[] utf8Bytes = new Gson().toJson(input).getBytes("UTF-8");
        final double messageSizeInKB = (utf8Bytes.length / 1024);

        log.debug("Message size {} KB", messageSizeInKB);

        return messageSizeInKB < MAX_MESSAGESIZE
    }

    /**
     * Executed only once.
     */
    @PostConstruct
    private void initConnection() {

        log.info("init connection called");

        // Set the properties ...
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        properties.put("connectionfactory.SBCF", connectionString);
        properties.put("queue.EventHub", eventHub);
        properties.put("property.connectionfactory.SBCF.username", user);
        properties.put("property.connectionfactory.SBCF.password", password);

        try {
            Context context = new InitialContext(properties);

            ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
            Destination queue = (Destination) context.lookup("EventHub");
            // Create Connection
            Connection connection = cf.createConnection();

            connection.setExceptionListener(new ExceptionListener() {
                @Override
                void onException(JMSException exception) {
                    sender = null;
                    log.error("ExceptionListener triggered: " + exception.getMessage(), exception);
                    // Try to reinit the connection
                    initConnection();
                }
            });

            sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sender = sendSession.createProducer(queue);

        } catch (NamingException e) {
            log.error("error creating JMS connection.", e);
        } catch (JMSException e) {
            log.error("error creating JMS connection.", e);
        }

        log.info("init connection success");
    }

    public void sendJsonMessage(String json) {
        sendBytesMessage(addMessageSource(json));
    }

    public static String addMessageSource(String json) {
        // add message source to json, needed for azure stream analytics
        return json.substring(0,1) + messageSourceValue + "," + json.substring(1);
    }

    private void sendBytesMessage(String messageText) {
        if (sender == null || sendSession == null) {
            log.warn("sender or session is null. Try to reinit connection.");
            initConnection();
        }
        if (sender == null || sendSession == null) {
            log.error("Send message to Azure Event Hub failed because sender or session can't be reinitialized.");
            log.error("Message was not sent: {}", messageText);
            return;
        }
        int tries = 0;
        boolean tryAgain = true;
        String retryId;
        while (tryAgain) {
            tries++;
            tryAgain = false;
            try {
                BytesMessage message = sendSession.createBytesMessage();
                message.writeBytes(messageText.getBytes("UTF-8"));
                sender.send(message);
            } catch (JMSException e) {
                if (retryId == null) {
                    retryId = UUID.randomUUID().toString();
                }
                if (tries < eventHubMaxRetries) {
                    log.error("Error sending message (retrying: " + retryId + ")", e);
                    tryAgain = true;
                    initConnection();
                    Thread.sleep(100);
                } else {
                    log.error("Error sending message because of " + tries + " successive JMSExceptions (ID: " + retryId
                            + "), giving up.", e);
                    log.error("Message was not sent: {}", messageText);
                }
            } catch (UnsupportedEncodingException e) {
                log.error("Error sending message because of an unsupported encoding", e);
                log.error("Message was not sent: {}", messageText);
            }  catch (Exception e) {
                log.error("Totally unexpected Error sending message", e);
                log.error("Message was not sent: {}", messageText);
            }
        }
    }

    public Health getHealth() {
        return sender != null ? new Health.Builder().up().build() : new Health.Builder().down().build();
    }

    /**
     * Write LayoutLog information to log.
     * @param input
     */
    private void logContextInformation(Object input) {
        if (input instanceof LayoutCtx) {
            LayoutCtx layoutCtx = (LayoutCtx) input;
            if (layoutCtx.getRequest() != null && layoutCtx.getRequest().getActivity() != null) {
                int actionCount = 0;
                if (layoutCtx.getRequest().getActivity().getActions() != null) {
                    actionCount = layoutCtx.getRequest().getActivity().getActions().size();
                }
                int eventCount = 0;
                if (layoutCtx.getRequest().getActivity().getEvents() != null) {
                    eventCount = layoutCtx.getRequest().getActivity().getEvents().size();
                }
                log.info("Sending JMS Message with: LayoutCtx ID: {}, actions: {}, events: {}.", layoutCtx.getId(), actionCount, eventCount);
            } else {
                log.info("Sending JMS Message with: LayoutCtx ID: {}, but request or activity not found.", layoutCtx.getId());
            }
        }
    }
}
