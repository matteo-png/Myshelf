package com.myshelf.apiMyshelf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
 @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // pas de CSRF pour une API (surtout en dev)
                .csrf(AbstractHttpConfigurer::disable)

                // on dit quelles routes sont autorisées
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()  // toutes tes API ouvertes
                        .anyRequest().permitAll()               // même le reste, pour l'instant
                )

                // on désactive complètement le formulaire de login par défaut
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // on utilise un mode "stateless" (préparé pour du JWT plus tard)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
