package com.pmi.tpd.core.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.SecurityConfig;

/**
 * <p>
 * ILoginInterceptor interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ILoginInterceptor {

    /**
     * <p>
     * beforeLogin.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param username
     *            a {@link java.lang.String} object.
     * @param password
     *            a {@link java.lang.String} object.
     * @param cookieLogin
     *            a boolean.
     */
    void beforeLogin(HttpServletRequest request,
        HttpServletResponse response,
        String username,
        String password,
        boolean cookieLogin);

    /**
     * <p>
     * afterLogin.
     * </p>
     *
     * @param request
     *            a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response
     *            a {@link javax.servlet.http.HttpServletResponse} object.
     * @param username
     *            a {@link java.lang.String} object.
     * @param password
     *            a {@link java.lang.String} object.
     * @param cookieLogin
     *            a boolean.
     * @param success
     *            a boolean.
     */
    void afterLogin(HttpServletRequest request,
        HttpServletResponse response,
        String username,
        String password,
        boolean cookieLogin,
        boolean success);

    /**
     * <p>
     * destroy.
     * </p>
     */
    void destroy();

    /**
     * <p>
     * init.
     * </p>
     *
     * @param params
     *            a {@link java.util.Map} object.
     * @param config
     *            a {@link org.springframework.security.access.SecurityConfig} object.
     */
    void init(Map<?, ?> params, SecurityConfig config);
}
