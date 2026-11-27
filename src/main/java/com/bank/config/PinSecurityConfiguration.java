package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class PinSecurityConfiguration {

    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder pinPasswordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public Clock pinSecurityClock() {
        return Clock.systemUTC();
    }
}