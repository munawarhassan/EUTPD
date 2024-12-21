package com.pmi.tpd.core.user.permission;

import static com.google.common.base.Preconditions.checkNotNull;

import com.pmi.tpd.security.permission.Permission;

/**
 * Base criteria to search for {@link Permission permissions}.
 */
public abstract class PermissionCriteria {

    private final Permission permission;

    protected PermissionCriteria(final Permission permission) {
        checkNotNull(permission, "a permission must be specified");
        this.permission = permission;

    }

    /**
     * Gets the {@link Permission permission} associated with this criteria.
     *
     * @return the permission
     */
    public Permission getPermission() {
        return permission;
    }

    protected abstract static class AbstractBuilder<T> {

        protected Permission permission;

        public T permission(final Permission permission) {
            this.permission = permission;
            return self();
        }

        public T resource(final Object resource) {
            // if (resource instanceof Repository) {
            // Repository repository = (Repository) resource;
            // this.repositoryId = repository.getId();
            // this.projectId = repository.getProject().getId();
            // } else if (resource instanceof Project) {
            // Project project = (Project) resource;
            // this.projectId = project.getId();
            // } else
            if (resource != null) {
                throw new IllegalArgumentException("Unsupported resource: " + resource);
            }

            return self();
        }

        protected abstract T self();

    }

}
