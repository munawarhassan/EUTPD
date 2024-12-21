package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.api.util.FluentIterable.from;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.IGrantedPermissionVisitor;
import com.pmi.tpd.core.model.user.IIterablePermissionGraph;
import com.pmi.tpd.security.permission.IEffectivePermission;
import com.pmi.tpd.security.permission.Permission;

/**
 * Compactly encoded representation of a set of granted permissions for a group or user. The granted permissions are
 * encoded as long values (8 bytes) as follows:
 * <ul>
 * <li>byte 1 (1 byte): type of resource the permission is granted to (global / project / repository)</li>
 * <li>bytes 2-5 (4 bytes): resourceId (project id / repository id); 0 for global permissions</li>
 * <li>bytes 6-8 (3 bytes): permissions, where the bit corresponding to the {@link Permission#getId() Permission#id} of
 * each granted and implied permission is set to 1. Permission ids are zero-based, so id 1 corresponds to the second
 * bit.</li>
 * </ul>
 * A couple of examples help clarify the encoding:
 * <h4>ADMIN</h4> Encoding: 00000001 || 00000000 00000000 00000000 00000000 || 00000000 00000000 00001100
 * <ul>
 * <li>byte 1: ADMIN is a global permission, which is encoded as 00000001 (binary)</li>
 * <li>byte 2-5: global permission, so resourceId = 0 --> binary: 00000000 00000000 00000000 00000000</li>
 * <li>byte 6-8: ADMIN (id: 2) implies USER(3). The corresponding bits are set to 1 --> 00000000 00000111 01111111. Note
 * that the bit that corresponds to SYS_ADMIN(id: 1) is set to 0.</li>
 * </ul>
 *
 * @since 2.0
 */
@ThreadSafe
public class DefaultPermissionGraph implements IIterablePermissionGraph, Externalizable {

    /** */
    private static final Encoding ENCODING = new Encoding();

    /** */
    private static final DefaultPermissionGraph NO_PERMS = new DefaultPermissionGraph(new long[0]);

    /** */
    private long[] values;

    /**
     * Default Constructor.
     */
    // for deserialization
    public DefaultPermissionGraph() {
    }

