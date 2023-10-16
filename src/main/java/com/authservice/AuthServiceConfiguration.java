package com.authservice;

import io.dropwizard.core.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import org.hibernate.validator.constraints.*;
import jakarta.validation.constraints.*;

public class AuthServiceConfiguration extends Configuration {
    @JsonProperty("database")
    @NotNull
    public DataSourceFactory database;

    @JsonProperty("flyway")
    public FlywayFactory flywayFactory;

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
}
