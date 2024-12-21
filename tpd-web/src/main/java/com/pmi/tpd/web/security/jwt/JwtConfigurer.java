package com.pmi.tpd.web.security.jwt;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author devacfr
 * @since 1.0
 */
public class JwtConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    /** */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** */
    private final JwtTokenProvider tokenProvider;

    private final UserDetailsService userDetailsService;

    /**
     * @param tokenProvider
     */
    public JwtConfigurer(final JwtTokenProvider tokenProvider, final UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        final JwtFilter customFilter = new JwtFilter(tokenProvider, userDetailsService);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
