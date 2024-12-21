package com.pmi.tpd.core.model.user;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import java.text.Normalizer;
import java.util.Date;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.IUserVisitor;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.model.AbstractAuditedEntityBuilder;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.database.support.IdentifierUtils;
import com.pmi.tpd.security.random.ISecureTokenGenerator;

/**
 * <p>
 * UserEntity class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Cacheable
@Entity(name = "User")
@Table(name = UserEntity.TABLE_NAME, uniqueConstraints = {
    @UniqueConstraint(name = "uc_t_userlogin_col", columnNames = { "login" }),
    @UniqueConstraint(name = "uc_t_userslug_col", columnNames = { "slug" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserEntity extends BaseAuditingEntity<Long> implements IUser, IInitializable {

  /**
   * The maximum length of a user slug
   */
  public static final int MAX_SLUG_LENGTH = 128;

  /**
   * The regex for user slugs
   */
  @SuppressWarnings("unused")
  private static final String SLUG_REGEXP = "[^\\\\/]+"; // anything besides a / or a \

  public static final int MAX_GENERATED_SLUG_LENGTH = MAX_SLUG_LENGTH - 1;

  public static final int MAX_SLUG_RETRY_COUNT = 10;

  /** generator identifier. */
  private static final String ID_GEN = "userIdGenerator";

  /** table name. */
  public static final String TABLE_NAME = "t_user";

  /** Generated user id. */
  @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
      pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
      valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME, pkColumnValue = "user_id", allocationSize = 1)
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
  private Long id;

  @Column(name = "slug", nullable = false, unique = true)
  private String slug;

  /** User email. */
  @Email
  @Size(max = 110)
  @Column(name = "email", length = 110)
  private String email;

  /** user unique identifier. */
  @NotNull
  @Pattern(regexp = "^[a-zA-Z0-9]*$")
  @Size(min = 1, max = 20)
  @Column(name = "login", nullable = false, updatable = true, unique = true, length = 20)
  private String username;

  /** display name. */
  @Size(max = 250)
  @Column(name = "display_name", length = 250)
  private String displayName;

  /** user password. */
  @NotNull
  @Column(name = "encoded_password", nullable = false)
  private String password;

  /** indicating user is active. */
  @Column(name = "activated")
  private boolean activated = false;

  /** list associated groups. */
  @ManyToMany(targetEntity = GroupEntity.class, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
      CascadeType.MERGE })
  @JoinTable(name = "t_user_group", joinColumns = {
      @JoinColumn(name = "user_fk", nullable = false, updatable = false) }, inverseJoinColumns = {
          @JoinColumn(name = "group_fk", nullable = false, updatable = false) })
  private Set<GroupEntity> groups;

  /** */
  @Enumerated(EnumType.STRING)
  @Column(name = "user_directory", length = 50, nullable = true)
  private UserDirectory directory;

  /** */
  @Column(name = "deleted_timestamp", nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date deletedDate;

  /**
   * Create new instance.
   */
  public UserEntity() {

  }

  /**
   * Create new instance and initialise with
   * {@link com.pmi.tpd.core.model.user.UserEntity.Builder}.
   *
   * @param builder
   *                contains value to initialise a new
   *                {@link com.pmi.tpd.core.model.user.UserEntity} instance.
   */
  public UserEntity(final Builder builder) {
    super(builder);
    this.id = builder.id();
    this.slug = builder.slug == null ? slugify(builder.username) : builder.slug;
    this.email = builder.email;
    this.displayName = builder.displayName;
    this.activated = builder.activated;
    this.groups = Sets.newHashSet(builder.groups);
    this.username = builder.username;
    this.password = builder.password;
    this.directory = builder.directory == null ? UserDirectory.defaultDirectory() : builder.directory;
    this.deletedDate = builder.deletedDate;
  }

  /** {@inheritDoc} */
  @Override
  public <T> T accept(@Nonnull final IUserVisitor<T> visitor) {
    return visitor.visit(this);
  }

  /** {@inheritDoc} */
  @Override
  public void initialize() {
    for (final GroupEntity grp : getGroups()) {
      // Don't use HibernateUtils.initialize here; it will cause a stack overflow
      Hibernate.initialize(grp);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return getUsername();
  }

  @Override
  public String getSlug() {
    return slug;
  }

  /** {@inheritDoc} */
  @Override
  public String getEmail() {
    return email;
  }

  /** {@inheritDoc} */
  public Set<GroupEntity> getGroups() {
    return groups;
  }

  /** {@inheritDoc} */
  @Override
  public Long getId() {
    return id;
  }

  /** {@inheritDoc} */
  @Override
  @Transient
  public String getDisplayName() {
    return this.displayName;
  }

  /** {@inheritDoc} */
  @Override
  public String getUsername() {
    return username;
  }

  /** {@inheritDoc} */
  @Override
  public String getPassword() {
    return password;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isActivated() {
    return activated && this.deletedDate == null;
  }

  @Override
  public UserDirectory getDirectory() {
    return directory;
  }

  /**
   * @return the timestamp when the user was deleted, or {@code null}. Note that
   *         this is only set after deletion and
   *         before the user has been cleaned up. In case the user gets
   *         "undeleted", it's cleared again.
   * @since 2.0
   */
  public Date getDeletedDate() {
    return deletedDate;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("id", this.getId())
        .add("username", username)
        .add("displayName", displayName)
        .add("email", email)
        .add("activated", activated)
        .add("directory", directory)
        .toString();
  }

  /**
   * Create new {@code Builder} instance and initialise with data of the current
   * instance.
   *
   * @return Returns new instance
   *         {@link com.pmi.tpd.core.model.user.UserEntity.Builder}.
   */
  @Nonnull
  public Builder copy() {
    return new Builder(this);
  }

  /**
   * <p>
   * builder.
   * </p>
   *
   * @return a {@link com.pmi.tpd.core.model.user.UserEntity.Builder} object.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Uses the provided {@link com.atlassian.security.random.SecureTokenGenerator}
   * to generate a user slug.
   *
   * @param tokenGenerator
   *                       the generator to use
   * @return the generated slug
   */
  public static String generateSlug(final ISecureTokenGenerator tokenGenerator) {
    String slug = tokenGenerator.generateToken();
    if (slug.length() > MAX_SLUG_LENGTH) { // No retries here, so we can use the full length
      // Generated tokens are currently always 40 characters long but the
      // SecureTokenGenerator contract states
      // they may be up to 255 characters
      slug = slug.substring(0, MAX_SLUG_LENGTH);
    }
    return slug;
  }

  /**
   * Slugifies a user name.
   * <p>
   * First the name is normalised to NFKD form; then any URL delimiters it
   * contains are replaced with underscores;
   * then it is truncated to {@link #MAX_GENERATED_SLUG_LENGTH 126 characters} (if
   * necessary); then it is converted to
   * lowercase.
   *
   * @param name
   *             the user name
   * @return the generated user slug
   */
  public static String slugify(final String name) {
    if (name == null) {
      return null;
    }
    String slug = Normalizer.normalize(name, Normalizer.Form.NFKD)
        // Note: The IDEA Pattern inspection is wrong here
        .replaceAll("[:/?#@!$&'()*+,;=%\\\\\\[\\]]", "_");
    if (slug.length() > MAX_GENERATED_SLUG_LENGTH) {
      slug = slug.substring(0, MAX_GENERATED_SLUG_LENGTH);
    }
    // username has IdentifierUtils.toLowerCase applied, so we do the same for the
    // slug
    return IdentifierUtils.toLowerCase(slug);
  }

  private static String checkSlug(final String slug) {
    checkNotNull(slug, "slug");
    state(!slug.trim().isEmpty(), "A non-blank slug is required");
    state(slug.length() <= MAX_SLUG_LENGTH, "The provided slug exceeds the maximum allowed length");

    final String check = slugify(slug);
    if (slug.length() < MAX_SLUG_LENGTH) {
      // If the slug is less than the max length, the slugify(String) should produce a
      // value which matches
      // its input value exactly. Otherwise, if any characters were replaced, it means
      // the provided slug
      // does not follow the rules for a slug and cannot be used
      state(slug.equals(check), "The provided slug is not valid (%s != %s)", slug, check);
    } else if (slug.length() == MAX_SLUG_LENGTH) {
      // If the slug is the maximum length, slugify(String) will truncate the
      // character at the end to ensure
      // there is one character left for a count. All other characters should be the
      // same, however
      state(slug.startsWith(check) && Character.isDigit(slug.charAt(MAX_GENERATED_SLUG_LENGTH)),
          "The provided slug is not valid (%s != %s with a trailing digit)",
          slug,
          check);
    }
    return slug;
  }

  /**
   * @author Christophe Friederich
   * @since 1.0
   */
  public static class Builder extends AbstractAuditedEntityBuilder<Long, UserEntity, Builder> {

    /** */
    private String slug;

    /** */
    private String email;

    /** */
    private String displayName;

    /** */
    private String username;

    /** */
    private String password;

    /** */
    private boolean activated;

    /** */
    private UserDirectory directory;

    /** */
    private final Set<GroupEntity> groups;

    /** */
    private Date deletedDate;

    /**
     *
     */
    protected Builder() {
      this.groups = Sets.newHashSet();
    }

    /**
     * Create new Builder with {@code user}.
     *
     * @param user
     *             a user entity to use.
     */
    protected Builder(@Nonnull final UserEntity user) {
      super(user);
      this.email = user.email;
      this.slug = user.slug;
      this.displayName = user.displayName;
      this.activated = user.activated;
      this.groups = Sets.newHashSet(user.groups);
      this.username = checkHasText(user.username, "user.login");
      this.password = user.password;
      this.directory = user.directory == null ? UserDirectory.defaultDirectory() : user.directory;
      this.deletedDate = user.deletedDate;
    }

    @Nonnull
    public Builder slug(@Nonnull final String value) {
      slug = checkSlug(value);

      return self();
    }

    /**
     * @param activated
     *                  indicate if the user is active or not.
     * @return Returns fluent {@link Builder}.
     */
    public Builder activated(final boolean activated) {
      this.activated = activated;
      return self();
    }

    /**
     * @param email
     * @return Returns fluent {@link Builder}.
     */
    public Builder email(final String email) {
      this.email = email;
      return self();
    }

    /**
     * @param displayName
     * @return Returns fluent {@link Builder}.
     */
    public Builder displayName(final String displayName) {
      this.displayName = displayName;
      return self();
    }

    /**
     * @param username
     * @return Returns fluent {@link Builder}.
     */
    public Builder username(final String username) {
      this.username = username;
      return self();
    }

    /**
     * @param password
     * @return Returns fluent {@link Builder}.
     */
    public Builder password(final String password) {
      this.password = password;
      return self();
    }

    /**
     * @param password
     * @return Returns fluent {@link Builder}.
     */
    public Builder directory(final UserDirectory directory) {
      this.directory = directory;
      return self();
    }

    /**
     * @param value
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    public Builder group(@Nullable final GroupEntity value) {
      return groups(value);
    }

    /**
     * @param values
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    public Builder groups(@Nullable final Iterable<GroupEntity> values) {
      addIf(Predicates.<GroupEntity>notNull(), groups, values);
      return self();
    }

    /**
     * @param value
     *               the first group to add
     * @param values
     *               a varargs array containing 0 or more groups to add after the
     *               first
     * @return Returns fluent {@link Builder}.
     */
    @Nonnull
    public Builder groups(@Nullable final GroupEntity value, @Nullable final GroupEntity... values) {
      addIf(Predicates.<GroupEntity>notNull(), groups, value, values);

      return self();
    }

    /**
     * @param directory
     * @return
     * @since 2.0
     */
    public Builder deletedDate(final Date value) {
      this.deletedDate = value;
      return self();
    }

    @Override
    public UserEntity build() {
      return new UserEntity(this);
    }

    @Override
    protected Builder self() {
      return this;
    }

  }
}
