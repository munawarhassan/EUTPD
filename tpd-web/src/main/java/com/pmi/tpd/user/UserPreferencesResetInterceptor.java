package com.pmi.tpd.user;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.SecurityConfig;
import org.springframework.util.StringUtils;

import com.pmi.tpd.ComponentManager;
import com.pmi.tpd.core.security.ILoginInterceptor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class UserPreferencesResetInterceptor implements ILoginInterceptor {

    @Override
    public void beforeLogin(final HttpServletRequest request,
        final HttpServletResponse response,
        final String username,
        final String password,
        final boolean cookieLogin) {
    }

    @Override
    public void afterLogin(final HttpServletRequest request,
        final HttpServletResponse response,
        final String username,
        final String password,
        final boolean cookieLogin,
        final boolean success) {

        if (!success) {
            return;
        }
        if (StringUtils.hasText(username)) {
            ComponentManager.getInstance()
                    .getAuthenticationContext()
                    .getCurrentUser()
                    .ifPresent(user -> ComponentManager.getInstance().getUserPreferencesManager().clearCache(user));

        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(final Map<?, ?> params, final SecurityConfig config) {
    }
}
