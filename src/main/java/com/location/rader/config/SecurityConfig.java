package com.location.rader.config;

import com.location.rader.utils.EndpointsConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/register","/login","/userHaveAccessTo/**","/ws/**","/requestForLocationAccess"
                        ,EndpointsConstants.NEW_NOTIFICATION_ENDPOINT
                        ,EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT
                        ,EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
                        .permitAll() // allow public registration
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()) // ✅ This is the new correct way in 6.1+
                .httpBasic(Customizer.withDefaults()); // or use .formLogin() if needed

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}