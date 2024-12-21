package com.pmi.tpd.core.user.spi;

import static com.pmi.tpd.api.paging.PageUtils.newRequest;
import static org.hamcrest.Matchers.hasItem;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.ContextConfiguration;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.api.paging.IPageProvider;
import com.pmi.tpd.api.paging.PagedIterable;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.DaoCoreConfig;
import com.pmi.tpd.core.DataSets;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.UserEntity;

@Configuration
@ContextConfiguration(classes = { DaoCoreConfig.class, UserRepositoryIT.class })
@DatabaseSetup(value = { DataSets.USERS })
public class UserRepositoryIT extends BaseDaoTestIT {

  @Inject
  private IUserRepository repository;

  @Inject
  private IGroupRepository groupRepository;

  @Test
  public void testCreateUser() {
    final UserEntity user = UserEntity.builder().username("bturner").password("pwd").build();
    final UserEntity created = repository.save(user);
    flush();
    clear();

    assertNotNull(created.getId());
    assertEquals(user, repository.getById(created.getId()));

  }

  @Test
  public void testCreateUserWithUppercase() {
    final UserEntity user = UserEntity.builder().username("Bturner").password("pwd").build();
    final UserEntity created = repository.save(user);
    flush();
    clear();

    assertNotNull(created.getId());
    assertEquals(user, repository.getById(created.getId()));

  }

  @Test
  public void testCreateAndRetrieveWithCjkName() {
    final String cjkName = "\u5718\u96c6";

    final UserEntity user = UserEntity.builder().username(cjkName).password("pwd").build();
    final UserEntity created = repository.save(user);
    flush();
    clear();

    assertEquals(cjkName, created.getName());
    assertEquals(user, repository.getById(created.getId()));
  }

  @Test
  public void testUserExists() {
    assertTrue(repository.existsUser("user"));
    assertFalse(repository.existsUser("toto"));
  }

  @Test
  public void testExistsEmail() {
    assertTrue(repository.existsEmail("user@company.com"));
    assertFalse(repository.existsEmail("toto@company.com"));
  }

  @Test
  public void testExistsEmailExcludingUser() {
    assertTrue(repository.existsEmail("user@company.com", "admin"));
    assertFalse(repository.existsEmail("user@company.com", "user"));
  }

  @Test
  public void testGetIdForUserKey() {
    assertEquals(Long.valueOf(3), repository.getIdForUserKey("user"));
    assertNull(repository.getIdForUserKey("toto"));
  }

  @Test
  public void testDelete() {
    final UserEntity user = repository.getById(10L);
    assertNotNull(user);
    repository.delete(user);
    flush();
    clear();

    assertFalse(repository.findById(10L).isPresent(), "user should be removed");
  }

  @Test
  public void testFindUsers() {
    final Page<UserEntity> page = repository.findUsers(newRequest(0, 10));
    assertNotNull(page);
    assertEquals(6, page.getTotalElements());

    checkAllUsers(page.getContent());
  }

  @Test
  public void testFindUsersWithQuery() {
    // search for username
    Page<UserEntity> page = repository.findUsers(newRequest(0, 10, (Sort) null, null, "user"));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    Iterator<UserEntity> users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());

