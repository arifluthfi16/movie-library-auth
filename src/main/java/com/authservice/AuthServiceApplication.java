package com.authservice;

import com.authservice.db.dao.UserDao;
import com.authservice.resources.AuthResource;
import com.authservice.resources.MQConsumerResource;
import com.authservice.security.BaseAuthenticator;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

public class AuthServiceApplication extends Application<AuthServiceConfiguration> {

    public static void main(final String[] args) throws Exception {
        new AuthServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "AuthService";
    }

    @Override
    public void initialize(final Bootstrap<AuthServiceConfiguration> bootstrap) {}

    @Override
    public void run(final AuthServiceConfiguration config,
                    final Environment environment) {
        // Initialize Database & Migrations
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql");

        runFlyway(config.getDataSourceFactory());

        // Setup dependencies & config
        UserDao userDao = jdbi.onDemand(UserDao.class);
        BaseAuthenticator authenticator = new BaseAuthenticator(userDao);

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getRabbitMQConfiguration().getHost());
        connectionFactory.setPort(config.getRabbitMQConfiguration().getPort());
        connectionFactory.setUsername(config.getRabbitMQConfiguration().getUsername());
        connectionFactory.setPassword(config.getRabbitMQConfiguration().getPassword());

        // Resource creation
        MQConsumerResource consumerResource = new MQConsumerResource(connectionFactory, userDao);
        AuthResource authResource = new AuthResource(authenticator, userDao);

        // Resource Registration
        environment.jersey().register(authResource);
        environment.lifecycle().manage(consumerResource);
    }

    private void runFlyway(DataSourceFactory dataSourceFactory) {
        Flyway flyway = Flyway.configure()
                .driver("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .dataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword())
                .locations("filesystem:src/main/java/com/authservice/db/migration")
                .load();
        flyway.migrate();
    }
}
