package com.authservice.resources;

import com.authservice.api.User;
import com.authservice.db.dao.UserDao;
import com.authservice.dto.CredentialsDTO;
import com.authservice.response.ResponseWrapper;
import com.authservice.security.BaseAuthenticator;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.util.StringUtil;
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

            return new ResponseWrapper<>(
                    Response.Status.OK,
                    "Login successful",
                    token
            ).build();
        } else {
            return new ResponseWrapper<>(
                    Response.Status.UNAUTHORIZED,
                    "Authorization failed",
                    null
            ).build();
        }
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        if (userDao.findByUsername(user.getUsername()) != null) {
            return new ResponseWrapper<>(
                Response.Status.CONFLICT,
                "Username already exists",
                null
            ).build();
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        User newUser = new User(user.getUsername(), hashedPassword, user.getRole());
        userDao.createUser(newUser);

        user.setPassword("");
        ResponseWrapper<User> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setStatusType(Response.Status.CREATED);
        responseWrapper.setData(user);
        responseWrapper.setMessage("User registered successfully");

        return responseWrapper.build();
    }

    @Path("/me")
    @GET
    public Response getCurrentUser(@HeaderParam("Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length()).trim();
            ResponseWrapper<User> responseWrapper = new ResponseWrapper<>();

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey("aeftbEgdU5MQhmbv6Tim81uhC00L1BiaBIWidMr40k0=")
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                User user = null;
                if (username != null && !username.isEmpty()) {
                    user = userDao.findByUsername(username);
                    user.setPassword("");
                }

                responseWrapper.setData(user);
                responseWrapper.setMessage("Retrieve succeed");
                responseWrapper.setStatusType(Response.Status.OK);

                return responseWrapper.build();
            } catch (Exception e) {
                return new ResponseWrapper<>(Response.Status.BAD_REQUEST, e.getMessage(),null).build();
            }
        } else {
            return new ResponseWrapper<>(Response.Status.UNAUTHORIZED, "Invalid token format",null).build();
        }
    }

    @GET
    @Path("/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        User user = userDao.findByUsername(username);

        if (user == null) {
            return new ResponseWrapper<>(Response.Status.NOT_FOUND, "User not found",null).build();
        }

        return new ResponseWrapper<>(
                Response.Status.OK, "Successfully retrieved user data",user
        ).build();
    }
}
