package com.pmi.tpd.service.testing.mock;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.pmi.tpd.security.spring.UserAuthenticationToken;

public class MockAuthenticationProvider implements AuthenticationProvider {

    public MockAuthenticationProvider() {
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        return authentication;
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.isAssignableFrom(UserAuthenticationToken.class);
    }

}
