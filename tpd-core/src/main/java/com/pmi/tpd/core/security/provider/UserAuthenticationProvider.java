package com.pmi.tpd.core.security.provider;

import static com.pmi.tpd.api.paging.PageUtils.asPageOf;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.user.IGroup;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IEffectivePermission;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.RememberMeUserAuthenticationToken;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * <p>
 * UserAuthenticationProvider class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UserAuthenticationProvider extends AbstractAuthenticationProvider {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationProvider.class);

  /** */
  protected boolean hideUserNotFoundExceptions = true;

  /** */
  private final DefaultPreAuthenticationChecks preAuthenticationChecks = new DefaultPreAuthenticationChecks();

  /** */
  private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

  /** */
  private final IUserRepository userRepository;

  /** */
  private final IGroupRepository groupRepository;

  /** */
  protected final IPermissionService permissionService;

  /** */
  private static IDirectory directory = DefaultDirectory.INTERNAL;

  /**
   * <p>
   * Constructor for DatabaseAuthenticationProvider.
   * </p>
   *
   * @param userRepository
   *                          a {@link IUserRepository} object.
   * @param groupRepository
   *                          a {@link IGroupRepository} object.
   * @param passwordEncoder
   *                          a {@link PasswordEncoder} used to encode and
   *                          validate passwords.
   * @param permissionService
   *                          a {@link IPermissionService}.
   */
  public UserAuthenticationProvider(@Nonnull final IUserRepository userRepository,
      @Nonnull final IGroupRepository groupRepository, @Nonnull final IPermissionService permissionService,
      final PasswordEncoder passwordEncoder) {
    super(passwordEncoder);
    this.userRepository = checkNotNull(userRepository, "userRepository");
    this.groupRepository = checkNotNull(groupRepository, "groupRepository");
    this.permissionService = checkNotNull(permissionService, "permissionService");
  }

  @Override
  public boolean isInternal() {
    return true;
  }

  @Override
  public UserDirectory getSupportedDirectory() {
    return UserDirectory.Internal;
  }

  @Override
  public IDirectory getDirectory() {
    return directory;
  }

  /** {@inheritDoc} */
  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    if (!supports(authentication.getClass())) {
      return null;
    }

    // Determine username
    final String username = authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();

    IUser user = null;
    try {
      user = retrieveUser(username, (UsernamePasswordAuthenticationToken) authentication);
    } catch (final UsernameNotFoundException notFound) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("User '{}' not found", username);
      }

      if (hideUserNotFoundExceptions) {
        throw new BadCredentialsException("Bad credentials", notFound);
      } else {
        throw notFound;
      }
    }

    final UserDetails userDetails = loadUserByUsername(username);

    preAuthenticationChecks.check(userDetails);
    additionalAuthenticationChecks(userDetails, (UsernamePasswordAuthenticationToken) authentication);

    postAuthenticationChecks.check(userDetails);

    return createSuccessAuthentication(authentication, user, userDetails);
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    // exclude RememberMe token
    if (RememberMeUserAuthenticationToken.class.isAssignableFrom(authentication)) {
      return false;
    }
    return UserAuthenticationToken.class.isInstance(authentication)
        || UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IUser loadUser(@Nonnull final String username) throws UsernameNotFoundException, DataAccessException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Try loading user {}", username);
    }
    final IUser user = findUserByName(username);
    if (user == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("User " + username + " was not found");
      }
      throw new UsernameNotFoundException("User " + username + " was not found");
    }

    return user;
  }

  /**
   * @param user
   *             the user to use.
   * @return Returns the list of {@link GrantedAuthority} associated to user.
   */
  @Override
  @Nonnull
  public List<GrantedAuthority> getGrantedAuthorities(@Nonnull final IUser user) {
    final Iterable<IEffectivePermission> effectivePermissions = permissionService.getEffectivePermissions(user);
    final List<GrantedAuthority> authorities = Lists.newArrayList();
    final Set<Permission> allPermissions = Sets.newHashSet();
    allPermissions
        .addAll(from(effectivePermissions).selectMany(effectivePermission -> ImmutableSet.<Permission>builder()
            .add(effectivePermission.getPermission())
            .addAll(effectivePermission.getPermission().getInheritedPermissions())
            .build()).toList());
    for (final Permission permission : allPermissions) {
      authorities.add(new SimpleGrantedAuthority(permission.name()));
    }

    return authorities;
  }

  @Override
  public IGroup findGroupByName(final String groupName) {
    return this.groupRepository.findByName(groupName);

  }

  @Override
  public IUser findUserByName(final String username) {
    return this.userRepository.findByName(username);
  }

  @Override
  public Page<String> findGroupsByName(final String groupName, @Nonnull final Pageable pageRequest) {
    return this.groupRepository.findGroupsByName(groupName, pageRequest).map(GroupEntity::getName);
  }

  @Override
  public Page<IUser> findUsersByName(final String username, @Nonnull final Pageable pageRequest) {
    return asPageOf(IUser.class, userRepository.findByName(username, pageRequest));
  }

  /**
   * @author devacfr<christophefriederich@mac.com>
   */
  private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

    @Override
    public void check(final UserDetails user) {
      if (!user.isAccountNonLocked()) {
        LOGGER.debug("User account is locked");
        throw new LockedException("User account is locked");
      }

      if (!user.isEnabled()) {
        LOGGER.debug("User account is disabled");

        throw new DisabledException("User is disabled");
      }

      if (!user.isAccountNonExpired()) {
        LOGGER.debug("User account is expired");

        throw new AccountExpiredException("User account has expired");
      }
    }
  }

  /**
   * @author Christophe Friederich
   * @since 2.0
   */
  private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

    @Override
    public void check(final UserDetails user) {
      if (!user.isCredentialsNonExpired()) {
        LOGGER.debug("User account credentials have expired");

        throw new CredentialsExpiredException("User credentials have expired");
      }
    }
  }

  @Override
  public void checkConnection() {
    findUsersByName("toto", PageUtils.newRequest(0, 10));
  }

}
