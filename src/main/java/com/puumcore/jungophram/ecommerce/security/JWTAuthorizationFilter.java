package com.puumcore.jungophram.ecommerce.security;

import com.puumcore.jungophram.ecommerce.configs.CustomSecurityConfig;
import com.puumcore.jungophram.ecommerce.repositories.AccountOps;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final AccountOps accountOps;
    private final CustomSecurityConfig customSecurityConfig;


    /**
     * Can be overridden in subclasses for custom filtering control,
     * returning {@code true} to avoid filtering of the given request.
     * <p>The default implementation always returns {@code false}.
     *
     * @param request current HTTP request
     * @return whether the given request should <i>not</i> be filtered
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final var servletPath = request.getServletPath();
        return Stream.of("/iam", "/error", "/api-docs", "/swagger-ui").anyMatch(servletPath::startsWith);
    }

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request     http client request
     * @param response    http client response
     * @param filterChain possible implemented http filters in the security config class
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith(customSecurityConfig.getTokenPrefix())) {
            Account userFromToken = accountOps.getUserFromToken(authorizationHeaderValue);

            final List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>(
                    Collections.singletonList(new SimpleGrantedAuthority(userFromToken.getRole().name()))
            ) {
            };
            if (grantedAuthorities.isEmpty()) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication");
                return;
            } else {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userFromToken.getEmail(), null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }


}