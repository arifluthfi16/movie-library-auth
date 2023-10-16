package com.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RabbitMQConfigurationDTO {
    private String host;
    private int port;
    private String username;
    private String password;
}
