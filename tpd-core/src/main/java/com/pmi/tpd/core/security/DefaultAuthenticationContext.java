package com.pmi.tpd.core.security;

import static java.util.Optional.empty;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.cluster.concurrent.IStatefulService;
import com.pmi.tpd.cluster.concurrent.ITransferableState;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultAuthenticationContext implements IAuthenticationContext, IStatefulService {

    @Override
    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    @Override
    public Optional<IUser> getCurrentUser() {
        return getCurrentToken().map(UserAuthenticationToken::getPrincipal);
    }

    @Override
    public Optional<UserAuthenticationToken> getCurrentToken() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UserAuthenticationToken) {
            return Optional.of((UserAuthenticationToken) authentication);
        }
        return empty();
    }

    @Nonnull
    @Override
    public ITransferableState getState() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return new ITransferableState() {

            @Override
            public void apply() {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            @Override
            public void remove() {
                SecurityContextHolder.clearContext();
            }
        };
    }

}
