package com.pmi.tpd.api.user;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.util.BuilderSupport;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * UserRequest class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Schema(name = "User", description = "Contains user information")
@JsonDeserialize(builder = UserRequest.Builder.class)
public class UserRequest implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  /** */
  @Pattern(regexp = "^[a-zA-Z0-9]*$")
  @NotNull
  @Size(min = 1, max = 20)
  private String username;

  /** */
  @Size(min = 5, max = 100)
  private String password;

  /** */
  @Size(max = 250)
  private String displayName;

  /** */
  @Email
  @Size(min = 5, max = 100)
  private String email;

  /** */
  @Size(min = 2, max = 5)
  private String langKey;

  /** */
  private boolean activated;

  /** */
  private UserDirectory directory;

  /** */
  private Set<String> authorities;

  /** */
  private boolean deletable;

  /** */
  private boolean updatable;

  /** */
  private boolean groupUpdatable;

  /** */
  private String directoryName;

  /** */
  private String avatarUrl;

  /** */
  /**
   * <p>
   * Constructor for UserRequest.
   * </p>
   */
  public UserRequest() {
  }

  /**
   * <p>
   * Constructor for UserRequest.
   * </p>
   *
   * @param builder
   *                a {@link com.pmi.tpd.api.user.UserRequest.Builder} object.
   */
  public UserRequest(final Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.displayName = builder.displayName;
    this.email = builder.email;
    this.langKey = builder.langKey;
    this.authorities = Sets.newHashSet(builder.authorities);
    this.activated = builder.activated;
    this.directory = builder.directory;
    this.deletable = builder.deletable;
    this.updatable = builder.updatable;
    this.groupUpdatable = builder.groupUpdatable;
    this.directoryName = builder.directoryName;
    this.avatarUrl = builder.avatarUrl;
  }

  /**
   * <p>
   * Getter for the field <code>password</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the user password", required = true, hidden = true)
  @JsonIgnore
  public String getPassword() {
    return password;
  }

  /**
   * <p>
   * Getter for the field <code>username</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the user name", required = true)
  public String getUsername() {
    return username;
  }

  /**
   * <p>
   * Getter for the field <code>displayName</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the display name")
  public String getDisplayName() {
    return displayName;
  }

  /**
   * <p>
   * Getter for the field <code>email</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the email address")
  public String getEmail() {
    return email;
  }

  /**
   * <p>
   * Getter for the field <code>langKey</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the language ISO code")
  public String getLangKey() {
    return langKey;
  }

  /**
   * <p>
   * Getter for the field <code>avatarUrl</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Schema(description = "Gets the avatar url")
  public String getAvatarUrl() {
    return avatarUrl;
  }

  /**
   * <p>
   * Getter for the field <code>authorities</code>.
   * </p>
   *
   * @return a {@link java.util.Set} object.
   */
  @ArraySchema(schema = @Schema(implementation = String.class, description = "Gets the list of authorities"))
  public Set<String> getAuthorities() {
    return authorities;
  }

  /**
   * <p>
   * isActivated.
   * </p>
   *
   * @return a boolean.
   */
  @Schema(description = "Gets the indicating whether the user is activated")
  public boolean isActivated() {
    return activated;
  }

  /**
   * @return Returns a {@link UserDirectory} representing the user directory used
   *         for this user.
   */
  @Schema(description = "Gets the type of security user directory used to store it.")
  public UserDirectory getDirectory() {
    return directory;
  }

  /**
   * @return
   * @since 2.0
   */
  public boolean isUpdatable() {
    return updatable;
  }

  /**
   * @return
   * @since 2.0
   */
  public boolean isGroupUpdatable() {
    return groupUpdatable;
  }

  /**
   * @return
   * @since 2.0
   */
  public boolean isDeletable() {
    return deletable;
  }

  /**
   * @return
   * @since 2.0
   */
  public String getDirectoryName() {
    return directoryName;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("displayName", displayName)
        .add("username", username)
        .add("email", email)
        .add("directory", directory)
        .add("directoryName", directoryName)
        .add("activated", activated)
        .toString();
  }

  /**
   * Convert the this instance to {@link IUser}.
   *
   * @return Returns a {@link IUser} representing this user.
   */
  public IUser toUser() {
    return User.builder()
        .activated(isActivated())
        .email(getEmail())
        .displayName(getDisplayName())
        .username(getUsername())
        .directory(getDirectory())
        .password(getPassword())
        .build();
  }

  /**
   * <p>
   * builder.
   * </p>
   *
   * @return a {@link com.pmi.tpd.api.user.UserRequest.Builder} object.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * <p>
   * builder.
   * </p>
   *
   * @param user
   *             a {@link com.pmi.tpd.api.user.IUser} object.
   * @return a {@link com.pmi.tpd.api.user.UserRequest.Builder} object.
   */
  public static Builder builder(final IUser user) {
    return new Builder(user);
  }

  /**
   * @return
   */
  public Builder copy() {
    return new Builder(this);
  }

  /**
   * @author Christophe Friederich
   * @since 1.0
   */
  @JsonPOJOBuilder(withPrefix = "")
  @JsonIgnoreProperties({ "displayName" })
  public static class Builder extends BuilderSupport {

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private String displayName;

    /** */
    private String email;

    /** */
    private String langKey;

    /** */
    private boolean activated;

    /** */
    private UserDirectory directory;

    /** */
    private final Set<String> authorities;

    /** */
    private boolean deletable;

    /** */
    private boolean updatable;

    /** */
    private boolean groupUpdatable;

    /** */
    private String directoryName;

    private String avatarUrl;

    /**
     *
     */
    public Builder() {
      this.authorities = Sets.newHashSet();
    }

    /**
     * Populate the builder for the specific {@link IUser user}.
     *
     * @param user
     *             a user to use.
     */
    public Builder(@Nonnull final IUser user) {
      this.username = user.getUsername();
      this.password = user.getPassword();
      this.displayName = user.getDisplayName();
      this.email = user.getEmail();
      this.authorities = Sets.newHashSet();
      this.activated = user.isActivated();
      this.directory = user.getDirectory() == null ? UserDirectory.defaultDirectory() : user.getDirectory();
    }

    /**
     * @param user
     */
    public Builder(@Nonnull final UserRequest user) {
      this.username = user.getUsername();
      this.password = user.getPassword();
      this.displayName = user.getDisplayName();
      this.email = user.getEmail();
      this.authorities = Sets.newHashSet(user.getAuthorities());
      this.activated = user.isActivated();
      this.directory = user.getDirectory() == null ? UserDirectory.defaultDirectory() : user.getDirectory();
      this.deletable = user.deletable;
      this.updatable = user.updatable;
      this.groupUpdatable = user.groupUpdatable;
      this.directoryName = user.directoryName;
      this.avatarUrl = user.avatarUrl;
    }

    /**
     * @param username
     *                 a username
     * @return Returns fluent {@link Builder}.
     */
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    @NotNull
    @Size(min = 1, max = 20)
    public Builder username(final String username) {
      this.username = username;
      return this;
    }

    /**
     * @param password
     *                 a password
     * @return Returns fluent {@link Builder}.
     */
    @NotNull
    @Size(min = 5, max = 100)
    public Builder password(final String password) {
      this.password = password;
      return this;
    }

    /**
     * @param displayName
     *                    a display name.
     * @return Returns fluent {@link Builder}.
     */
    @Size(max = 250)
    public Builder displayName(final String displayName) {
      this.displayName = displayName;
      return this;
    }

    /**
     * @param email
     *              a email.
     * @return Returns fluent {@link Builder}.
     */
    @Email
    @Size(min = 5, max = 100)
    public Builder email(final String email) {
      this.email = email;
      return this;
    }

    /**
     * @param langKey
     *                a language key
     * @return Returns fluent {@link Builder}.
     */
    @Size(min = 2, max = 5)
    public Builder langKey(final String langKey) {
      this.langKey = langKey;
      return this;
    }

    /**
     * @param avatarUrl
     *                  a avatar Url
     * @return Returns fluent {@link Builder}.
     */
    public Builder avatarUrl(final String avatarUrl) {
      this.avatarUrl = avatarUrl;
      return this;
    }

    /**
     * @param activated
     *                  indicate if user is activate or not.
     * @return Returns fluent {@link Builder}.
     */
    public Builder activated(final boolean activated) {
      this.activated = activated;
      return this;
    }

    /**
     * @param directory
     *                  the type of user directory used.
     * @return Returns fluent {@link Builder}.
     */
    public Builder directory(final UserDirectory directory) {
      this.directory = directory;
      return this;
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    @JsonIgnore
    public Builder authority(@Nullable final String permission) {
      return authorities(permission);
    }

    /**
     * @param values
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    public Builder authorities(@Nullable final Iterable<String> values) {
      addIf(Predicates.<String>notNull(), authorities, values);
      return this;
    }

    /**
     * @param value
     * @param values
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    @JsonIgnore
    public Builder authorities(@Nullable final String value, @Nullable final String... values) {
      addIf(Predicates.<String>notNull(), authorities, value, values);
      return this;
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     * @since 2.0
     */
    public Builder deletable(final boolean value) {
      this.deletable = value;
      return this;
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     * @since 2.0
     */
    public Builder updatable(final boolean value) {
      this.updatable = value;
      return this;
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     * @since 2.0
     */
    public Builder groupUpdatable(final boolean value) {
      this.groupUpdatable = value;
      return this;
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     * @since 2.0
     */
    public Builder directoryName(final String value) {
      this.directoryName = value;
      return this;
    }

    /**
     * @return Returns new instance of {@link UserRequest}.
     */
    public UserRequest build() {
      return new UserRequest(this);
    }

  }

}
