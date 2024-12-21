package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.security.permission.Permission.USER;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.service.testing.cluster.SpringManagedCluster;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.IUserVisitor;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.database.support.IdentifierUtils;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;

public class CachingPermissionGraphFactoryTest extends MockitoTestCase {

    // test with strong consistency guarantees, even though the production code will run looser consistency guarantees
    // (eventually consistent) because of the near-cache setting on the map. If this test ran with eventually consistent
    // settings, we'd end up with a flakey test
    @RegisterExtension
    public static SpringManagedCluster cluster = new SpringManagedCluster.Builder().size(2).build();

    @Mock(lenient = true)
    private IEffectivePermissionRepository effectivePermissionDao;

    private CachingPermissionGraphFactory factory;

    private CachingPermissionGraphFactory verificationFactory;

    private Multimap<String, GrantedPermission> groupPermissions;

    @Mock(lenient = true)
    private ISubmissionEntity submission1;

    @Mock(lenient = true)
    private ISubmissionEntity submission2;

    @Mock(lenient = true)
    private ISubmissionEntity submission3;

    @Mock
    private ProductEntity product1;

    @Mock
    private PlatformTransactionManager transactionManager;

    private UserEntity user;

    private Multimap<Long, GrantedPermission> userPermissions;

    @Mock
    private IUserService userService;

    @BeforeEach
    public void setUp() {
        user = createMockUserEntity(1, "test-user");

        // create mock submissions
        when(submission1.getId()).thenReturn(1L);
        when(submission2.getId()).thenReturn(2L);
        when(submission3.getId()).thenReturn(3L);

        // mock default submission permissions: s1: WRITE, s2: READ, s3: NONE
        // when(submissionPermissionDao.findDefaultPermissions(any(Pageable.class)))
        // .thenAnswer(new Answer<Page<InternalProjectPermission>>() {
        //
        // @Override
        // public Page<SubmissionPermission> answer(final InvocationOnMock invocation) throws Throwable {
        // return PageUtils.createPage(Lists.newArrayList(
        // SubmissionPermission.builder().project(submission1).permission(SUBMISSION_WRITE).build(),
        // SubmissionPermission.builder().project(submission2).permission(SUBMISSION_READ).build()),
        // (Pageable) invocation.getArguments()[0]);
        // }
        // });

        groupPermissions = HashMultimap.create(10, 10);
        userPermissions = HashMultimap.create(10, 10);

        // mock findByGroup backed by the groupPermissions multimap
        when(effectivePermissionDao.findByGroup(anyString(), any(Pageable.class))).thenAnswer(invocation -> {
            final String group = IdentifierUtils.toLowerCase((String) invocation.getArguments()[0]);
            final List<GrantedPermission> results = new ArrayList<>();
            final Collection<GrantedPermission> groupPerms = groupPermissions.get(group);
            if (groupPerms != null && !groupPerms.isEmpty()) {
                results.addAll(groupPerms);
            }

            return PageUtils.createPage(results, (Pageable) invocation.getArguments()[1]);
        });

        // mock findByUserId backed by the userPermissions multimap
        when(effectivePermissionDao.findByUserId(any(Long.class), any(Pageable.class))).thenAnswer(invocation -> {
            final Long userId = (Long) invocation.getArguments()[0];
            final List<GrantedPermission> results = new ArrayList<>();
            final Collection<GrantedPermission> perms = userPermissions.get(userId);
            if (perms != null && !perms.isEmpty()) {
                results.addAll(perms);
            }

            return PageUtils.createPage(results, (Pageable) invocation.getArguments()[1]);
        });

        final SessionFactory sessionFactory = mock(SessionFactory.class, Mockito.RETURNS_DEEP_STUBS);
        cluster.registerBeans(cluster, effectivePermissionDao, transactionManager, sessionFactory);

        // initialize the factory
        factory = buildFactory(0);
        factory.warmCaches();

        verificationFactory = buildFactory(1);
        verificationFactory.warmCaches();
    }

