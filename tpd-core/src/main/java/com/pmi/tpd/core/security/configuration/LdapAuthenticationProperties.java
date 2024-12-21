package com.pmi.tpd.core.security.configuration;

import javax.annotation.Nullable;

import com.pmi.tpd.api.user.UserDirectory;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@Setter
public class LdapAuthenticationProperties implements IAuthenticationProperties {

  /** */
  private String name;

  /** */
  private boolean authenticationOnly;

  /** */
  private String hostname;

  /** */
  private Integer port;

  /** */
  private String username;

  /** */
  private String password;

  /** */
  private LdapSchema ldapSchema;

  /** */
  private UserLdapSchema userSchema;

  /** */
  private GroupLdapSchema groupSchema;

  /** */
  private MembershipLdapSchema membershipSchema;

  @Nullable
  public UserDirectory getDirectoryType() {
    if (empty()) {
      return null;
    }
    return authenticationOnly ? UserDirectory.InternalLdap : UserDirectory.Ldap;
  };

  public boolean empty() {
    return name == null;
  }

}
