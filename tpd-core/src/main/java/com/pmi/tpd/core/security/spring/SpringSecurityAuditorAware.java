package com.pmi.tpd.core.security.spring;

import static java.util.Optional.of;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.data.domain.AuditorAware;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.security.IAuthenticationContext;

/**
 * Implementation of AuditorAware based on Spring Security.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    /** Constant <code>SYSTEM_ACCOUNT="system"</code>. */
    public static final String SYSTEM_ACCOUNT = "system";

    /** */
    private final IAuthenticationContext authenticationContext;

    /**
     * <p>
     * Constructor for SpringSecurityAuditorAware.
     * </p>
     *
     * @param authenticationContext
     *            a {@link com.pmi.tpd.security.IAuthenticationContext} object.
     */
    @Inject
    public SpringSecurityAuditorAware(@Nonnull final IAuthenticationContext authenticationContext) {
        this.authenticationContext = Assert.notNull(authenticationContext);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getCurrentAuditor() {
        return authenticationContext.getCurrentUser().map((user) -> user.getUsername()).or(() -> of(SYSTEM_ACCOUNT));
    }

}
