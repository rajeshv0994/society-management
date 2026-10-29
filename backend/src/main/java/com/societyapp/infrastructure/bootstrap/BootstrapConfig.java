package com.societyapp.infrastructure.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapConfig {
    @Bean
    CommandLineRunner bootstrap(
            JdbcClient db,
            PasswordEncoder p,
            @Value("${app.bootstrap.super-admin-email}") String email,
            @Value("${app.bootstrap.super-admin-password}") String password) {
        return args -> {
            var count =
                    db.sql("select count(*) from users where role='SUPER_ADMIN'")
                            .query(Long.class)
                            .single();
            if (count == 0)
                db.sql(
                                "insert into users(full_name,email,password_hash,role,email_verified,mobile_verified) values('Platform Administrator',:e,:p,'SUPER_ADMIN',true,true)")
                        .param("e", email)
                        .param("p", p.encode(password))
                        .update();
        };
    }
}
