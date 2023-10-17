package com.authservice.db.dao;

import com.authservice.api.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(User.class)
public interface UserDao {
    @SqlQuery("SELECT * FROM users WHERE username = :username")
    User findByUsername(@Bind("username") String username);

    @SqlUpdate("INSERT INTO users (username, password, role, country) VALUES (:user.username, :user.password, :user.role, :user.country)")
    void createUser(@BindBean("user") User user);
}
