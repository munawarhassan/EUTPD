/**
 * Copyright 2015 Christophe Friederich Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.pmi.tpd.core;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.security.DefaultAuthenticationContext;
import com.pmi.tpd.core.security.DefaultAuthenticationService;
import com.pmi.tpd.core.security.DefaultAuthenticationSynchroniser;
import com.pmi.tpd.core.security.DefaultSecurityService;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.security.IAuthenticationSynchroniser;
import com.pmi.tpd.core.security.ISecurityService;
import com.pmi.tpd.core.security.audit.AuthenticationAuditListener;
import com.pmi.tpd.core.security.audit.AuthorizationAuditListener;
import com.pmi.tpd.core.security.configuration.SecurityProperties;
import com.pmi.tpd.core.security.provider.DefaultAuthentificationProviderService;
import com.pmi.tpd.core.security.provider.IAuthenticationProviderService;
import com.pmi.tpd.core.security.spring.SpringSecurityAuditorAware;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.impl.DefaultPasswordHelper;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.random.DefaultSecureRandomService;
import com.pmi.tpd.security.random.DefaultSecureTokenGenerator;
import com.pmi.tpd.security.random.ISecureRandomService;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.security.spring.ExtendedAuthenticationTrustResolver;
import com.pmi.tpd.security.spring.ExtendedMethodSecurityExpressionHandler;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.spring.env.EnableConfigurationProperties;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * <p>
 * SecurityCoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({ SecurityProperties.class })
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends GlobalMethodSecurityConfiguration implements EnvironmentAware {

    /** */
    private RelaxedPropertyResolver applicationProperties;

    /** */
    private final SecurityProperties securityConfiguration;

    /**
     *
     */
    public SecurityConfig(final SecurityProperties securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.applicationProperties = new RelaxedPropertyResolver(environment, "security.");
    }

    /**
     * @return Returns {@link AuthenticationTrustResolver} instance.
     */
    @Bean
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new ExtendedAuthenticationTrustResolver();
    }

    /**
     * @return Returns {@link MethodSecurityExpressionHandler} instance.
     */
    @Bean
    protected MethodSecurityExpressionHandler expressionHandler() {
        final DefaultMethodSecurityExpressionHandler expressionHandler = new ExtendedMethodSecurityExpressionHandler();
        return expressionHandler;
    }

    /**
     * @return Returns IAuthenticationContext instance.
     */
    @Bean
    public static IAuthenticationContext authenticationContext() {
        return new DefaultAuthenticationContext();
    }

    /**
     * @return Returns {@link ISecureRandomService} instance.
     */
    @Bean
    public static ISecureRandomService secureRandomService() {
        return DefaultSecureRandomService.getInstance();
    }

    /**
     * @return Returns {@link ISecureTokenGenerator} instance.
     */
    @Bean
    public static ISecureTokenGenerator secureTokenGenerator() {
        return DefaultSecureTokenGenerator.getInstance();
    }

    /**
     * <p>
     * passwordEncoder.
     * </p>
     *
     * @return a {@link org.springframework.security.crypto.password.PasswordEncoder} object.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @param userRepository
     *                               a user repository.
     * @param userPreferencesManager
     *                               a user preference manager.
     * @param passwordEncoder
     *                               a password encoder.
     * @param secureTokenGenerator
     *                               a token generator.
     * @return Returns a {@link IPasswordResetHelper} instance.
     */
    @Bean
    public IPasswordResetHelper passwordHelper(final IUserRepository userRepository,
        final IUserPreferencesManager userPreferencesManager,
        final PasswordEncoder passwordEncoder,
        final ISecureTokenGenerator secureTokenGenerator) {
        return new DefaultPasswordHelper(userRepository, userPreferencesManager, secureTokenGenerator, passwordEncoder,
                applicationProperties.getProperty("password.reset.validity.period", Integer.class));
    }

    /**
     * synchronize external user directory with internal directory.
     *
     * @param authenticationProviderService
     *                                      the authentication provider service.
     * @param userRepository
     *                                      a {@link IUserRepository} object.
     * @param groupRepository
     *                                      a {@link IGroupRepository} object.
     * @param userPreferencesManager
     *                                      a {@link IUserPreferencesManager} object.
     * @param eventPublisher
     *                                      the event publisher
     * @return Returns a {@link IAuthenticationProviderService} instance.
     */
    @Bean
    public static IAuthenticationSynchroniser authenticationSynchroniser(
        @Nonnull final IAuthenticationProviderService authenticationProviderService,
        @Nonnull final IUserRepository userRepository,
        @Nonnull final IGroupRepository groupRepository,
        @Nonnull final IUserPreferencesManager userPreferencesManager,
        @Nonnull final IEventPublisher eventPublisher) {
        return new DefaultAuthenticationSynchroniser(authenticationProviderService, userRepository, groupRepository,
                userPreferencesManager, eventPublisher);
    }

    /**
     * <p>
     * delegate Authentication Provider.
     * </p>
     *
     * @param userRepository
     *                                   a {@link IUserRepository} object.
     * @param groupRepository
     *                                   a {@link IGroupRepository} object.
     * @param authenticationSynchroniser
     *                                   a {@link IAuthenticationSynchroniser} object.
     * @param permissionServiceProvider
     *                                   a provider of {@link IPermissionService} object.
     * @param passwordEncoder
     *                                   the password encoder to use.
     * @param applicationContext
     *                                   Spring application context
     * @param applicationProperties
     *                                   global application properties
     * @param i18nService
     *                                   i18n service.
     * @return a {@link IAuthenticationProviderService} object.
     */
    @Bean
    public IAuthenticationProviderService authenticationProviderService(@Nonnull final IUserRepository userRepository,
        @Nonnull final IGroupRepository groupRepository,
        @Nonnull final Provider<IPermissionService> permissionServiceProvider,
        @Nonnull final Provider<IAuthenticationSynchroniser> authenticationSynchroniser,
        @Nonnull final PasswordEncoder passwordEncoder,
        @Nonnull final ApplicationContext applicationContext,
        @Nonnull final I18nService i18nService) {

        return new DefaultAuthentificationProviderService(userRepository, groupRepository, permissionServiceProvider,
                authenticationSynchroniser, passwordEncoder, applicationContext, securityConfiguration, i18nService);

    }

    /**
     * @param applicationEventPublisher
     *                                      spring application event publisher.
     * @param userRepository
     *                                      a user repository.
     * @param authenticationProviderService
     *                                      a authentication provider service.
     * @param i18nService
     *                                      a i18n service.
     * @param userCache
     *                                      a user cache.
     * @return Returns instance of {@link IAuthenticationService}.
     */
    @Bean()
    public static IAuthenticationService authenticationService(
        @Nonnull final ApplicationEventPublisher applicationEventPublisher,
        @Nonnull final IUserRepository userRepository,
        @Nonnull final IAuthenticationProviderService authenticationProviderService,
        @Nonnull final I18nService i18nService,
        @Nonnull final UserCache userCache) {
        return new DefaultAuthenticationService(applicationEventPublisher, userRepository,
                authenticationProviderService, i18nService, userCache);
    }

    /**
     * @param i18nService
     *                                  a i18n service
     * @param requestManager
     *                                  a request manager.
     * @param userService
     *                                  a user service.
     * @param persistentTokenRepository
     *                                  a persistent token repository.
     * @return Returns instance of {@link ISecurityService}.
     */
    @Bean(name = "DefaultSecurityService")
    public static ISecurityService securityService(@Nonnull final I18nService i18nService,
        @Nonnull final IRequestManager requestManager,
        @Nonnull final IUserService userService) {
        return new DefaultSecurityService(i18nService, requestManager, userService);
    }

    /**
     * <p>
     * springSecurityAuditorAware.
     * </p>
     *
     * @param authenthicationContext
     *                               a {@link com.pmi.tpd.core.security.ISecurityService} object.
     * @return a {@link com.pmi.tpd.core.security.spring.SpringSecurityAuditorAware} object.
     */
    @Bean(name = "springSecurityAuditorAware")
    public static SpringSecurityAuditorAware springSecurityAuditorAware(
        @Nonnull final IAuthenticationContext authenthicationContext) {
        return new SpringSecurityAuditorAware(authenthicationContext);
    }

    /**
     * <p>
     * authenticationAuditListener.
     * </p>
     *
     * @param publisher
     *                  a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     * @return a {@link com.pmi.tpd.core.security.audit.AuthenticationAuditListener} object.
     */
    @Bean
    public static AuthenticationAuditListener authenticationAuditListener(@Nonnull final IEventPublisher publisher) {
        return new AuthenticationAuditListener(publisher);
    }

    /**
     * <p>
     * authorizationAuditListener.
     * </p>
     *
     * @param publisher
     *                  a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     * @return a {@link com.pmi.tpd.core.security.audit.AuthorizationAuditListener} object.
     */
    @Bean
    public static AuthorizationAuditListener authorizationAuditListener(@Nonnull final IEventPublisher publisher) {
        return new AuthorizationAuditListener(publisher);
    }

}
