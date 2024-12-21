package com.pmi.tpd.web.core.hateoas;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.UriInfo;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.1
 * @param <T>
 */
public class HateoasResourceSupport<T> implements IHateoasResourceSupport<T> {

    /** */
    protected UriInfo uriInfo;

    /** */
    private final Class<? extends T> jaxrsResource;

    /**
     * @param resourceType
     * @param uriInfo
     */
    public HateoasResourceSupport(@Nonnull final Class<? extends T> resourceType, @Nonnull final UriInfo uriInfo) {
        this.uriInfo = Assert.checkNotNull(uriInfo, "uriInfo");
        this.jaxrsResource = Assert.checkNotNull(resourceType, "resourceType");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends T> getJaxrsResource() {
        return jaxrsResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        final Class<? extends T> jaxrsResource,
        final Class<E> entityClass,
        final Class<D> resourceType) {
        return new JaxRsResourceAssemblerSupport<>(uriInfo, jaxrsResource, entityClass, resourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        final Class<E> entityClass,
        final Class<D> resourceType) {
        return new JaxRsResourceAssemblerSupport<>(uriInfo, jaxrsResource, entityClass, resourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> CollectionModel<E> resources(final Page<E> page) {
        final PagedModel<E> resources = new PagedModel<>(page.getContent(), new PagedModel.PageMetadata(page.getSize(),
                page.getNumber(), page.getTotalElements(), page.getTotalPages()));
        resources.add(linkTo().withSelfRel());
        return resources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> CollectionModel<E> resources(final Iterable<E> items) {
        final CollectionModel<E> resources = new CollectionModel<>(items);
        resources.add(linkTo().withSelfRel());
        return resources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends IIdentityEntity<?>> EntityModel<E> resource(@Nonnull final E entity) {
        final EntityModel<E> resource = new EntityModel<>(entity);
        final Link selfRel = linkTo().slash(entity.getId()).withSelfRel();
        resource.add(selfRel);
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxRsLinkBuilder linkTo() {
        return linkTo(jaxrsResource, new Object[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxRsLinkBuilder linkTo(final Object... parameters) {
        return JaxRsLinkBuilder.linkTo(uriInfo, jaxrsResource, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxRsLinkBuilder linkTo(final Map<String, ?> parameters) {
        return JaxRsLinkBuilder.linkTo(uriInfo, jaxrsResource, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxRsLinkBuilder linkTo(final Method method, final Object... parameters) {
        return JaxRsLinkBuilder.linkTo(uriInfo, jaxrsResource, method, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxRsLinkBuilder linkTo(final Object invocationValue) {
        return JaxRsLinkBuilder.linkTo(uriInfo, invocationValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T methodOn(final Object... parameters) {
        return JaxRsLinkBuilder.methodOn(jaxrsResource, parameters);
    }

}
