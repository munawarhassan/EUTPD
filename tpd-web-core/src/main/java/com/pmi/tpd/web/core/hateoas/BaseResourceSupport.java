package com.pmi.tpd.web.core.hateoas;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.pmi.tpd.api.model.IIdentityEntity;

/**
 * @author Christophe Friederich
 * @since 1.1
 * @param <T>
 */
@Scope("request")
public class BaseResourceSupport<T extends BaseResourceSupport<?>> {

    /** */
    @Context
    private UriInfo uriInfo;

    /** */
    private HateoasResourceSupport<?> resourceSupport;

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected IHateoasResourceSupport<T> getHateoasSupport() {
        if (resourceSupport == null) {
            resourceSupport = new HateoasResourceSupport<>(this.getClass(), uriInfo);
        }
        return (IHateoasResourceSupport<T>) resourceSupport;
    }

    /**
     * @param jaxrsResource
     * @param entityClass
     * @param resourceType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        final Class<?> jaxrsResource,
        final Class<E> entityClass,
        final Class<D> resourceType) {
        @SuppressWarnings("rawtypes")
        final HateoasResourceSupport<?> resourceSupport = new HateoasResourceSupport(jaxrsResource, uriInfo);
        return resourceSupport.assembler(entityClass, resourceType);
    }

    /**
     * @param entityClass
     * @param resourceType
     * @return
     */
    public <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        final Class<E> entityClass,
        final Class<D> resourceType) {
        return getHateoasSupport().assembler(entityClass, resourceType);
    }

    /**
     * @param page
     * @return
     */
    public <E> CollectionModel<E> resources(final Page<E> page) {
        return getHateoasSupport().resources(page);
    }

    /**
     * @param items
     * @return
     */
    public <E> CollectionModel<E> resources(final Iterable<E> items) {
        return getHateoasSupport().resources(items);
    }

    /**
     * @param entity
     * @return
     */
    public <E extends IIdentityEntity<?>> EntityModel<E> resource(@Nonnull final E entity) {
        return getHateoasSupport().resource(entity);
    }

    /**
     * @param jaxrsResource
     * @return
     */
    public JaxRsLinkBuilder linkTo(final Class<?> jaxrsResource) {
        final HateoasResourceSupport<?> resourceSupport = new HateoasResourceSupport<>(jaxrsResource, uriInfo);
        return resourceSupport.linkTo();
    }

    /**
     * @return
     */
    public JaxRsLinkBuilder linkTo() {
        return getHateoasSupport().linkTo();
    }

    /**
     * @param parameters
     * @return
     */
    public JaxRsLinkBuilder linkTo(final Object... parameters) {
        return getHateoasSupport().linkTo(parameters);
    }

    /**
     * @param parameters
     * @return
     */
    public JaxRsLinkBuilder linkTo(final Map<String, ?> parameters) {
        return getHateoasSupport().linkTo(parameters);
    }

    /**
     * @param method
     * @param parameters
     * @return
     */
    public JaxRsLinkBuilder linkTo(final Method method, final Object... parameters) {
        return getHateoasSupport().linkTo(method, parameters);
    }

    /**
     * @param invocationValue
     * @return
     */
    public JaxRsLinkBuilder linkTo(final Object invocationValue) {
        return getHateoasSupport().linkTo(invocationValue);
    }

    /**
     * @param parameters
     * @return
     */
    public T methodOn(final Object... parameters) {
        return getHateoasSupport().methodOn(parameters);
    }
}
