package com.pmi.tpd.core.user.impl;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.security.support.CommonValidations.validateUser;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.QGroupEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;

/**
 * Default JPA implementation of {@link IUserRepository}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Repository
public class JpaUserRepository extends DefaultJpaRepository<UserEntity, Long> implements IUserRepository {

  /**
   * @param entityManager
   *                      the JPA entity manager.
   */
  public JpaUserRepository(final EntityManager entityManager) {
    super(UserEntity.class, entityManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QUserEntity entity() {
    return QUserEntity.userEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public UserEntity getOne(final Long id) {
    // enforce the EntityNotFoundException
    return HibernateUtils.initialize(super.getOne(id));
  }

  @Override
  public UserEntity getById(final Long id) {
    // enforce the EntityNotFoundException
    return HibernateUtils.initialize(super.getById(id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean existsUser(@Nonnull final String username) {
    validateUser(username);
    final long i = count(QUserEntity.userEntity.username.equalsIgnoreCase(username));
    return i == 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean existsEmail(@Nonnull final String email) {
    checkHasText(email, "email");
    final long i = count(QUserEntity.userEntity.email.equalsIgnoreCase(email));
    return i == 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean existsEmail(@Nonnull final String email, @Nonnull final String usernameToExclude) {
    checkHasText(email, "email");
    checkHasText(usernameToExclude, "usernameToExclude");
    final long i = count(QUserEntity.userEntity.email.equalsIgnoreCase(email)
        .and(QUserEntity.userEntity.username.notEqualsIgnoreCase(usernameToExclude)));
    return i == 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public UserEntity findByName(@Nonnull final String login) {
    validateUser(login);
    return from().where(entity().username.eq(login)).fetchOne();
  }

  @Override
  @Nullable
  public UserEntity findBySlug(@Nonnull final String slug) {
    checkHasText(slug, "slug");
    return from().where(entity().slug.eq(slug)).fetchOne();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Long getIdForUserKey(@Nonnull final String userKey) {
    checkHasText(userKey, "userKey");
    return from().select(entity().id).where(entity().username.eq(userKey)).fetchOne();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Page<UserEntity> findUsers(@Nonnull final Pageable request) {
    checkNotNull(request, "request");
    Predicate predicate = null;
    if (request instanceof IFilterable) {
      final IFilterable filterable = (IFilterable) request;
      final String query = filterable.getQuery();
      if (StringUtils.isNotEmpty(query) && !"*".equals(query)) {
        predicate = DslPagingHelper.createLikeIgnoreCase(QUserEntity.userEntity.username, query)
            .or(DslPagingHelper.createLikeIgnoreCase(QUserEntity.userEntity.email, query))
            .or(DslPagingHelper.createLikeIgnoreCase(QUserEntity.userEntity.displayName, query));
      }
    }
    return findAll(predicate, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Page<UserEntity> findByName(@Nullable final String username, @Nonnull final Pageable request) {
    checkNotNull(request, "request");
    Predicate predicate = null;
    if (StringUtils.isNotEmpty(username)) {
      predicate = DslPagingHelper.createLikeIgnoreCase(QUserEntity.userEntity.username, username)
          .or(DslPagingHelper.createLikeIgnoreCase(QUserEntity.userEntity.displayName, username));
    }

    return findAll(predicate, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Page<UserEntity> findByDeletedDateEarlierThan(@Nonnull final Date date, final @Nonnull Pageable pageable) {
    checkNotNull(pageable, "request");
    checkNotNull(date, "date");
    return toPage(from().where(entity().deletedDate.before(date)), pageable);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional()
  @Override
  public void addGroupMember(final @Nonnull GroupEntity group, @Nonnull final UserEntity user) {
    checkNotNull(group, "group");
    checkNotNull(user, "user");
    this.saveAndFlush(user.copy().group(group).build());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Page<UserEntity> findUsersWithGroup(final @Nonnull String groupName, @Nonnull final Pageable pageRequest) {
    final Predicate predicates = DslPagingHelper.createPredicates(pageRequest, entity());
    return toPage(
        from().innerJoin(entity().groups, QGroupEntity.groupEntity)
            .where(QGroupEntity.groupEntity.name.equalsIgnoreCase(groupName))
            .where(predicates),
        pageRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nonnull
  public Page<UserEntity> findUsersWithoutGroup(final String groupName, final Pageable pageRequest) {
    final Predicate predicates = DslPagingHelper.createPredicates(pageRequest, entity());
    return toPage(from().where(entity().notIn(JPAExpressions.selectFrom(entity())
        .innerJoin(entity().groups, QGroupEntity.groupEntity)
        .where(QGroupEntity.groupEntity.name.equalsIgnoreCase(groupName)))).where(predicates),
        pageRequest);

  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public boolean removeGroupMember(@Nonnull final GroupEntity group, @Nonnull final UserEntity user) {
    checkNotNull(group, "group");
    validateUser(user);
    boolean removed = false;
    for (final GroupEntity grp : user.getGroups()) {
      if (group.equals(grp)) {
        user.getGroups().remove(grp);
        removed = true;
        break;
      }
    }
    if (removed) {
      this.saveAndFlush(user);
    }
    return removed;
  }

  @Override
  public UserEntity renameUser(final @Nonnull UserEntity user, @Nonnull final String newUsername) {
    validateUser(newUsername);
    return this.saveAndFlush(user.copy().username(newUsername).slug(UserEntity.slugify(newUsername)).build());
  }

}
