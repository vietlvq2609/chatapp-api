package com.vietle.mychatapi.config;

import com.vietle.mychatapi.jwt.JwtSecurityFilter;
import com.vietle.mychatapi.jwt.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtTokenUtil jwtTokenUtil;

    public WebSecurityConfig(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Bean
    public JwtSecurityFilter jwtSecurityFilter() {
        return new JwtSecurityFilter(jwtTokenUtil);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                            .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/signin",
                                "/api/v1/auth/refresh-token"
                            ).permitAll()
                            .anyRequest().permitAll()
                )
                .addFilterBefore(jwtSecurityFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
