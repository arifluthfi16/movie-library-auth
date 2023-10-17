package com.authservice.resources;

import com.authservice.api.User;
import com.authservice.db.dao.UserDao;
import com.authservice.dto.UserResponseDTO;
import com.authservice.security.BaseAuthenticator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MQConsumerResource implements Managed {
    private final ConnectionFactory factory;
    private final UserDao userDao;
    private final BaseAuthenticator baseAuthenticator;
    private final String consumerQueueName = "username_consumer";
    private final String publisherQueueName = "username_consumer_response";

    public MQConsumerResource(ConnectionFactory factory, UserDao userDao, BaseAuthenticator baseAuthenticator) {
        this.factory = factory;
        this.userDao = userDao;
        this.baseAuthenticator = baseAuthenticator;
    }

    @Override
    public void start() throws Exception {
        Connection connection = factory.newConnection();
        Channel consumerChannel = connection.createChannel();
        Channel publisherChannel = connection.createChannel();

        consumerChannel.queueDeclare(consumerQueueName, false, false, false, null);
        publisherChannel.queueDeclare(publisherQueueName, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String token = new String(delivery.getBody(), StandardCharsets.UTF_8).trim();

            AMQP.BasicProperties properties = delivery.getProperties();
            String correlationId = properties.getCorrelationId();

            if (!token.isEmpty()) {
                if (token.startsWith("\"") && token.endsWith("\"")) token = token.substring(1, token.length() - 1);
                User user = baseAuthenticator.getUserByToken(token);
                String userJson = serializeUserToJson(user);
                sendUserDataToResponseQueue(publisherChannel, userJson, correlationId);
            } else {
                sendUserDataToResponseQueue(publisherChannel, "", correlationId);
            }
        };

        consumerChannel.basicConsume(consumerQueueName, true, deliverCallback, consumerTag -> { });
    }

    private String serializeUserToJson(User user) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(UserResponseDTO.fromUserEntity(user));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private void sendUserDataToResponseQueue(Channel publisherChannel, String userJson, String correlationId) {
        try {
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .contentEncoding("UTF-8")
                    .correlationId(correlationId)
                    .build();

            publisherChannel.basicPublish("", publisherQueueName, properties, userJson.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
