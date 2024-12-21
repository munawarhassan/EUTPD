package com.pmi.tpd;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;
import com.pmi.tpd.web.security.AjaxLogoutSuccessHandler;
import com.pmi.tpd.web.security.Http401UnauthorizedEntryPoint;
import com.pmi.tpd.web.security.jwt.JwtConfiguration;
import com.pmi.tpd.web.security.jwt.JwtConfigurer;
import com.pmi.tpd.web.security.jwt.JwtTokenProvider;

/**
 * @author Christophe Friederich
 * @since 1.0
 */

@Configuration
@EnableConfigurationProperties({ JwtConfiguration.class })
@EnableWebSecurity
@Import({ WebSocketSecurityConfig.class })
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /** */
    public static final String RELATIVE_LOGIN_PATH = "auth/login";

    /** */
    public static final String RELATIVE_LOGOUT_PATH = "auth/logout";

    /** */
    public static final String ABSOLUTE_LOGIN_PATH = "/" + RELATIVE_LOGIN_PATH;

    /** */
    public static final String ABSOLUTE_LOGOUT_PATH = "/" + RELATIVE_LOGOUT_PATH;

    /** */
    @Inject
    private JwtConfiguration jwtConfiguration;

    @Inject
    private IAuthenticationService authenticationService;

    @Inject
    private PasswordEncoder passwordEncoder;

    /** */
    private static final boolean CSRF_PROTECTION = false;

    @Bean
    public JwtTokenProvider jwtTokenProvide() {
        return new JwtTokenProvider(jwtConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/index.html")
                .antMatchers("/favicon.ico")
                .antMatchers("/rest/api/setup/**/*")
                .antMatchers("/system/**/*")
                .antMatchers("/js/**/*.{js,html}")
                .antMatchers("/i18n/**/*")
                .antMatchers("/json/**/*.json")
                .antMatchers("/swagger-ui/**")
                .antMatchers("/lib/**/*")
                .antMatchers("/assets/**");
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(authenticationService).passwordEncoder(passwordEncoder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        http.servletApi().rolePrefix("");
        if (CSRF_PROTECTION) {
            http.csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringAntMatchers(ABSOLUTE_LOGIN_PATH, "/rest/api/secure/register", "/rest/api/secure/activate");

            // http.addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class);
        } else {
            http.csrf().disable();
        }

        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(
            new AndRequestMatcher(CanCreateNewHttpSessionRequestMatcher.matcher(), new MethodRequestMatcher("POST")));
        http.requestCache().requestCache(requestCache);

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        http.exceptionHandling()
                .authenticationEntryPoint(new Http401UnauthorizedEntryPoint())
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
                .and()
                .anonymous()
                .authenticationFilter(
                    new AnonymousAuthenticationFilter("anonymous", ApplicationConstants.Security.ANONYMOUS_USER,
                            Lists.<GrantedAuthority> newArrayList(
                                new SimpleGrantedAuthority(ApplicationConstants.Authorities.ANONYMOUS))))
                .authenticationProvider(new AnonymousAuthenticationProvider("anonymous"))
                .and()
                .logout()
                .logoutUrl(ABSOLUTE_LOGOUT_PATH)
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new AjaxLogoutSuccessHandler())
                .deleteCookies("JSESSIONID", ApplicationConstants.Hazelcast.COOKIE_NAME)
                .permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/metrics/**")
                .hasAnyAuthority(ApplicationConstants.Authorities.ADMIN, ApplicationConstants.Authorities.SYS_ADMIN)
                .anyRequest()
                .permitAll() // all authorizations are managed by Jersey.
                .and()
                .apply(securityConfigurerAdapter());
    }

    /**
     * Apply CORS configuration before Spring Security. By default, "http.cors" take a bean called
     * corsConfigurationSource.
     *
     * @implNote https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#cors
     * @return a CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private JwtConfigurer securityConfigurerAdapter() {
        return new JwtConfigurer(jwtTokenProvide(), authenticationService);
    }

    /**
     * {@link RequestMatcher} that matches requests that are allowed to create a new HttpSession if one does not exist.
     * This matcher is used by Spring Security to control whether an HttpSession should be created.
     */
    private static class CanCreateNewHttpSessionRequestMatcher implements RequestMatcher {

        public static RequestMatcher matcher() {
            return new CanCreateNewHttpSessionRequestMatcher();
        }

        @Override
        public boolean matches(final HttpServletRequest request) {
            if (request.isRequestedSessionIdFromCookie()) {
                // session cookie was provided, allow sessions (purely as a defensive move)
                return true;
            }

            // don't create sessions for Basic auth
            final String authHeader = request.getHeader("Authorization");
            return authHeader == null || !authHeader.startsWith("Basic ");
        }

    }

    /**
     * This class can be configured to match a specific HTTP method.
     */
    private static class MethodRequestMatcher implements RequestMatcher {

        /** */
        private final String httpMethod;

        /**
         * Creates a RequestMatcher for the configured HTTP method.
         *
         * @param httpMethod
         *                   the HTTP method to match.
         */
        MethodRequestMatcher(final String httpMethod) {
            this.httpMethod = httpMethod;
        }

        @Override
        public boolean matches(final HttpServletRequest request) {
            final String currentHttpMethod = request.getMethod();
            if (httpMethod.equals(currentHttpMethod)) {
                return true;
            }
            return false;
        }
    }

}
