package com.authservice.resources;

import com.authservice.api.User;
import com.authservice.db.dao.UserDao;
import com.authservice.dto.CredentialsDTO;
import com.authservice.security.BaseAuthenticator;
import io.dropwizard.auth.AuthenticationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final BaseAuthenticator baseAuthenticator;
    private final UserDao userDao;

    public AuthResource(BaseAuthenticator baseAuthenticator, UserDao userDao) {
        this.baseAuthenticator = baseAuthenticator;
        this.userDao = userDao;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Valid CredentialsDTO credentials) throws AuthenticationException {
        User user = baseAuthenticator.authenticate(credentials).orElse(null);

        if (user != null) {
            String token = baseAuthenticator.generateToken(user);
            return Response.ok("Authentication successful. Token: " + token).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentication failed").build();
        }
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        if (userDao.findByUsername(user.getUsername()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username already exists")
                    .build();
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        User newUser = new User(user.getUsername(), hashedPassword, user.getRole());
        userDao.createUser(newUser);

        // Return a success response
        return Response.status(Response.Status.CREATED)
                .entity("User registered successfully")
                .build();
    }
}
