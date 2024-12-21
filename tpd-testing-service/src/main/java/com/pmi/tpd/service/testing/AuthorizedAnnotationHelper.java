package com.pmi.tpd.service.testing;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Defaults;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.security.annotation.AnonymousRequired;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * Ensures all {@code PreAuthorize} and {@code PostAuthorize} annotations are <i>somewhat</i> correct, which is to say
 * they reference <i>real</i> fields and <i>real</i> parameters. Otherwise mistakes of this kind are ignored and the
 * permission check is not applied, which could be disastrous for security.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class AuthorizedAnnotationHelper {

    private final Class<?> serviceClass;

    public AuthorizedAnnotationHelper(final Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public List<Throwable> findBrokenAuthorizedMethods() throws Exception {
        // Use a null user so isAuthenticated() fails
        SecurityContextHolder.getContext().setAuthentication(UserAuthenticationToken.builder().build());
        try {
            return validateAnnotations(serviceClass);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    private static Object[] mockArguments(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        // We just need to make sure that are arguments are never null and return non-null objects from some properties
        final Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            // Rely on Mockito for everything except for what it can't handle (ie final classes)
            if (parameterTypes[i].isPrimitive()) {
                args[i] = Defaults.defaultValue(parameterTypes[i]);
            } else if (parameterTypes[i] == String.class) {
                // Make sure this isn't null for MockMethodSecurityExpressionHandler
                args[i] = "";
            } else if (Modifier.isFinal(parameterTypes[i].getModifiers())) {
                args[i] = null;
            } else {
                // Deep mock so nested EL expressions (ie a.b) never return null
                args[i] = mock(parameterTypes[i], RETURNS_DEEP_STUBS);
            }
        }
        return args;
    }

    private static ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("security-test-context.xml", AuthorizedAnnotationHelper.class);

    }

    // Wrap bean in Spring security to activate Authorize annotations
    private static Object secureBean(final ClassPathXmlApplicationContext applicationContext,
        final Class<?> serviceClass,
        final Object bean) {
        final DefaultMethodSecurityExpressionHandler handler = applicationContext
                .getBean(DefaultMethodSecurityExpressionHandler.class);
        handler.setParameterNameDiscoverer(new LocalVariableTableParameterNameDiscoverer() {

            @Override
            public String[] getParameterNames(Method method) {
                // Because we're spying we need to get parameter names from the _actual_ class
                method = ReflectionUtils.findMethod(serviceClass, method.getName(), method.getParameterTypes());
                return super.getParameterNames(method);
            }
        });
        return applicationContext.getAutowireCapableBeanFactory().initializeBean(bean, serviceClass.getName());
    }

    private static List<Throwable> validateAnnotations(final Class<?> serviceClass) throws Exception {
        // Check for a class-level annotation. If it's present, it means every visible method needs to be checked,
        // not just methods that are explicitly annotated
        //
        // Note: PostAuthorize is not checked at the class level because there is nowhere in the codebase where it
        // is applied that way
        final PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(serviceClass, PreAuthorize.class);

        // Create a service by bypassing the constructor - we don't actually care about any of the fields
        // If the @PreAuthorize fails then we expect a NPE at some stage (if the method is non-trivial)
        final Object spy = mock(serviceClass);
        final ClassPathXmlApplicationContext applicationContext = createApplicationContext();
        final Object secureSpy = secureBean(applicationContext, serviceClass, spy);
        final List<Throwable> exceptions = Lists.newArrayList();

        try {
            // Ideally we get the declared methods from the class, but we can't because it gets mocked
            for (final Class<?> serviceInterface : ClassUtils.getAllInterfaces(serviceClass)) {
                for (final Method serviceMethod : serviceInterface.getDeclaredMethods()) {
                    final Method method = ReflectionUtils
                            .findMethod(serviceClass, serviceMethod.getName(), serviceMethod.getParameterTypes());
                    final Authentication token = SecurityContextHolder.getContext().getAuthentication();
                    boolean isAnonymous = false;
                    try {
                        if (AnnotationUtils.findAnnotation(method, AnonymousRequired.class) != null) {
                            final Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
                            authorities.add(
                                new SimpleGrantedAuthority(ApplicationConstants.Security.DEFAULT_GROUP_ANONYMOUS_CODE));
                            SecurityContextHolder.getContext()
                                    .setAuthentication(
                                        new AnonymousAuthenticationToken(ApplicationConstants.Security.ANONYMOUS_USER,
                                                ApplicationConstants.Security.ANONYMOUS_USER, authorities));
                            isAnonymous = true;
                        }
                        // TODO We need to parse the value of both of these types and 'test' all of the conditional
                        // paths
                        // e.g. hasGlobalPermission(#good) and hasGlobalPermission(#bad)
                        // This will pass even though 'bad' may not exist
                        if (AnnotationUtils.findAnnotation(method, PostAuthorize.class) != null) {
                            validatePostAuthorize(spy, secureSpy, serviceMethod, mockArguments(method));
                        } else if (preAuthorize != null
                                || AnnotationUtils.findAnnotation(method, PreAuthorize.class) != null) {
                            validatePreAuthorize(secureSpy, serviceMethod, mockArguments(method));
                        }
                    } catch (final AccessDeniedException e) {
                        // Expected
                    } catch (final Throwable e) {
                        if (!isAnonymous) {
                            // Test all the methods instead of failing on the first one
                            exceptions.add(e);
                        }
                    } finally {
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            }
        } finally {
            applicationContext.close();
        }
        return exceptions;
    }

    private static void validatePreAuthorize(final Object secureSpy, final Method serviceMethod, final Object[] args) {
        // Invoke the method with mock arguments
        ReflectionUtils.invokeMethod(serviceMethod, secureSpy, args);
        fail("PreAuthorize has failed: " + serviceMethod);
    }

    private static void validatePostAuthorize(final Object spy,
        final Object secureSpy,
        final Method serviceMethod,
        final Object[] args) throws Exception {
        // Return something from this method when is then checked by PostAuthorize
        serviceMethod.invoke(
            doAnswer(invocation -> mock(invocation.getMethod().getReturnType(), RETURNS_DEEP_STUBS)).when(spy),
            args);
        ReflectionUtils.invokeMethod(serviceMethod, secureSpy, args);
        fail("PostAuthorize has failed: " + serviceMethod);
    }
}
