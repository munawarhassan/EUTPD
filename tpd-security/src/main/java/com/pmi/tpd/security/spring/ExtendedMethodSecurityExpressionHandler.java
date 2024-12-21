package com.pmi.tpd.security.spring;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;

import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.security.permission.IPermissionService;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class ExtendedMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler implements Ordered {

    /** */
    // Guard used to verify that no method calls requiring permission checks are called from
    // ExtendedMethodSecurityExpressionRoot. If they were, we'd end up with an infinite loop.
    // Instead, you'll get an IllegalStateException.
    // See ExtendedMethodSecurityExpressionRoot for implementation details.
    private final ThreadLocal<Boolean> permissionLoopGuard = new ThreadLocal<>();

    /** */
    private final AuthenticationTrustResolver trustResolver = new ExtendedAuthenticationTrustResolver();

    /** */
    private ApplicationContext applicationContext;

    /** */
    private volatile IPermissionService permissionService;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        super.setApplicationContext(applicationContext);
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication,
        final MethodInvocation invocation) {
        final ExtendedMethodSecurityExpressionRoot root = new ExtendedMethodSecurityExpressionRoot(authentication,
                permissionLoopGuard);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setPermissionService(getPermissionService());
        root.setThis(invocation.getThis());
        root.setTrustResolver(trustResolver);

        return root;
    }

    @VisibleForTesting
    protected IPermissionService getPermissionService() {
        if (permissionService == null) {
            synchronized (trustResolver) {
                if (permissionService == null) {
                    permissionService = applicationContext.getBean(IPermissionService.class);
                }
            }
        }
        return permissionService;
    }
}
