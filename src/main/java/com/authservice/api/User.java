package com.authservice.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.Principal;

@Getter
@Setter
@NoArgsConstructor
public class User implements Principal {
    private long id;
    private String username;
    private String password;
    private String role;
    private String country;

    @JsonCreator
    public User(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("role") String role,
            @JsonProperty("country") String country
    ) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.country = country;
    }

    @Override
    public String getName() {
        return username;
    }
}