    @AfterEach
    public void teardown() {
        cluster.reset();
    }

    @Test
    public void testAnyPermission() {
        // tests whether passing a null resourceId returns 'permission on any resource'
        mockNoGroupMemberships(user);
        final IPermissionGraph graph = factory.createGraph(user);

        assertFalse(graph.isGranted(USER, null));
    }

    // @Test
    // public void testDefaultPermissions() {
    // // mock empty lists to be returned from the user/group permission queries
    // mockNoGroupMemberships(user);
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, submission1));
    // // check inherited permissions as well
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission1));
    // assertTrue(graph.isGranted(SUBMISSION_VIEW, submission1));
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission2));
    // assertTrue(graph.isGranted(SUBMISSION_VIEW, submission2));
    //
    // // make sure no other permissions are granted
    // assertFalse(graph.isGranted(SUBMISSION_VIEW, submission3));
    // assertFalse(graph.isGranted(SUBMISSION_CREATE, submission2));
    // assertFalse(graph.isGranted(SUBMISSION_ADMIN, submission1));
    // }
    //
    // @Test
    // public void testGroupPermissions() {
    // mockGroupMemberships(user, "SubmissionAdmin");
    // final UserEntity user2 = createMockUserEntity(2, "other");
    // mockGroupMemberships(user2, "app-user", "bla");
    //
    // mockPermissions(GlobalPermissionEntity.builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // GlobalPermissionEntity.builder().group("app-user").permission(USER).build(),
    // GlobalPermissionEntity.builder().group("bla").permission(USER).build());
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph.isGranted(USER, null)); // inherited from SUBMISSION_CREATE
    // assertFalse(graph.isGranted(SYS_ADMIN, null));
    //
    // final IPermissionGraph graph2 = factory.createGraph(user2);
    // assertTrue(graph2.isGranted(USER, null));
    // assertFalse(graph2.isGranted(SUBMISSION_CREATE, null));
    // }
    //
    // @Test
    // public void testMixedPermissions() {
    // // tests case where both user and group level permissions are granted
    // mockGroupMemberships(user, "SubmisionAdmin");
    //
    // mockPermissions(GlobalPermissionEntity.builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // GlobalPermissionEntity.builder().user(user).permission(SUBMISSION_ADMIN).submission(submission3).build());
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    //
    // // check group permissions
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph.isGranted(USER, null)); // inherited from SUBMISSION_CREATE
    // assertFalse(graph.isGranted(ADMIN, null));
    //
    // // check user permissions
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission3));
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission));
    // assertFalse(graph.isGranted(SUBMISSION_ADMIN, submission2));
    //
    // // check default permissions
    // assertFalse(graph.isGranted(SUBMISSION__ADMIN, submission1));
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission1));
    // assertFalse(graph.isGranted(SUBMISSION_SEND, submission2));
    // assertTrue(graph.isGranted(SUBMISSION_READ, submission2));
    // }
    //
    // @Test
    // public void testMixedPermissionsWithTooSmallCache() {
    // final UserEntity user2 = createMockUserEntity(2, "user-2");
    // final UserEntity user3 = createMockUserEntity(2, "user-3");
    //
    // // tests case where both user and group level permissions are granted
    // mockGroupMemberships(user, "SubmissionAdmin", "app-users");
    // mockGroupMemberships(user2, "app-users");
    // mockGroupMemberships(user3, "app-users");
    //
    // mockPermissions(GlobalPermissionEntity.builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // GlobalPermissionEntity.builder().group("app-users").permission(USER).build(),
    // GlobalPermissionEntity.builder().user(user).permission(SUBMISSION_ADMIN).submission(submission3).build(),
    // GlobalPermissionEntity.builder().user(user2).permission(SUBMISSION_READ).submission(submission3).build(),
    // GlobalPermissionEntity.builder().user(user3).permission(SUBMISSION_READ).submission(submission3).build());
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission3));
    //
    // final IPermissionGraph graph2 = factory.createGraph(user2);
    // assertTrue(graph2.isGranted(USER, null));
    // assertTrue(graph2.isGranted(SUBMISSION_READ, submission3));
    // assertFalse(graph2.isGranted(SUBMISSION_ADMIN, submission3));
    //
    // final IPermissionGraph graph3 = factory.createGraph(user3);
    // assertTrue(graph3.isGranted(USER, null));
    // assertTrue(graph3.isGranted(SUBMISSION_READ, submission3));
    // assertFalse(graph3.isGranted(SUBMISSION_ADMIN, submission3));
    // }
    //
    // @Test
    // public void testOnPermissionEventDefaultPermission() {
    // mockNoGroupMemberships(user);
    //
    // IPermissionGraph graph = factory.createGraph(user);
    // verificationFactory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission1)); // granted through default submission permission on
    // // p1
    //
    // // revoke the default permission on p1
    // final SubmissionPermissionRevokedEvent event = mock(SubmissionPermissionRevokedEvent.class);
    // when(event.getSubmission()).thenReturn(submission1);
    // when(event.getPermission()).thenReturn(SUBMISSION_SEND);
    // factory.onPermissionsChanged(event);
    //
    // assertFalse(graph.isGranted(SUBMISSION_SEND, submission1)); // should be revoked now
    //
    // // verify on the other Hazelcast instance as well
    // graph = verificationFactory.createGraph(user);
    // assertFalse(graph.isGranted(SUBMISSION_SEND, submission1)); // should be revoked now
    //
    // // now grant a new default permission
    // final SubmissionPermissionGrantedEvent event2 = mock(SubmissionPermissionGrantedEvent.class);
    // when(event2.getSubmission()).thenReturn(submission1);
    // when(event2.getPermission()).thenReturn(SUBMISSION_READ);
    // factory.onPermissionsChanged(event2);
    //
    // // verify on the other Hazelcast instance
    // graph = verificationFactory.createGraph(user);
    // assertFalse(graph.isGranted(SUBMISSION_SEND, submission1)); // should still be revoked
    // assertTrue(graph.isGranted(SUBMISSION_READ, submission1)); // should be granted
    //
    // // the DAO should be hit only once, all subsequent updates are in-memory without hitting the DAO
    // verify(effectivePermissionDao).findByUserId(eq(1L), any(Pageable.class));
    // }
    //
    // @Test
    // public void testOnPermissionModifiedEventDefaultPermission() {
    // mockNoGroupMemberships(user);
    //
    // IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_SEND, submission1)); // granted through default submission permission on
    // // p1
    //
    // // modify the default permission on p1
    // final SubmissionPermissionModifiedEvent modifiedEvent = mock(SubmissionPermissionModifiedEvent.class);
    // when(modifiedEvent.getSubmission()).thenReturn(submission1);
    // when(modifiedEvent.getPermission()).thenReturn(SUBMISSION_READ);
    // factory.onPermissionsChanged(modifiedEvent);
    //
    // graph = factory.createGraph(user);
    // assertFalse(graph.isGranted(SUBMISSION_SEND, submission1)); // should be revoked
    // assertTrue(graph.isGranted(SUBMISSION_READ, submission1)); // should be granted
    //
    // }
    //
    // @Test
    // public void testOnPermissionEventNonDefault() {
    // mockGroupMemberships(user, "SubmissionAdmin", "master-of-the-universe");
    //
    // mockPermissions(GlobalPermissionEntity.builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // SubmissionPermissionEntity.builder()
    // .user(user)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission1)
    // .build());
    //
    // IPermissionGraph graph = factory.createGraph(user);
    //
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission1));
    // assertFalse(graph.isGranted(SUBMISSION_ADMIN, submission2));
    // assertFalse(graph.isGranted(SUBMISSION_ADMIN, submission3));
    //
    // // simulate that someone granted SUBMISSION_ADMIN to user on p2
    // SubmissionPermissionGrantedEvent event = mock(SubmissionPermissionGrantedEvent.class);
    // when(event.getSubmission()).thenReturn(submission2);
    // when(event.getAffectedUser()).thenReturn(user);
    // when(event.getPermission()).thenReturn(SUBMISSION_ADMIN);
    //
    // resetMockedPermissions();
    // mockPermissions(GlobalPermissionEntity.builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // SubmissionPermissionEntity.builder()
    // .user(user)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission1)
    // .build(),
    // SubmissionPermissionEntity.builder()
    // .user(user)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission2)
    // .build());
    //
    // factory.onPermissionsChanged(event);
    //
    // // verify on the other Hazelcast instance
    // graph = verificationFactory.createGraph(user);
    //
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission1));
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission2));
    // assertFalse(graph.isGranted(SUBMISSION_ADMIN, submission3));
    //
    // // simulate that the SUBMISSION_ADMIN permission on p3 is granted to the master-of-the-universe group
    // event = mock(SubmissionPermissionGrantedEvent.class);
    // when(event.getSubmission()).thenReturn(submission3);
    // when(event.getAffectedGroup()).thenReturn("master-of-the-universe");
    // when(event.getPermission()).thenReturn(SUBMISSION_ADMIN);
    //
    // resetMockedPermissions();
    // mockPermissions(
    // new InternalGlobalPermission.Builder().group("SubmissionAdmin").permission(SUBMISSION_CREATE).build(),
    // SubmissionPermissionEntity.builder()
    // .user(user)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission1)
    // .build(),
    // SubmissionPermissionEntity.builder()
    // .user(user)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission2)
    // .build(),
    // SubmissionPermissionEntity.builder()
    // .group("master-of-the-universe")
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission3)
    // .build());
    //
    // factory.onPermissionsChanged(event);
    //
    // // verify on the other Hazelcast instance
    // graph = verificationFactory.createGraph(user);
    //
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission1));
    // assertTrue(graph.isGranted(SUBMISSIONT_ADMIN, submission2));
    // assertTrue(graph.isGranted(SUBMISSION_ADMIN, submission3));
    // }
    //
    // @Test
    // public void testUserPermissions() {
    // final UserEntity user2 = createMockUserEntity(2, "other");
    // mockNoGroupMemberships(user);
    // mockNoGroupMemberships(user2);
    //
    // mockPermissions(GlobalPermissionEntity.builder().user(user).permission(SUBMISSION_CREATE).build(),
    // GlobalPermissionEntity.builder().user(user2).permission(USER).build(),
    // SubmissionPermissionEntity.builder()
    // .user(user2)
    // .permission(SUBMISSION_ADMIN)
    // .submission(submission3)
    // .build());
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph.isGranted(USER, null)); // inherited from SUBMISSION_CREATE
    // assertFalse(graph.isGranted(SYS_ADMIN, null));
    //
    // final IPermissionGraph graph2 = factory.createGraph(user2);
    // assertTrue(graph2.isGranted(USER, null));
    // assertFalse(graph2.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph2.isGranted(SUBMISSION_ADMIN, submission3));
    // assertTrue(graph2.isGranted(SUBMISSION_WRITE, submission3)); // inherited
    // assertTrue(graph2.isGranted(SUBMISSION_READ, submission3)); // inherited
    // assertTrue(graph2.isGranted(SUBMISSION_VIEW, submission3)); // inherited
    //
    // // verify that the default permissions still apply
    // assertFalse(graph2.isGranted(SUBMISSION_ADMIN, submission1));
    // assertTrue(graph2.isGranted(SUBMISSION_WRITE, submission1));
    // assertFalse(graph2.isGranted(SUBMISSION_WRITE, submission2));
    // assertTrue(graph2.isGranted(SUBMISSION_READ, submission2));
    //
    // // caches should be filled - subsequent calls should not result in cache hits
    // factory.createGraph(user);
    // verificationFactory.createGraph(user);
    // factory.createGraph(user2);
    // verificationFactory.createGraph(user2);
    //
    // // verify that the Dao has only been accessed once for each user
    // verify(effectivePermissionDao).findByUserId(eq(1L), any(PageRequest.class));
    // verify(effectivePermissionDao).findByUserId(eq(2L), any(PageRequest.class));
    // }
    //
    // @Test
    // public void testGraphRecalculation() throws InterruptedException {
    // mockNoGroupMemberships(user);
    //
    // mockPermissions(GlobalPermissionEntity.builder().user(user).permission(SUBMISSION_SEND).build());
    //
    // final IPermissionGraph graph = factory.createGraph(user);
    // assertTrue(graph.isGranted(SUBMISSION_CREATE, null));
    // assertTrue(graph.isGranted(LICENSED_USER, null)); // inherited from SUBMISSION_CREATE
    // assertFalse(graph.isGranted(SYS_ADMIN, null));
    //
    // factory.onPermissionsChanged(new GlobalPermissionRevokedEvent(this, SUBMISSION_CREATE, null, user));
    // userPermissions.clear();
    //
    // assertFalse(graph.isGranted(SUBMISSION_CREATE, null));
    // assertFalse(graph.isGranted(USER, null)); // inherited from SUBMISSION_CREATE
    // assertFalse(graph.isGranted(SYS_ADMIN, null));
    //
    // verify(effectivePermissionDao, times(2)).findByUserId(eq(1), any(PageRequest.class));
    // }

    private CachingPermissionGraphFactory buildFactory(final int node) {
        final CachingPermissionGraphFactory permissionGraphFactory = new CachingPermissionGraphFactory(
                cluster.getNode(node).<String, DefaultPermissionGraph> getMap("default"),
                cluster.getNode(node).<String, DefaultPermissionGraph> getMap("groups"),
                cluster.getNode(node).<Long, DefaultPermissionGraph> getMap("users"), effectivePermissionDao,
                userService);
        return permissionGraphFactory;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private UserEntity createMockUserEntity(final long userId, final String username) {
        final UserEntity user = mock(UserEntity.class, withSettings().lenient());
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(username);
        when(user.getUsername()).thenReturn(username);
        when(user.accept(any(IUserVisitor.class)))
                .thenAnswer(invocation -> ((IUserVisitor) invocation.getArguments()[0]).visit(user));

        return user;
    }

    private void mockGroupMemberships(final IUser user, final String... groups) {
        when(userService.findGroupsByUser(eq(user.getUsername()), any(Pageable.class))).thenAnswer(invocation -> {
            final Pageable request = (Pageable) invocation.getArguments()[1];
            if (request.getOffset() >= groups.length) {
                return PageUtils.createEmptyPage(request);
            } else {
                final List<String> values = Lists.newArrayList();
                for (int i = (int) request.getOffset(); i < groups.length
                        && values.size() < request.getPageSize(); ++i) {
                    values.add(groups[i]);
                }
                return PageUtils.createPage(values, request);
            }
        });
    }

    private void mockNoGroupMemberships(final IUser user) {
        mockGroupMemberships(user); // no groups provided
    }

    @SuppressWarnings("unused")
    private void mockPermissions(final GrantedPermission... grantedPermissions) {
        for (final GrantedPermission permission : grantedPermissions) {
            if (StringUtils.isNotBlank(permission.getGroup())) {
                // lowercase the group names - this is what the various DAOs do internally as well
                groupPermissions.put(IdentifierUtils.toLowerCase(permission.getGroup()), permission);
            }
            if (permission.getUser() != null) {
                userPermissions.put(permission.getUser().getId(), permission);
            }
        }
    }

    /**
     * Clears user and group permissions
     */
    @SuppressWarnings("unused")
    private void resetMockedPermissions() {
        groupPermissions.clear();
        userPermissions.clear();
    }
}
