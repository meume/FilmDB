package com.demo.filmdb.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.demo.filmdb.rest.util.Path.API_PREFIX;
import static com.demo.filmdb.rest.util.Path.LOGIN;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    public static final String ROLE_ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/graphql").permitAll()
                        .requestMatchers(API_PREFIX + LOGIN).permitAll()
                        .requestMatchers(HttpMethod.DELETE).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST).hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT).hasRole(ROLE_ADMIN)
                        .anyRequest().permitAll()
                ).build();
    }

    @SuppressWarnings({"deprecation"})
    @Bean
    public UserDetailsService userDetailsService() {
        User.UserBuilder users = User.withDefaultPasswordEncoder();
        UserDetails admin = users
                .username("admin")
                .password("password")
                .roles("USER", ROLE_ADMIN)
                .build();
        UserDetails user = users
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }
}
