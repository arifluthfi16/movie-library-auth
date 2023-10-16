package com.authservice;

import com.authservice.dto.RabbitMQConfigurationDTO;
import io.dropwizard.core.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import jakarta.validation.constraints.*;

public class AuthServiceConfiguration extends Configuration {
    @NotNull
    @JsonProperty("database")
    public DataSourceFactory database;

    @JsonProperty("flyway")
    public FlywayFactory flywayFactory;

    @NotNull
    @JsonProperty("rabbitmq")
    private RabbitMQConfigurationDTO rabbitMQConfiguration;

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public FlywayFactory getFlywayFactory() {
        return flywayFactory;
    }

    @JsonProperty("rabbitmq")
    public RabbitMQConfigurationDTO getRabbitMQConfiguration() {
        return rabbitMQConfiguration;
    }

    @JsonProperty("rabbitmq")
    public void setRabbitMQConfiguration(RabbitMQConfigurationDTO rabbitMQConfiguration) {
        this.rabbitMQConfiguration = rabbitMQConfiguration;
    }
}
