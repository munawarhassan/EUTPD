package com.pmi.tpd.security.spring;

import static com.pmi.tpd.security.support.CommonValidations.validateGlobalPermission;

import java.security.Principal;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import com.google.common.base.Predicate;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserProfileRequest;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.Permission;

/**
 * This class extends the default
 * {@link org.springframework.security.access.expression.method.MethodSecurityExpressionRoot} to provide specific
 * methods in the SpringEL for Spring Security expressions.
 *
 * @since 2.0
 * @author Christophe Friederich
 */
public class ExtendedMethodSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    /** */
    private Object filterObject;

    /** */
    private Object target;

    /** */
    private final ThreadLocal<Boolean> permissionLoopGuard;

    /** */
    private IPermissionService permissionService;

    /** */
    private Object returnObject;

    public ExtendedMethodSecurityExpressionRoot(final Authentication a,
            final ThreadLocal<Boolean> evaluationPermissions) {
        super(a);
        this.permissionLoopGuard = evaluationPermissions;
    }

    public boolean hasAnyPermission(final Permission permission) {
        return applyPredicate(permission, permission1 -> permissionService.hasAnyUserPermission(permission1));
    }

    public boolean hasGlobalPermission(final Permission permission) {
        validateGlobalPermission(permission);

        return applyPredicate(permission, permission1 -> permissionService.hasGlobalPermission(permission1));
    }

    public boolean hasGlobalPermission(final UserRequest user, final Permission permission) {
        validateGlobalPermission(permission);

        return applyPredicate(permission,
            permission1 -> permissionService.hasGlobalPermission(user.getUsername(), permission1));
    }

    public boolean hasGlobalPermission(final String username, final Permission permission) {
        validateGlobalPermission(permission);

        return applyPredicate(permission, permission1 -> permissionService.hasGlobalPermission(username, permission1));
    }

    public boolean hasGlobalPermission(final Principal user, final Permission permission) {
        validateGlobalPermission(permission);

        return applyPredicate(permission, perm -> permissionService.hasGlobalPermission(user.getName(), perm));
    }

    public boolean isCurrentUser(final Principal user) {
        return user != null && isCurrentUser(user.getName());
    }

    public boolean isCurrentUser(final String username) {
        final UserAuthenticationToken user = resolveToken();

        return user != null && equals(user.getName(), username);
    }

    public boolean isCurrentUser(final IUser user) {
        return user != null && isCurrentUser(user.getName());
    }

    public boolean isCurrentUser(final UserRequest user) {
        return user != null && isCurrentUser(user.getUsername());
    }

    public boolean isCurrentUser(final UserProfileRequest user) {
        return user != null && isCurrentUser(user.getUsername());
    }

    private boolean applyPredicate(final Permission permission, final Predicate<Permission> predicate) {
        enterPermissionCheck();
        try {
            return predicate.apply(permission);
        } finally {
            exitPermissionCheck();
        }
    }

    private void enterPermissionCheck() {
        if (permissionLoopGuard.get() != null) {
            throw new IllegalStateException(
                    "Nested method authorisation check detected. This would have led to an infinite loop!");
        }
        permissionLoopGuard.set(Boolean.TRUE);
    }

    private void exitPermissionCheck() {
        permissionLoopGuard.remove();
    }

    private UserAuthenticationToken resolveToken() {
        if (authentication instanceof UserAuthenticationToken) {
            return (UserAuthenticationToken) authentication;
        }
        return null;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public void setReturnObject(final Object returnObject) {
        this.returnObject = returnObject;
    }

    public void setPermissionService(final IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void setFilterObject(final Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    /**
     * Sets the "this" property for use in expressions. Typically this will be the "this" property of the
     * {@code JoinPoint} representing the method invocation which is being protected.
     *
     * @param target
     *            the target object on which the method in is being invoked.
     */
    void setThis(final Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return target;
    }

    /**
     * <p>
     * Compares two CharSequences, returning {@code true} if they represent equal sequences of characters.
     * </p>
     * <p>
     * {@code null}s are handled without exceptions. Two {@code null} references are considered to be equal. The
     * comparison is <strong>case sensitive</strong>.
     * </p>
     *
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @param cs1
     *            the first CharSequence, may be {@code null}
     * @param cs2
     *            the second CharSequence, may be {@code null}
     * @return {@code true} if the CharSequences are equal (case-sensitive), or both {@code null}
     * @since 3.0 Changed signature from equals(String, String) to equals(CharSequence, CharSequence)
     * @see Object#equals(Object)
     * @see #equalsIgnoreCase(CharSequence, CharSequence)
     */
    private static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
