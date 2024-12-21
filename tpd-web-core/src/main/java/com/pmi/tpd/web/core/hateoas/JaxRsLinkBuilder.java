package com.pmi.tpd.web.core.hateoas;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.core.LastInvocationAware;
import org.springframework.hateoas.server.core.LinkBuilderSupport;
import org.springframework.hateoas.server.core.MethodInvocation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.1
 */
public final class JaxRsLinkBuilder extends LinkBuilderSupport<JaxRsLinkBuilder> {

    /**
     * Creates a new {@link JaxRsLinkBuilder} from the given {@link UriComponentsBuilder}.
     *
     * @param builder
     *            must not be {@literal null}.
     */
    private JaxRsLinkBuilder(final UriComponentsBuilder builder) {
        super(builder);
    }

    /**
     * Creates a {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class.
     *
     * @param service
     *            the class to discover the annotation on, must not be {@literal null}.
     * @return Returns the fluent builder.
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo, final Class<?> service) {
        return linkTo(uriInfo, service, new Object[0]);
    }

    /**
     * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class
     * binding the given parameters to the URI template.
     *
     * @param resourceType
     *            the class to discover the annotation on, must not be {@literal null}.
     * @param parameters
     *            additional parameters to bind to the URI template declared in the annotation, must not be
     *            {@literal null}.
     * @return Returns the fluent builder.
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo,
        final Class<?> resourceType,
        final Object... parameters) {

        Assert.notNull(resourceType, "Controller type must not be null!");
        Assert.notNull(parameters, "Parameters must not be null!");

        final URI uri = uriInfo.getBaseUriBuilder().path(resourceType).build(parameters);

        return new JaxRsLinkBuilder(UriComponentsBuilder.fromUri(uri));
    }

    /**
     * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class
     * binding the given parameters to the URI template.
     *
     * @param resourceType
     *            the class to discover the annotation on, must not be {@literal null}.
     * @param parameters
     *            map of additional parameters to bind to the URI template declared in the annotation, must not be
     *            {@literal null}.
     * @return Returns the fluent builder.
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo,
        final Class<?> resourceType,
        final Map<String, ?> parameters) {

        Assert.notNull(resourceType, "Controller type must not be null!");
        Assert.notNull(parameters, "Parameters must not be null!");

        final URI uri = uriInfo.getBaseUriBuilder().path(resourceType).build(parameters);

        return new JaxRsLinkBuilder(UriComponentsBuilder.fromUri(uri));
    }

    /*
     *
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo, final Method method, final Object... parameters) {
        return linkTo(uriInfo, method.getDeclaringClass(), method, parameters);
    }

    /**
     * @param uriInfo
     * @param resourceType
     * @param method
     * @param parameters
     * @return
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo,
        final Class<?> resourceType,
        final Method method,
        final Object... parameters) {

        Assert.notNull(resourceType, "resourceType type must not be null!");
        Assert.notNull(method, "Method must not be null!");

        final URI uri = uriInfo.getBaseUriBuilder().path(resourceType, method.getName()).build(parameters);

        return new JaxRsLinkBuilder(UriComponentsBuilder.fromUri(uri));
    }

    /**
     * @param uriInfo
     * @param invocationValue
     * @return
     */
    public static JaxRsLinkBuilder linkTo(final UriInfo uriInfo, final Object invocationValue) {
        Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
        final LastInvocationAware invocations = (LastInvocationAware) invocationValue;

        final MethodInvocation invocation = invocations.getLastInvocation();
        final Object[] parameters = invocation.getArguments();
        final Method method = invocation.getMethod();

        final URI uri = uriInfo.getBaseUriBuilder()
                .path(invocation.getTargetType())
                .path(invocation.getTargetType(), method.getName())
                .build(parameters);

        return new JaxRsLinkBuilder(UriComponentsBuilder.fromUri(uri));
    }

    /**
     * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
     * imports of {@link WebMvcLinkBuilder}.
     *
     * @param controller
     *            must not be {@literal null}.
     * @param parameters
     *            parameters to extend template variables in the type level mapping.
     * @return
     */
    public static <T> T methodOn(final Class<T> controller, final Object... parameters) {
        return DummyInvocationUtils.methodOn(controller, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JaxRsLinkBuilder getThis() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JaxRsLinkBuilder createNewInstance(final UriComponentsBuilder builder,
        final List<Affordance> affordances) {
        return new JaxRsLinkBuilder(builder);
    }
}
