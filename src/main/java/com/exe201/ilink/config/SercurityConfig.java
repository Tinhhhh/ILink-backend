package com.exe201.ilink.config;

import com.exe201.ilink.sercurity.JwtAuthenticationEntryPoint;
import com.exe201.ilink.sercurity.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SercurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAuthenticationFilter authenticationFilter;

    private final LogoutHandler logoutHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .authorizeHttpRequests(request ->
                request.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/auth/**",
                        "/post/shop", "/post/all", "/post/details",
                        "/category/all",
                        "/product/details",
                        "/shop/**",
                        "/payment/**").permitAll()
                    .requestMatchers("/order/buyer/**").hasAuthority("BUYER")
                    .requestMatchers("/account/**", "/create-payment-link").hasAnyAuthority("BUYER", "SELLER")
                    .requestMatchers("/product/shop", "/product/new", "/product/edit", "product/picture").hasAnyAuthority("SELLER", "MANAGER")
                    .requestMatchers("/order/seller/**").hasAnyAuthority("SELLER")
                    .requestMatchers("/product/**", "/shop/**", "/category/new").hasAnyAuthority("MANAGER")
                    .requestMatchers("/admin/**","order/admin/**").hasAuthority("ADMIN")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.logout(logout -> logout.logoutUrl("api/v1/account/logout")
            .addLogoutHandler(logoutHandler)
            .logoutSuccessHandler((request, response, authentication)
                -> SecurityContextHolder.clearContext()));


        return http.build();
    }
}
