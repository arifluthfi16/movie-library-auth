package com.authservice.security;

import com.authservice.api.User;
import com.authservice.db.dao.UserDao;
import com.authservice.dto.CredentialsDTO;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.Optional;

public class BaseAuthenticator implements Authenticator<CredentialsDTO, User> {
    private final UserDao userDao;

    public BaseAuthenticator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(CredentialsDTO credentials) throws AuthenticationException {
        User user = getUserFromDatabase(credentials.getUsername());
        if (user != null && BCrypt.checkpw(credentials.getPassword(), user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private User getUserFromDatabase(String username) {
        if (username == null) return null;
        return userDao.findByUsername(username);
    }

    public String generateToken(User user) {
        String secretKey = "aeftbEgdU5MQhmbv6Tim81uhC00L1BiaBIWidMr40k0=";
        long expirationTime = 86400000L; // 1 Day
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