    private DefaultPermissionGraph(final long[] values) {
        this.values = values;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass() && Arrays.equals(values, ((DefaultPermissionGraph) o).values);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean isGranted(@Nonnull final Permission permission, @Nullable final Object resource) {
        if (values.length == 0) {
            return false;
        }

        final Matcher matcher = ENCODING.createMatcher(permission, resource);
        for (final long value : values) {
            // values is sorted by category (global, project, repo), resourceId
            // there is only 1 value for every [category, resourceId] pair. All permissions (including implied
            // permissions are encoded in the value).
            if (matcher.matches(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return Get the size of graph.
     */
    public int getSize() {
        return values.length;
    }

    @Override
    public Iterator<IEffectivePermission> iterator() {
        return from(Longs.asList(values)).transform(Encoding.TO_EFFECTIVE_PERMISSION).iterator();
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();
        values = new long[size];
        for (int i = 0; i < size; ++i) {
            values[i] = in.readLong();
        }
    }

    // For debugging
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultPermissionGraph{values={");
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(ENCODING.toString(values[i]));
            }
        }
        sb.append("}}");
        return sb.toString();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(values.length);
        for (final long value : values) {
            out.writeLong(value);
        }
    }

    /**
     * Builds {@link DefaultPermissionGraph} instances.
     */
    public static class Builder implements IGrantedPermissionVisitor {

        /** */
        private static final long NO_GLOBAL_PERMS = Encoding.FLAG_PERM_GLOBAL;

        /** */
        private static final Predicate<Long> IS_RESOURCE_PERM = Encoding::isResourcePermission;

        /** */
        private final List<Long> values = Lists.newArrayList();

        /**
         * @param grantedPermission
         * @return Returns fluent {@link Builder}.
         */
        public Builder add(@Nonnull final GrantedPermission grantedPermission) {
            grantedPermission.accept(this);
            return this;
        }

        /**
         * @param permission
         * @param resourceId
         * @return Returns fluent {@link Builder}.
         */
        public Builder add(@Nonnull final Permission permission, @Nullable final Integer resourceId) {
            values.add(ENCODING.encode(permission, resourceId));
            return this;
        }

        /**
         * @param permissions
         * @return Returns fluent {@link Builder}.
         */
        public Builder addAll(@Nonnull final DefaultPermissionGraph permissions) {
            for (final long value : permissions.values) {
                values.add(value);
            }
            return this;
        }

        /**
         * @param permissions
         * @return Returns fluent {@link Builder}.
         */
        public Builder addAll(@Nonnull final Iterable<? extends GrantedPermission> permissions) {
            for (final GrantedPermission permission : permissions) {
                add(permission);
            }
            return this;
        }

        /**
         * @return
         */
        public DefaultPermissionGraph build() {
            if (values.isEmpty()) {
                return NO_PERMS;
            }

            final Pair<Long, List<Long>> permsByScope = partitionPermsByScope(Lists.newArrayList(values));

            final long globalPermissions = permsByScope.getLeft();

            // if global perms imply PROJECT_ADMIN then all resource permissions are implied so
            // as an optimisation we can completely omit them
            final List<Long> resourcePerms = impliesProjectAdmin(globalPermissions) ? Collections.<Long> emptyList()
                    : reduceResourcePerms(globalPermissions, permsByScope.getRight());

            final long[] v = new long[resourcePerms.size() + (isNonEmpty(globalPermissions) ? 1 : 0)];
            int index = 0;
            if (isNonEmpty(globalPermissions)) {
                v[index++] = globalPermissions;
            }

            for (final Long value : resourcePerms) {
                v[index++] = value;
            }
            return new DefaultPermissionGraph(v);
        }

        @Override
        public void visit(@Nonnull final GrantedPermission globalPermission) {
            values.add(ENCODING.encode(globalPermission.getPermission(), null));
        }

        /**
         * @return the first of the pair is the effective global permission, the second is the list of resource perms
         *         (not yet reduced)
         */
        private Pair<Long, List<Long>> partitionPermsByScope(final List<Long> values) {
            // sorting causes global permissions to come first, then project permissions and then repo permissions
            Collections.sort(values);

            List<Long> resourcePerms;

            final int resourcePermIndex = Math.max(0, Iterables.indexOf(values, IS_RESOURCE_PERM));

            long result = NO_GLOBAL_PERMS;
            // collapse the global permissions into a single effective permission
            for (final Long permission : values.subList(0, resourcePermIndex)) {
                result |= permission;
            }
            resourcePerms = Lists.newArrayList(values.subList(resourcePermIndex, values.size()));

            return Pair.of(result, resourcePerms);
        }

        private boolean impliesProjectAdmin(final long globalPermissions) {
            // final long projAdminPermission = (long) Encoding.encodePermissionInt(Permission.PROJECT_ADMIN);
            // return (globalPermissions & projAdminPermission) == projAdminPermission;
            final long projAdminPermission = Encoding.encodePermissionInt(Permission.ADMIN);
            return (globalPermissions & projAdminPermission) == projAdminPermission;
        }

        private boolean isNonEmpty(final long globalPermission) {
            return globalPermission != NO_GLOBAL_PERMS;
        }

        private List<Long> reduceResourcePerms(final long globalPermission, final List<Long> resourcePerms) {
            // iterate resource permissions, clipping any implied by the supplied global permission
            // and combining any that target the same category + resourceId
            final boolean hasGlobalPermission = isNonEmpty(globalPermission);
            final ListIterator<Long> it = resourcePerms.listIterator();
            long prev = 0;
            while (it.hasNext()) {
                long value = it.next();

                // trim a resource permission if implied by a global permission
                if (hasGlobalPermission && (globalPermission & Encoding.unmaskPermissionBits(value)) != 0L) {
                    it.remove();
                    continue;
                }

                // combine values that target the same category + resourceId (equal except for the last 3 bytes)
                if (Encoding.isSameCategoryAndResourceId(value, prev)) {
                    value = value | prev;
                    it.remove();
                    it.previous();
                    it.set(value);
                    it.next();
                }
                prev = value;
            }
            return resourcePerms;
        }
    }

    /**
     * Encapsulates all the encoding / decoding logic for the DefaultPermissionGraph.
     */
    static class Encoding {

        /** */
        private static final long FLAG_PERM_GLOBAL = 1L << 56;

        /** */
        private static final long FLAG_PERM_PROJECT = 2L << 56;

        /** */
        private static final long FLAG_PERM_REPO = 4L << 56;

        /** the encoding of the effective permission for each permission id. */
        private static final int[] ENCODED_PERMISSIONS;

        /** lookup for permission by the encoding of the effective permission. */
        private static final Map<Integer, Permission> DECODED_PERMISSIONS;

        /** maps an encoded effective permission to an instance of EffectPermission. */
        private static final Function<Long, IEffectivePermission> TO_EFFECTIVE_PERMISSION = input -> {
            final long encoded = input;
            final Permission permission = decodePermission(encoded);
            if ((encoded & FLAG_PERM_GLOBAL) != 0) {
                return new SimpleEffectiveGlobalPermission(permission);
            } else {
                return null;
                // final int resourceId = decodeResourceId(encoded);
                // if ((encoded & FLAG_PERM_PROJECT) != 0) {
                // return new SimpleEffectiveProjectPermission(resourceId, permission);
                // } else {
                // return new SimpleEffectiveRepositoryPermission(resourceId, permission);
                // }
            }
        };

        static {
            // Pre-calculate the encodings/decodings of all permissions to make encoding/decoding as fast as possible
            ENCODED_PERMISSIONS = new int[Permission.values().length];
            DECODED_PERMISSIONS = Maps.newHashMap();

            for (final Permission permission : Permission.values()) {
                int value = encodePermissionInt(permission);
                for (final Permission inherited : permission.getInheritedPermissions()) {
                    value |= encodePermissionInt(inherited);
                }

                // Use the ordinal to store the permission. This guarantees the smallest ENCODED_PERMISSIONS array. Note
                // that re-ordering the enum values does not affect this lookup, as ENCODED_PERMISSIONS is built at
                // runtime.
                ENCODED_PERMISSIONS[permission.ordinal()] = value;
                DECODED_PERMISSIONS.put(value, permission);
            }
        }

        public String toString(final long encoded) {
            final String permissionString = decodePermission(encoded).toString();
            if ((encoded & FLAG_PERM_GLOBAL) != 0) {
                return permissionString;
            } else {
                return permissionString + ": " + decodeResourceId(encoded);
            }
        }

        /**
         * Encodes a {@code permission} and {@code resourceId} pair into a {@code long} value. The {@code resourceId} is
         * assumed to refer to the resource type of {@code permission}. (e.g. a project-id for {@code PROJECT_READ}).
         *
         * @param permission
         *            the granted permission
         * @param resourceId
         *            the id of the repository or project the permission was granted on. Use {@code null} for global
         *            permissions
         * @return the granted permission, encoded as a {@code long} value
         */
        public long encode(@Nonnull final Permission permission, @Nullable final Integer resourceId) {
            Assert.state(resourceId == null || resourceId > 0, "resourceId must be null or greater than 0");

            // encoding = 1 byte (global/project/repo) 4 bytes resourceId 3 bytes permission
            return getCategory(permission) | encodeResourceId(resourceId) | encodeEffectivePermissionInt(permission);
        }

        /**
         * Creates a matcher that can be used to check whether an encoded granted permission matches the provided
         * {@code permission} and {@code resource}.
         * <p>
         * A {@code null resource} matches any encoded granted permission that is equal to or inherits the provided
         * {@code permission}.
         *
         * @param permission
         *            the target permission
         * @param resource
         *            the target resource, can be {@code null} for global permissions or 'permission on any resource'
         *            checks.
         * @return a matcher
         */
        @Nonnull
        public Matcher createMatcher(@Nonnull final Permission permission, @Nullable final Object resource) {
            // checkArgument(resource == null || resource instanceof Project || resource instanceof Repository);
            final int permissionInt = encodePermissionInt(permission);
            final int projectId = getProjectId(resource);
            final int repositoryId = getRepositoryId(resource);

            return encoded -> {
                if ((encoded & permissionInt) == 0L) {
                    // encoded does not grant the permission
                    return false;
                }

                if (projectId == 0 && repositoryId == 0) {
                    // a null resource was passed in. This tests for the 'any' permission or a global permission
                    return true;
                }

                if ((encoded & FLAG_PERM_GLOBAL) != 0) {
                    // encoded is a global permission that matches or inherits the requested permission
                    return true;
                }

                final int encodedId = decodeResourceId(encoded);
                // if encodedId == 0, the resource permission has been granted on all resources of that type. A match
                // occurs when the requested permission has been granted to the provided resource, or to all
                // resources of _that type_.
                return (encoded & FLAG_PERM_PROJECT) != 0 && projectId != 0
                        && (encodedId == 0 || projectId == encodedId)
                        || (encoded & FLAG_PERM_REPO) != 0 && repositoryId != 0
                                && (encodedId == 0 || repositoryId == encodedId);
            };
        }

        private int getProjectId(final Object resource) {
            // if (resource instanceof Project) {
            // return ((Project) resource).getId();
            // }
            // if (resource instanceof Repository) {
            // return ((Repository) resource).getProject().getId();
            // }
            return 0;
        }

        private int getRepositoryId(final Object resource) {
            return 0;
            // return resource instanceof Repository ? ((Repository) resource).getId() : 0;
        }

        private static long getCategory(final Permission permission) {
            if (permission.isGlobal()) {
                return FLAG_PERM_GLOBAL;
                // } else if (permission.isResource(Project.class)) {
                // return FLAG_PERM_PROJECT;
                // } else if (permission.isResource(Repository.class)) {
                // return FLAG_PERM_REPO;
            } else {
                throw new IllegalArgumentException("Unsupported permission type " + permission);
            }
        }

        /**
         * Decodes the resource id from the encoded granted permission.
         *
         * @param encoded
         *            the encoded granted permission
         * @return the id of the project or repository the permission was granted to. {@code 0} for global permissions.
         */
        @VisibleForTesting
        static int decodeResourceId(final long encoded) {
            return (int) (encoded >> 24);
        }

        private static boolean isSameCategoryAndResourceId(final long encoded1, final long encoded2) {
            return (encoded1 & 0xffffffffff000000L) == (encoded2 & 0xffffffffff000000L);
        }

        private static long encodeResourceId(final Integer resourceId) {
            return resourceId != null ? resourceId << 24 : 0;
        }

        // only used for testing the encode
        @VisibleForTesting
        static Permission decodePermission(final long encoded) {
            final int permission = unmaskPermissionBitsInt(encoded);
            return DECODED_PERMISSIONS.get(permission);
        }

        private static int unmaskPermissionBitsInt(final long encoded) {
            // the permission is encoded in the last 3 bytes
            return 0xffffff & (int) encoded;
        }

        private static long unmaskPermissionBits(final long encoded) {
            return (long) 0xffffff & (int) encoded;
        }

        private static int encodePermissionInt(final Permission permission) {
            return 1 << permission.getId();
        }

        private static int encodeEffectivePermissionInt(final Permission permission) {
            return ENCODED_PERMISSIONS[permission.ordinal()];
        }

        // private long getProjectMatcher(final int projectId) {
        // return FLAG_PERM_PROJECT | encodeResourceId(projectId);
        // }

        private static boolean isResourcePermission(final long encoding) {
            return (encoding & Encoding.FLAG_PERM_GLOBAL) == 0;
        }
    }

    /**
     * Matcher.
     *
     * @author devacfr<christophefriederich@mac.com>
     */
    interface Matcher {

        boolean matches(long encoded);
    }
}
