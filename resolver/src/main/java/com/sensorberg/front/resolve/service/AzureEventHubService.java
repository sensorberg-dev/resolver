package com.sensorberg.front.resolve.service;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Andreas Dörner on 25.01.16.
 */

@Service
@Slf4j
public class AzureEventHubService {

    private static String messageSourceValue = "\"messageSource\":\"RESOLVER\"";

    private Session sendSession;
    private MessageProducer messageProducer;

    @Async
    public void sendObjectMessage(Object input) {
        Gson gson = new Gson();
        sendJsonMessage(gson.toJson(input));
    }

    /**
     * Executed only once.
     */
    @PostConstruct
    private void initConnection() {

        log.info("init connection called");

        // Configure JNDI environment
        Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        env.put(Context.PROVIDER_URL, "application.properties");
        Context context = null;
        try {

            context = new InitialContext(env);
            ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
            Destination queue = (Destination) context.lookup("EventHub");
            Connection connection = cf.createConnection();

            connection.setExceptionListener(exception -> {
                log.error("ExceptionListener triggered: " + exception.getMessage(), exception);
                // Try to reinit the connection
                initConnection();
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
        // add message source to json, needed for azure stream analytics
        json = json.substring(0,1) + messageSourceValue + "," + json.substring(1);
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

            log.info("Message sent {}", messageText);

        } catch (JMSException e) {
            log.error("Error sending message", e);
            initConnection();
        } catch (UnsupportedEncodingException e) {
            log.error("Error sending message", e);
        }
    }
}