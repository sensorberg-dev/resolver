package com.sensorberg.front.resolve.service
import com.google.gson.Gson
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

    private static String messageSourceValue = "\"messageSource\":\"RESOLVER\"";

    private static double MAX_MESSAGESIZE = 256D;

    private Session sendSession;
    private MessageProducer messageProducer;

    @Async
    public void sendAsyncObjectMessage(Object input) {
        sendObjectMessage(input);
    }

    public void sendSynchronousObjectMessage(Object input) {
        sendObjectMessage(input);
    }

    private void sendObjectMessage(Object input) {
        logContextInformation(input);
        sendJsonMessage(new Gson().toJson(input));
    }

    /**
     * Convert into a JSON String to check length.
     * True if size is below max message size
     * @param input
     * @return
     */
    public boolean checkObjectSize(Object input) {
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
                    messageProducer = null;
                    log.error("ExceptionListener triggered: " + exception.getMessage(), exception);
                    // Try to reinit the connection
                    initConnection();
                }
            });

            sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = sendSession.createProducer(queue);

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
        if (messageProducer == null) {
            initConnection();
        }
        try {
            BytesMessage message = sendSession.createBytesMessage();
            message.writeBytes(messageText.getBytes("UTF-8"));
            messageProducer.send(message);
        } catch (JMSException | UnsupportedEncodingException e) {
            log.error("Error sending message", e);
            log.error("Message was not {}", messageText);
        }  catch (Exception e) {
            log.error("Totally unexpected Error sending message", e);
            log.error("Message was not {}", messageText);
        }
    }

    public Health getHealth() {
        return messageProducer != null ? new Health.Builder().up().build() : new Health.Builder().down().build();
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
