package com.pmi.tpd.service.testing.mock;

import static org.mockito.Mockito.mock;

import org.aopalliance.intercept.MethodInvocation;
import org.mockito.Mockito;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;

import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.spring.ExtendedMethodSecurityExpressionHandler;

public class MockMethodSecurityExpressionHandler extends ExtendedMethodSecurityExpressionHandler {

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication,
        final MethodInvocation invocation) {
        final MethodSecurityExpressionOperations root = super.createSecurityExpressionRoot(authentication, invocation);
        // Spy on our expression operations and for every method that returns a boolean, return false to fail the
        // permission check
        return mock(root.getClass(), Mockito.withSettings().spiedInstance(root).defaultAnswer(invocation1 -> {
            if (invocation1.getMethod().getReturnType() == Boolean.TYPE) {
                for (final Object arg : invocation1.getArguments()) {
                    // If the argument is null that means the expression references a variable that doesn't exist
                    // Returning true will not throw the appropriate permission exception, which fails
                    if (arg == null) {
                        return true;
                    }
                }
                return false;
            }
            return ReflectionUtils.invokeMethod(invocation1.getMethod(), root, invocation1.getArguments());
        }));
    }

    @Override
    protected IPermissionService getPermissionService() {
        // We don't need the permissionService to be set for this handler to function
        return null;
    }
}