    // search for email
    page = repository.findUsers(newRequest(0, 10, (Sort) null, null, "user@company.com"));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());

    // search for display name
    page = repository.findUsers(newRequest(0, 10, (Sort) null, null, "lastName firstName"));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());
  }

  @Test
  public void testFindUsersWithFilter() {
    // search for username
    final Page<UserEntity> page = repository
        .findUsers(newRequest(0, 10, (Sort) null, new Filters(Filter.eq("username", "user")), null));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    final Iterator<UserEntity> users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());

  }

  @Test
  public void testFindByNameWithQuery() {
    // search for username
    Page<UserEntity> page = repository.findByName("user", newRequest(0, 10));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    Iterator<UserEntity> users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());

    // search for display name
    page = repository.findByName("lastName firstName", newRequest(0, 10));
    assertNotNull(page);
    assertEquals(1, page.getTotalElements());

    users = page.getContent().iterator();
    assertEquals(3, users.next().getId().longValue());

    // find all
    page = repository.findByName(null, newRequest(0, 10));
    assertNotNull(page);
    assertEquals(6, page.getTotalElements());

    checkAllUsers(page.getContent());
  }

  @Test
  public void testFindAll() {
    Page<UserEntity> page = repository.findAll(newRequest(0, 3));
    assertNotNull(page);
    assertEquals(3, page.getNumberOfElements());

    Iterator<UserEntity> users = page.getContent().iterator();
    assertEquals(1, users.next().getId().longValue());
    assertEquals(2, users.next().getId().longValue());
    assertEquals(3, users.next().getId().longValue());
    assertFalse(users.hasNext());

    page = repository.findAll(page.nextPageable());
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(3, page.getNumberOfElements());

    users = page.getContent().iterator();
    assertEquals(10, users.next().getId().longValue());
    assertEquals(11, users.next().getId().longValue());
    assertEquals(12, users.next().getId().longValue());
    assertFalse(users.hasNext());
  }

  @Test
  public void testFindAllWithPredicate() {
    final Page<UserEntity> page = repository.findAll(repository.entity().id.mod(2L).eq(0L), newRequest(0, 13));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(3, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(2, users.get(0).getId().longValue());
    assertEquals(10, users.get(1).getId().longValue());
    assertEquals(12, users.get(2).getId().longValue());
  }

  @Test
  public void testFindByName() {
    assertEquals(1, repository.findByName("sysadmin").getId().intValue());
    assertNull(repository.findByName("sadmin")); // Not checking partial matches
  }

  @Test
  public void testGetOne() {
    assertEquals(1L, repository.getById(1L).getId().longValue());
    try {
      repository.getById(100L);
    } catch (final JpaObjectRetrievalFailureException | EntityNotFoundException ex) {
      // good
    } catch (final Exception ex) {
      fail("wrong exception");
    }
  }

  @Test
  public void testUpdate() {
    final UserEntity initial = repository.getById(1L);
    final UserEntity modified = initial.copy().username("sysadmin").build();
    repository.save(modified);

    flush();
    clear();

    final UserEntity updated = repository.getById(1L);
    assertEquals("sysadmin", updated.getName());
  }

  @Test
  public void testFindByDeletedDateEarlierThan() throws Exception {
    final Instant now = Instant.now();
    final UserEntity deleted1 = createUserDeletedAt(now.minus(Duration.standardHours(3)).toDate());
    final UserEntity deleted2 = createUserDeletedAt(now.minus(Duration.standardHours(1)).toDate());
    // Add one with a null date to make sure it is not returned
    createUserDeletedAt(null);

    final Collection<UserEntity> resultEarlier = findByDeletedDateEarlierThan(now.minus(Duration.standardHours(4)));
    assertEquals(0, resultEarlier.size());

    final Collection<UserEntity> resultBetween = findByDeletedDateEarlierThan(now.minus(Duration.standardHours(2)));
    assertEquals(1, resultBetween.size());
    assertThat(resultBetween, hasItem(deleted1));

    final Collection<UserEntity> resultLater = findByDeletedDateEarlierThan(now);
    assertEquals(2, resultLater.size());
    assertThat(resultLater, hasItem(deleted1));
    assertThat(resultLater, hasItem(deleted2));
  }

  @Test
  public void testfindUsersByGroup() {
    final Page<UserEntity> page = repository.findUsersWithGroup("grp-user", newRequest(0, 10));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(2, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(2, users.get(0).getId().longValue());
    assertEquals(3, users.get(1).getId().longValue());
  }

  @Test
  public void testfindUsersWithGroupWithFilter() {
    final Page<UserEntity> page = repository.findUsersWithGroup("grp-user",
        newRequest(0, 10, null, new Filters(Filter.eq("username", "user")), null));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(1, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(3, users.get(0).getId().longValue());
  }

  @Test
  public void testFindUsersWithoutGroup() {
    final Page<UserEntity> page = repository.findUsersWithoutGroup("grp-user", newRequest(0, 10));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(4, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(1, users.get(0).getId().longValue());
    assertEquals(10, users.get(1).getId().longValue());
    assertEquals(11, users.get(2).getId().longValue());
    assertEquals(12, users.get(3).getId().longValue());
  }

  @Test
  public void testFindUsersWithoutGroupWithFilter() {
    final Page<UserEntity> page = repository.findUsersWithoutGroup("grp-user",
        newRequest(0, 10, null, new Filters(Filter.eq("username", "access.key.1")), null));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(1, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(10, users.get(0).getId().longValue());
  }

  @Test
  public void testAddGroupMember() {
    UserEntity user = repository.getById(3L);
    final GroupEntity group = GroupEntity.builder().name("new-group").build();
    groupRepository.saveAndFlush(group);
    this.repository.addGroupMember(group, user);

    user = repository.getById(3L);
    assertTrue(FluentIterable.from(user.getGroups()).anyMatch(grp -> "new-group".equals(grp.getName())));
  }

  @Test
  public void testRemoveGroupMember() {
    final UserEntity user = repository.getById(3L);
    final GroupEntity group = groupRepository.findByName("grp-user");
    assertTrue(this.repository.removeGroupMember(group, user));

    final Page<UserEntity> page = repository.findUsersWithGroup("grp-user", newRequest(0, 10));
    assertNotNull(page);
    assertTrue(page.isLast());
    assertEquals(1, page.getTotalElements());

    final List<UserEntity> users = ImmutableList.copyOf(page.getContent());
    assertEquals(2, users.get(0).getId().longValue());

  }

  @Test
  public void testRemoveGroupMemberNotBelongToUser() {
    final UserEntity user = repository.getById(3L);
    final GroupEntity group = groupRepository.findByName("grp-administrator");
    assertFalse(this.repository.removeGroupMember(group, user));

  }

  private UserEntity createUserDeletedAt(final Date date) {
    final UserEntity user = UserEntity.builder()
        .username("del-" + (date == null ? "null" : date.getTime()))
        .password("pwd")
        .deletedDate(date)
        .build();
    return repository.save(user);
  }

  private List<UserEntity> findByDeletedDateEarlierThan(final Instant date) {
    return Lists.newArrayList(new PagedIterable<>(
        (IPageProvider<UserEntity>) request -> repository.findByDeletedDateEarlierThan(date.toDate(), request),
        10));
  }

  private void checkAllUsers(final List<UserEntity> list) {
    final Iterator<UserEntity> users = list.iterator();
    assertEquals(1, users.next().getId().longValue());
    assertEquals(2, users.next().getId().longValue());
    assertEquals(3, users.next().getId().longValue());
    assertEquals(10, users.next().getId().longValue());
    assertEquals(11, users.next().getId().longValue());
    assertEquals(12, users.next().getId().longValue());
    assertFalse(users.hasNext());
  }

}
