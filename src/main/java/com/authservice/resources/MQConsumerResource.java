package com.authservice.resources;

import com.authservice.api.User;
import com.authservice.db.dao.UserDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MQConsumerResource implements Managed {
    private final ConnectionFactory factory;
    private final UserDao userDao;
    private final String consumerQueueName = "username_consumer";
    private final String publisherQueueName = "username_consumer_response";

    public MQConsumerResource(ConnectionFactory factory, UserDao userDao) {
        this.factory = factory;
        this.userDao = userDao;
    }

    @Override
    public void start() throws Exception {
        Connection connection = factory.newConnection();
        Channel consumerChannel = connection.createChannel();
        Channel publisherChannel = connection.createChannel();

        consumerChannel.queueDeclare(consumerQueueName, false, false, false, null);
        publisherChannel.queueDeclare(publisherQueueName, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String username = new String(delivery.getBody(), StandardCharsets.UTF_8);

            AMQP.BasicProperties properties = delivery.getProperties();
            String correlationId = properties.getCorrelationId();

            if (!username.isEmpty()) {
                User user = userDao.findByUsername(username);
                user.setPassword("");
                String userJson = serializeUserToJson(user);
                sendUserDataToResponseQueue(publisherChannel, userJson, correlationId);
            }
        };

        consumerChannel.basicConsume(consumerQueueName, true, deliverCallback, consumerTag -> { });
    }

    private String serializeUserToJson(User user) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(user);
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
