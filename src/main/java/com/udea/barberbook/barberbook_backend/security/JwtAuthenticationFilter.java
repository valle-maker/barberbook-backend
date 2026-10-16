package com.udea.barberbook.barberbook_backend.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.udea.barberbook.barberbook_backend.user.domain.User;
import com.udea.barberbook.barberbook_backend.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JTI_ATTRIBUTE = "jwt_jti";
    public static final String EXPIRATION_ATTRIBUTE = "jwt_exp";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlocklistService tokenBlocklistService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.parseClaims(token);

                if (!tokenBlocklistService.isRevoked(claims.getId())) {
                    // The role is re-read from the database on every request instead of trusting
                    // the one inside the token (HA-01): a demoted or disabled account loses its
                    // privileges immediately, not when its token expires.
                    userRepository.findByEmail(claims.getSubject())
                        .filter(User::isEnabled)
                        .ifPresent(user -> {
                            var authentication = new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            request.setAttribute(JTI_ATTRIBUTE, claims.getId());
                            request.setAttribute(EXPIRATION_ATTRIBUTE, claims.getExpiration().toInstant());
                        });
                }
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
