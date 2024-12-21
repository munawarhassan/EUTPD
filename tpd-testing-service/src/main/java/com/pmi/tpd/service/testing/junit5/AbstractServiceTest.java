package com.pmi.tpd.service.testing.junit5;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import com.pmi.tpd.security.annotation.Secured;
import com.pmi.tpd.security.annotation.Unsecured;
import com.pmi.tpd.service.testing.AuthorizedAnnotationHelper;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * A base class for building tests for {@code Service}-annotated classes.
 */
public abstract class AbstractServiceTest extends MockitoTestCase {

    /** */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** */
    private final Class<?> serviceClass;

    /** */
    private final Class<?>[] serviceInterfaces;

    protected AbstractServiceTest(final Class<?> serviceClass, final Class<?>... serviceInterfaces) {
        this.serviceClass = serviceClass;
        this.serviceInterfaces = serviceInterfaces;

    }

    protected Logger getLogger() {
        return log;
    }

    @Test
    public void testServiceMethodsCheckPermissions() throws Exception {
        // Check to see if the service is permissioned at the class level. If so, all public methods implicitly have
        // permission checks and there is no need to test them individually.
        //
        // Note: We intentionally do not check for other permission annotations on the class. (There is some
        // debate about whether we should check for this one, either)
        if (AnnotationUtils.findAnnotation(serviceClass, PreAuthorize.class) != null) {
            return;
        }

        final List<Method> unpermissioned = new ArrayList<>();
        for (final Class<?> serviceInterface : serviceInterfaces) {
            for (final Method serviceMethod : serviceInterface.getMethods()) {
                final Method method = serviceClass.getMethod(serviceMethod.getName(),
                    serviceMethod.getParameterTypes());

                final Secured secured = AnnotationUtils.findAnnotation(method, Secured.class);
                if (secured != null) {
                    if (StringUtils.isBlank(secured.value())) {
                        log.warn("{}: @Secured annotation is not documented", method);
                    }
                    // This method is secured using something other than Spring annotations
                    continue;
                }

                final Unsecured unsecured = AnnotationUtils.findAnnotation(method, Unsecured.class);
                if (unsecured != null) {
                    if (StringUtils.isBlank(unsecured.value())) {
                        log.warn("{}: @Unsecured annotation is not documented", method);
                    }
                    // This method is intentionally unsecured
                    continue;
                }

                if (AnnotationUtils.findAnnotation(method, PreAuthorize.class) == null
                        && AnnotationUtils.findAnnotation(method, PostAuthorize.class) == null
                        && AnnotationUtils.findAnnotation(method, PreFilter.class) == null
                        && AnnotationUtils.findAnnotation(method, PostFilter.class) == null) {
                    unpermissioned.add(method);
                }
            }
        }

        if (!unpermissioned.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            builder.append(serviceClass.getSimpleName())
                    .append(" contains ")
                    .append(unpermissioned.size())
                    .append(" method");
            if (unpermissioned.size() > 1) {
                builder.append("s");
            }
            builder.append(" to which permission checks have not been applied:");
            for (final Method method : unpermissioned) {
                builder.append("\n")
                        .append(method.getReturnType().getSimpleName())
                        .append(" ")
                        .append(method.getName())
                        .append("(");
                boolean first = true;
                for (final Class<?> type : method.getParameterTypes()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append(type.getSimpleName());
                }
                builder.append(")");
            }

            fail(builder.toString());
        }
    }

    @Test
    public void testPrePostAuthorize() throws Throwable {
        final AuthorizedAnnotationHelper helper = new AuthorizedAnnotationHelper(serviceClass);
        final List<Throwable> failures = helper.findBrokenAuthorizedMethods();
        if (!failures.isEmpty()) {
            final MultipleFailuresError multipleFailuresError = new MultipleFailuresError(null, failures);
            failures.forEach(multipleFailuresError::addSuppressed);
            throw multipleFailuresError;
        }
    }

}
