package com.pmi.tpd.core.user.permission;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.hazelcast.map.IMap;
import com.pmi.tpd.api.event.ICancelableEvent;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.paging.IPageProvider;
import com.pmi.tpd.api.paging.PagedIterable;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.user.GroupCleanupEvent;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.IIterablePermissionGraph;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.AbstractUserVisitor;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.security.permission.IEffectivePermission;
import com.pmi.tpd.security.permission.Permission;

/**
 * {@link IPermissionGraphFactory} that efficiently caches the granted permissions using {@link DefaultPermissionGraph}
 * instances.
 * <p>
 * A user can be granted a permission through any of the following:
 * <ul>
 * <li>Default permissions defined on a project</li>
 * <li>Permissions granted to a group that the user is a member of</li>
 * <li>Permissions granted specifically to the user</li>
 * </ul>
 * <p>
 * This class tracks each of these categories individually and uses them to create {@link PermissionGraph} instances for
 * specific users.
 * <p>
 * {@link CachingPermissionGraphFactory} listens for relevant permission and project events to keep the cached
 * permissions in sync with the database.
 *
 * @since 2.0
 */
public class CachingPermissionGraphFactory implements IPermissionGraphFactory {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingPermissionGraphFactory.class);

    /** */
    private static final String DEFAULT_KEY = "default";

    /** */
    private final IMap<String, DefaultPermissionGraph> defaultPermissions;

    /** */
    private final Function<String, DefaultPermissionGraph> defaultPermissionsLoader;

    /** */
    private final IEffectivePermissionRepository effectivePermissionRepository;

    /** */
    private final IMap<String, DefaultPermissionGraph> groupPermissions;

    /** */
    private final Function<String, DefaultPermissionGraph> groupPermissionsLoader;

    /** */
    private final IMap<Long, DefaultPermissionGraph> userPermissions;

    /** */
    private final Function<Long, DefaultPermissionGraph> userPermissionsLoader;

    /** */
    // @Value("${page.max.granted.permissions}")
    // TODO fix it
    private int maxGrantedPermissionsPageSize = 1000;

    /** */
    private final IUserService userService;

    /** */
    private volatile long cacheVersion;

    /**
     * Create new instance of {@link CachingPermissionGraphFactory}.
     *
     * @param defaultPermissions
     *                                      default permissions
     * @param groupPermissions
     *                                      group permissions
     * @param userPermissions
     *                                      user permission
     * @param effectivePermissionRepository
     *                                      a effective permission repository
     * @param userService
     *                                      the user service.
     */
    @Inject
    public CachingPermissionGraphFactory(final IMap<String, DefaultPermissionGraph> defaultPermissions,
            final IMap<String, DefaultPermissionGraph> groupPermissions,
            final IMap<Long, DefaultPermissionGraph> userPermissions,
            final IEffectivePermissionRepository effectivePermissionRepository, final IUserService userService) {

        this.defaultPermissions = defaultPermissions;
        this.effectivePermissionRepository = effectivePermissionRepository;
        this.groupPermissions = groupPermissions;
        this.userPermissions = userPermissions;
        this.userService = userService;

        defaultPermissionsLoader = createDefaultPermissionsLoader();
        groupPermissionsLoader = createGroupPermissionsLoader();
        userPermissionsLoader = createUserPermissionsLoader();

        maxGrantedPermissionsPageSize = 1000; // For testing; will be overwritten by the setter at runtime
    }

    @Override
    @Nonnull
    public IIterablePermissionGraph createGraph(@Nonnull final IUser user) {
        return new RecalculatingPermissionGraph(user);
    }

    /**
     * Execute when a group is deleted from all user directories visible to the server.
     *
     * @param event
     *              a group clean event.
     */
    @EventListener
    public void onGroupCleanup(final GroupCleanupEvent event) {
        invalidate(groupPermissions, event.getGroup());
    }

    /**
     * @param event
     *              a permission event.
     */
    @EventListener
    public void onPermissionsChanged(final PermissionEvent event) {
        if (event instanceof ICancelableEvent) {
            return;
        }

        final IUser user = event.getAffectedUser();
        final String group = event.getAffectedGroup();
        if (user != null) {
            invalidate(userPermissions, user.getId());
        } else if (StringUtils.isNotBlank(group)) {
            invalidate(groupPermissions, group);
        }
        // else if (event instanceof ProjectPermissionEvent) {
        // final Project project = ((ProjectPermissionEvent) event).getProject();
        // // default permission has changed
        // if (event instanceof ProjectPermissionGrantedEvent) {
        // updateDefaultPermission(project, event.getPermission());
        // } else if (event instanceof ProjectPermissionRevokedEvent) {
        // updateDefaultPermission(project, null);
        // } else if (event instanceof ProjectPermissionModifiedEvent) {
        // updateDefaultPermission(project, event.getPermission());
        // }
        // }
    }

    // @EventListener
    // public void onProjectDeleted(final ProjectDeletedEvent event) {
    // // the caches need to be updated manually because on project deletion, the system does a bulk delete of the
    // // granted permissions on the project, without raising permission events.
    // updateDefaultPermission(event.getProject(), null);
    //
    // // brute force: clear all user/group permission caches
    // invalidateAll(userPermissions);
    // invalidateAll(groupPermissions);
    // }
    //
    // @EventListener
    // public void onRepositoryDeleted(final RepositoryDeletedEvent event) {
    // // brute force: clear all user/group permission caches
    // invalidateAll(userPermissions);
    // invalidateAll(groupPermissions);
    //
    // // the defaultPermissions cache does not have to be invalidated; it is not affected by the repository delete
    // }

    /**
     * Execute when a user is deleted from all user directories visible to the server.
     *
     * @param event
     *              a user cleanup event.
     */
    @EventListener
    public void onUserCleanup(final UserCleanupEvent event) {
        final IUser deletedUser = event.getDeletedUser();
        if (deletedUser != null) {
            invalidate(userPermissions, deletedUser.getId());
        }
    }

    /**
     * Sets the size max of granted permission page.
     *
     * @param maxGrantedPermissionsPageSize
     *                                      the size to use.
     */
    public void setMaxGrantedPermissionsPageSize(final int maxGrantedPermissionsPageSize) {
        this.maxGrantedPermissionsPageSize = maxGrantedPermissionsPageSize;
    }

    /**
     *
     */
    public void warmCaches() {
        // warm the default permissions cache to avoid contention on startup
        getDefaultPermissions();
    }

    /**
     * @return a {@link Function} that loads the {@link PermissionGraph} representing the default project permissions
     *         from the database
     */
    private Function<String, DefaultPermissionGraph> createDefaultPermissionsLoader() {
        return key -> new DefaultPermissionGraph.Builder()
                // .addAll(new PagedIterable((IPageProvider<InternalProjectPermission>) request -> projectPermissionDao
                // .findDefaultPermissions(request), maxGrantedPermissionsPageSize))
                .build();
    }

    /**
     * @return a {@link Function} that loads the {@link PermissionGraph} for a given group from the database
     */
    private Function<String, DefaultPermissionGraph> createGroupPermissionsLoader() {
        return group -> new DefaultPermissionGraph.Builder().addAll(new PagedIterable<>(
                (IPageProvider<GrantedPermission>) request -> effectivePermissionRepository.findByGroup(group, request),
                maxGrantedPermissionsPageSize)).build();
    }

    /**
     * @return a {@link Function} that loads the {@link PermissionGraph} for a given user ID from the database
     */
    private Function<Long, DefaultPermissionGraph> createUserPermissionsLoader() {
        return userId -> new DefaultPermissionGraph.Builder()
                .addAll(new PagedIterable<>((IPageProvider<GrantedPermission>) request -> effectivePermissionRepository
                        .findByUserId(userId, request), maxGrantedPermissionsPageSize))
                .build();
    }

    private DefaultPermissionGraph getDefaultPermissions() {
        return getPermissionGraph(defaultPermissions, DEFAULT_KEY, defaultPermissionsLoader);
    }

    private DefaultPermissionGraph getGroupsPermissions(final Iterable<String> groups) {
        final DefaultPermissionGraph.Builder builder = new DefaultPermissionGraph.Builder();
        for (final String group : groups) {
            builder.addAll(getPermissionGraph(groupPermissions, group, groupPermissionsLoader));
        }

        return builder.build();
    }

    private <K> DefaultPermissionGraph getPermissionGraph(final IMap<K, DefaultPermissionGraph> cache,
        final K key,
        final Function<K, DefaultPermissionGraph> loader) {
        DefaultPermissionGraph graph = cache.get(key);
        while (graph == null) {
            // cache miss
            graph = Assert.notNull(loader.apply(key), "cache loader returned a null value!");

            final DefaultPermissionGraph other = cache.putIfAbsent(key, graph);
            if (other != null && !other.equals(graph)) {
                // the cache was updated concurrently but the values don't line up. Clear the cache value and reload
                cache.delete(key);
                graph = null;
            }
        }

        return graph;
    }

    private DefaultPermissionGraph getUserPermissions(final Long userId) {
        return getPermissionGraph(userPermissions, userId, userPermissionsLoader);
    }

    private Iterable<String> loadGroupsByUser(final IUser user) {
        return new PagedIterable<>(
                (IPageProvider<String>) request -> userService.findGroupsByUser(user.getUsername(), request),
                maxGrantedPermissionsPageSize);
    }

    /**
     * Gets the indicating whether the cache is invalid since a version.
     *
     * @param version
     *                a version to check.
     * @return Returns {@code true} whether the cache is invalid, {@code false} otherwise.
     */
    public boolean cacheInvalidatedSince(final long version) {
        // using != rather than > to allow for long rollover
        return cacheVersion != version;
    }

    private <K> void invalidate(final IMap<K, DefaultPermissionGraph> cache, final K key) {
        cache.delete(key);
        markCacheUpdated();
    }

    // private void invalidateAll(final IMap<?, DefaultPermissionGraph> cache) {
    // cache.clear();
    // markCacheUpdated();
    // }

    private void markCacheUpdated() {
        // not a race condition: all we care about is whether it's different to the one when a PermissionGraph
        // was created so whether we skip a bit is irrelevant
        cacheVersion++;
    }
    //
    // private void updateDefaultPermission(final Project project, final Permission newPermission) {
    // defaultPermissions.executeOnKey(DEFAULT_KEY,
    // new DefaultPermissionsUpdatingEntryProcessor(project, newPermission));
    // markCacheUpdated();
    // }

    /**
     * @author devacfr<christophefriederich@mac.com>
     * @since 2.0
     */
    private interface PermissionGraphCalculator {

        IIterablePermissionGraph calculate();
    }

    /**
     * A permission graph for a user that is recalculated on demand when the factory caches are invalidated for any
     * reason.
     * <p>
     * Note that the graph version is tracked per node and is not synchronized across the cluster. As a result, the
     * permission graph will only be recalculated if the permission cache is invalidated on the same node. This is
     * sufficient because:
     * <ul>
     * <li>PermissionGraph instances are created per request and don't span multiple requests</li>
     * <li>Recalculation is required to make permission changes directly applicable to the request in which they are
     * made</li>
     * </ul>
     */
    private final class RecalculatingPermissionGraph implements IIterablePermissionGraph {

        /** */
        private final PermissionGraphCalculator calculator;

        /** */
        private final IUser user;

        /** */
        private volatile Pair<IIterablePermissionGraph, Long> graphAndVersion;

        private RecalculatingPermissionGraph(final IUser user) {
            this.user = user;
            calculator = newCalculatorForUser(user);
            graphAndVersion = recalculate();
        }

        @Override
        public boolean isGranted(final Permission permission, @Nullable final Object resource) {
            return getGraph().isGranted(permission, resource);
        }

        @Override
        public Iterator<IEffectivePermission> iterator() {
            return getGraph().iterator();
        }

        private IIterablePermissionGraph getGraph() {
            // not using atomic references or a synchronized block because the worst that will
            // happen is we have two concurrent calculations and only one will win
            // but even this is highly unlikely given the thread-local alignment of where the graph is stored
            if (cacheInvalidatedSince(graphAndVersion.getRight())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Permission graph for user {} has expired and will be recalculated",
                        user.getUsername());
                }
                graphAndVersion = recalculate();
            }

            return graphAndVersion.getLeft();
        }

        private PermissionGraphCalculator newCalculatorForUser(final IUser user) {
            return user.accept(new AbstractUserVisitor<PermissionGraphCalculator>() {

                @Override
                public PermissionGraphCalculator visit(final UserEntity user) {
                    return new NormalUserPermissionGraphCalculator(user);
                }

                @SuppressWarnings("unused")
                public PermissionGraphCalculator visit(final User user) {
                    return new NormalUserPermissionGraphCalculator(user);
                }

                @Override
                public PermissionGraphCalculator visit(final IUser user) {
                    return new NormalUserPermissionGraphCalculator(user);
                }

            });
        }

        private Pair<IIterablePermissionGraph, Long> recalculate() {
            // copy the cacheVersion to a local variable to handle cache updates that happen concurrently with
            // the calculation of the cache.
            final long currentVersion = cacheVersion;
            return Pair.of(calculator.calculate(), currentVersion);
        }
    }

    // /**
    // * {@link com.hazelcast.map.EntryProcessor} that atomically updates the project permission in a cached
    // * {@link DefaultPermissionGraph} entry.
    // */
    // private static class DefaultPermissionsUpdatingEntryProcessor
    // extends AbstractEntryProcessor<String, DefaultPermissionGraph> implements Externalizable {
    //
    // private int projectId;
    //
    // private Permission newPermission;
    //
    // @SuppressWarnings("UnusedDeclaration")
    // public DefaultPermissionsUpdatingEntryProcessor() {
    // }
    //
    // private DefaultPermissionsUpdatingEntryProcessor(final Project project, final Permission newPermission) {
    // this.projectId = project.getId();
    // this.newPermission = newPermission;
    // }
    //
    // @Override
    // public Object process(final Map.Entry<String, DefaultPermissionGraph> entry) {
    // // create a new default permission set, minus the permission for the affected project
    // final DefaultPermissionGraph.Builder builder = new DefaultPermissionGraph.Builder();
    //
    //
    // if (newPermission != null) {
    // builder.add(newPermission, projectId);
    // }
    //
    // // add the new default permission for the project
    // entry.setValue(builder.build());
    // return null;
    // }
    //
    // @Override
    // public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    // projectId = in.readInt();
    // newPermission = (Permission) in.readObject();
    // }
    //
    // @Override
    // public void writeExternal(final ObjectOutput out) throws IOException {
    // out.writeInt(projectId);
    // out.writeObject(newPermission);
    // }
    // }

    /**
     * @author devacfr<christophefriederich@mac.com>
     */
    private final class NormalUserPermissionGraphCalculator implements PermissionGraphCalculator {

        /** */
        private final IUser user;

        private NormalUserPermissionGraphCalculator(final IUser user) {
            this.user = user;
        }

        @Override
        public IIterablePermissionGraph calculate() {
            return new DefaultPermissionGraph.Builder()
                    // all default permissions
                    .addAll(getDefaultPermissions())
                    // all permissions for the user's group
                    .addAll(getGroupsPermissions(loadGroupsByUser(user)))
                    // and all permissions granted directly to the user
                    .addAll(getUserPermissions(user.getId()))
                    .build();
        }
    }

}
