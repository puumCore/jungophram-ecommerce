package com.puumcore.jungophram.ecommerce.security;

import com.puumcore.jungophram.ecommerce.configs.CustomSecurityConfig;
import com.puumcore.jungophram.ecommerce.models.constants.Role;
import com.puumcore.jungophram.ecommerce.repositories.AccountOps;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 12:15 PM
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountOps accountOps;
    private final CustomSecurityConfig customSecurityConfig;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/iam/**", "/error", "/api-docs/**", "/swagger-ui/**").permitAll());

        http.authorizeHttpRequests(
                        request ->
                                request
                                        .requestMatchers(
                                                //STOCK
                                                "/products/create",
                                                "/products/remove",
                                                "/products/update-price",
                                                "/products/update-qty",
                                                "/products/update-info",
                                                "/products/add",

                                                //USERS
                                                "/users/filter",

                                                //ORDERS
                                                "/orders/complete",
                                                "/orders/cancel",
                                                "/orders/filter"
                                        ).hasAuthority(Role.ADMIN.name())

                                        .requestMatchers("/actuator/**").permitAll()
                                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JWTAuthorizationFilter(accountOps, customSecurityConfig), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling().authenticationEntryPoint(
                (request, response, authException) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, "Sorry, but you don't have authorized access to interact with this system!")
        );
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(14);
    }
}
