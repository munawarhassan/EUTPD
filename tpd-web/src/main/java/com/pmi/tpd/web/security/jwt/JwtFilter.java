package com.pmi.tpd.web.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 * 
 * @author devacfr
 * @since 1.0
 */
public class JwtFilter extends OncePerRequestFilter {

    /** */
    private final JwtTokenProvider tokenProvider;

    // TODO add authentication and validation of stored user.
    private final UserDetailsService userDetailsService;

    /**
     * @param tokenProvider
     */
    public JwtFilter(final JwtTokenProvider tokenProvider, final UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {
        final String jwt = resolveToken(request);
        Authentication authentication = null;
        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
            authentication = this.tokenProvider.getAuthentication(jwt, request);
            if (authentication != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(final HttpServletRequest request) {
        final String bearerToken = request.getHeader(JwtConfigurer.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}