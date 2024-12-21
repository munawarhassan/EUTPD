package com.pmi.tpd.core.security.configuration;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@ConfigurationProperties("app.security.authentication.directory")
public class SecurityProperties {

  private final Map<Class<? extends IAuthenticationProperties>, IAuthenticationProperties> configs = Maps
      .newHashMap();

  public LdapAuthenticationProperties getLdap() {
    return getAuthenticationConfiguration(LdapAuthenticationProperties.class);

  }

  public void setLdap(final LdapAuthenticationProperties ldap) {
    if (configs == null) {
      configs.remove(LdapAuthenticationProperties.class);
    } else {
      configs.put(ldap.getClass(), ldap);
    }

  }

  public ActiveDirectoryAuthenticationProperties getActiveDirectory() {
    return getAuthenticationConfiguration(ActiveDirectoryAuthenticationProperties.class);
  }

  public void setActiveDirectory(final ActiveDirectoryAuthenticationProperties activeDirectory) {
    if (configs == null) {
      configs.remove(ActiveDirectoryAuthenticationProperties.class);
    } else {
      configs.put(activeDirectory.getClass(), activeDirectory);
    }

  }

  /**
   * - * @return Returns a {@link Iterable} containing all authentication
   * configuration.
   */
  @Nonnull
  public Iterable<IAuthenticationProperties> authentications() {
    return configs.values();
  }

  /**
   * @return Returns the current authentication configuration.
   */
  public Optional<LdapAuthenticationProperties> currentAuthenticationConfiguration() {
    return this.configs.values()
        .stream()
        .map(c -> LdapAuthenticationProperties.class.cast(c))
        .filter(c -> !c.empty())
        .findFirst();
  }

  /**
   * Gets the indicating whether the configuration is empty.
   *
   * @return Returns {@code true} whether the configuration is empty, otherwise
   *         {@code false}.
   */
  public boolean empty() {
    return configs.isEmpty();
  }

  @Nullable
  protected <T extends IAuthenticationProperties> T getAuthenticationConfiguration(final Class<T> type) {
    if (configs.containsKey(type)) {
      return type.cast(configs.get(type));
    }
    return null;

  }

  /**
   * @param props
   * @return
   */
  public void applyDefaultValue() {
    if (empty()) {
      return;
    }

    if (getLdap() != null && !getLdap().empty()) {
      final LdapAuthenticationProperties defaultProperties = IAuthenticationProperties.defaultLdap();
      final LdapAuthenticationProperties current = getLdap();
      if (current.getUserSchema() == null) {
        current.setUserSchema(new UserLdapSchema());
      }
      if (current.getGroupSchema() == null) {
        current.setGroupSchema(new GroupLdapSchema());
      }
      if (current.getMembershipSchema() == null) {
        current.setMembershipSchema(new MembershipLdapSchema());
      }

      current.getUserSchema().override(defaultProperties.getUserSchema());
      current.getGroupSchema().override(defaultProperties.getGroupSchema());
      current.getMembershipSchema().override(defaultProperties.getMembershipSchema());
    }
    if (getActiveDirectory() != null && !getActiveDirectory().empty()) {
      final ActiveDirectoryAuthenticationProperties defaultProperties = IAuthenticationProperties
          .defaultActiveDirectory();
      final ActiveDirectoryAuthenticationProperties current = getActiveDirectory();

      if (current.getUserSchema() == null) {
        current.setUserSchema(new UserLdapSchema());
      }
      if (current.getGroupSchema() == null) {
        current.setGroupSchema(new GroupLdapSchema());
      }
      if (current.getMembershipSchema() == null) {
        current.setMembershipSchema(new MembershipLdapSchema());
      }

      current.getUserSchema().override(defaultProperties.getUserSchema());
      current.getGroupSchema().override(defaultProperties.getGroupSchema());
      current.getMembershipSchema().override(defaultProperties.getMembershipSchema());
    }

  }
}
