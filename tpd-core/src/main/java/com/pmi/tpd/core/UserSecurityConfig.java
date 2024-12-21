package com.pmi.tpd.core;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.thymeleaf.ITemplateEngine;

import com.hazelcast.core.HazelcastInstance;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.context.ContextConfiguration;
import com.pmi.tpd.core.mail.DefaultEmailNotifier;
import com.pmi.tpd.core.mail.IMailService;
import com.pmi.tpd.core.model.user.UserConverter;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.user.DefaultUserAdminService;
import com.pmi.tpd.core.user.DefaultUserService;
import com.pmi.tpd.core.user.IEmailNotifier;
import com.pmi.tpd.core.user.IUserAdminService;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.CachingPermissionGraphFactory;
import com.pmi.tpd.core.user.permission.DefaultPermissionAdminService;
import com.pmi.tpd.core.user.permission.IPermissionGraphFactory;
import com.pmi.tpd.core.user.permission.IPermissionValidationService;
import com.pmi.tpd.core.user.permission.PermissionGraphFactoryLifecycle;
import com.pmi.tpd.core.user.permission.PermissionServiceImpl;
import com.pmi.tpd.core.user.permission.PermissionValidationServiceImpl;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.core.user.preference.DefaultUserPreferencesManager;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.preference.spi.DefaultUserPropertyManager;
import com.pmi.tpd.core.user.preference.spi.IUserPropertyManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.IPermissionService;

/**
 * <p>
 * UserCoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Import({ ContextConfiguration.class })
@Configuration
public class UserSecurityConfig {

    /**
     * <p>
     * userService.
     * </p>
     *
     * @param userRepository
     *                                      a {@link com.pmi.tpd.core.user.spi.IUserRepository} object.
     * @param userPreferencesManager
     *                                      a {@link com.pmi.tpd.core.user.preference.IUserPreferencesManager} object.
     * @param passwordHelper
     *                                      a password helper.
     * @param authenticationServiceProvider
     *                                      a authentication service.
     * @return a {@link com.pmi.tpd.core.user.IUserService} object.
     */
    @Bean
    public static IUserService userService(@Nonnull final IEventPublisher eventPublisher,
        @Nonnull final INavBuilder navBuilder,
        @Nonnull final IInternalAvatarService avatarService,
        @Nonnull final IUserRepository userRepository,
        @Nonnull final IGroupRepository groupRepository,
        @Nonnull final IUserPreferencesManager userPreferencesManager,
        @Nonnull final IAuthenticationContext authenticationContext,
        @Nonnull final IPasswordResetHelper passwordHelper,
        final I18nService i18nService,
        final Provider<IAuthenticationService> authenticationServiceProvider) {
        return new DefaultUserService(eventPublisher, navBuilder, avatarService, userRepository, groupRepository,
                userPreferencesManager, authenticationContext, passwordHelper, i18nService,
                authenticationServiceProvider);
    }

    /**
     * <p>
     * userPropertyManager.
     * </p>
     *
     * @param eventPublisher
     *                           a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     * @param propertySetFactory
     *                           a {@link com.pmi.tpd.api.context.IPropertySetFactory} object.
     * @param userKeyStore
     *                           a {@link com.pmi.tpd.core.user.spi.IUserStore} object.
     * @return a {@link com.pmi.tpd.core.user.preference.spi.IUserPropertyManager} object.
     */
    @Bean
    public static IUserPropertyManager userPropertyManager(@Nonnull final IEventPublisher eventPublisher,
        @Nonnull final IPropertySetFactory propertySetFactory,
        final IUserRepository userKeyStore) {
        return new DefaultUserPropertyManager(eventPublisher, propertySetFactory, userKeyStore);
    }

    /**
     * <p>
     * userPreferencesManager.
     * </p>
     *
     * @param publisher
     *                        a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     * @param userRepository
     *                        a {@link com.pmi.tpd.core.user.spi.IUserRepository} object.
     * @param propertyManager
     *                        a {@link com.pmi.tpd.core.user.preference.spi.IUserPropertyManager} object.
     * @return a {@link com.pmi.tpd.core.user.preference.IUserPreferencesManager} object.
     */
    @Bean
    public static IUserPreferencesManager userPreferencesManager(@Nonnull final IEventPublisher publisher,
        @Nonnull final IUserRepository userRepository,
        @Nonnull final IUserPropertyManager propertyManager,
        IPropertySetFactory propertySetFactory) {
        return new DefaultUserPreferencesManager(publisher, userRepository, propertyManager, propertySetFactory);
    }

    @Bean
    public DefaultEmailNotifier emailNotifier(final IMailService mailService,
        @Nonnull final MessageSource messageSource,
        @Nonnull final ITemplateEngine templateEngine,
        final I18nService i18nService,
        final IAuthenticationContext authenticationContext,
        final INavBuilder navBuilder) {
        return new DefaultEmailNotifier(mailService, messageSource, templateEngine, i18nService, authenticationContext,
                navBuilder);
    }

    @Bean
    public IPermissionService permissionService(final IAuthenticationContext authenticationContext,
        final IEffectivePermissionRepository effectivePermissionDao,
        final IPermissionGraphFactory permissionGraphFactory,
        final IUserService userService) {
        return new PermissionServiceImpl(authenticationContext, effectivePermissionDao, permissionGraphFactory,
                userService);
    }

    @Bean
    public IPermissionGraphFactory permissionGraphFactory(final HazelcastInstance hazelcast,
        final IEffectivePermissionRepository effectivePermissionRepository,
        final IUserService userService) {
        return new CachingPermissionGraphFactory(hazelcast.getMap("cache.permissionGraph.defaultPermissions"),
                hazelcast.getMap("cache.permissionGraph.groupPermissions"),
                hazelcast.getMap("cache.permissionGraph.userPermissions"), effectivePermissionRepository, userService);
    }

    @Bean
    public PermissionGraphFactoryLifecycle permissionGraphFactoryLifecycle(
        final CachingPermissionGraphFactory permissionGraphFactory) {
        return new PermissionGraphFactoryLifecycle(permissionGraphFactory);
    }

    @Bean
    public static UserConverter userConverter(final IUserRepository userRepository) {
        return new UserConverter(userRepository);
    }

    @Bean
    public IPermissionAdminService permissionAdminService(final IUserService userService,
        final UserConverter userConverter,
        final IGlobalPermissionRepository globalPermissionDao,
        final IAuthenticationContext authenticationContext,
        final IPermissionService permissionService,
        final IPermissionValidationService permissionValidationService,
        final I18nService i18nService,
        final IEventPublisher eventPublisher) {
        return new DefaultPermissionAdminService(userService, userConverter, globalPermissionDao, authenticationContext,
                permissionService, permissionValidationService, i18nService, eventPublisher);
    }

    @Bean
    public IPermissionValidationService permissionValidationService() {
        return new PermissionValidationServiceImpl();
    }

    @Bean
    public IUserAdminService userAdminService(final IPermissionAdminService permissionAdminService,
        final IUserService userService,
        final IPasswordResetHelper passwordHelper,
        final IEmailNotifier emailNotifier,
        final I18nService i18nService,
        final IAuthenticationService authenticationProviderService,
        final IEventPublisher eventPublisher,
        final IUserRepository userRepository,
        final IGroupRepository groupRepository,
        /* final ICaptchaService captchaService, */ final PlatformTransactionManager transactionManager) {
        return new DefaultUserAdminService(permissionAdminService, userService, passwordHelper, emailNotifier,
                i18nService, authenticationProviderService, eventPublisher, userRepository, groupRepository,
                transactionManager);
    }

}
