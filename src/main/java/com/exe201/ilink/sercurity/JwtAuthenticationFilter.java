package com.exe201.ilink.sercurity;

import com.exe201.ilink.model.entity.Token;
import com.exe201.ilink.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().contains("api/v1/auth")){
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwtToken;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);

        if (jwtToken == null && jwtTokenProvider.validateToken(jwtToken) ) {
            String username = jwtTokenProvider.getUsername(jwtToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Token token = tokenRepository.findTokenByAccessToken(jwtToken);

            if (jwtTokenProvider.isTokenValid(jwtToken, userDetails.getUsername())
                    &&  token != null
                    && !token.isRevoked()
                    && !token.isExpired()
            ) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }
        }

        filterChain.doFilter(request, response);

    }


}
