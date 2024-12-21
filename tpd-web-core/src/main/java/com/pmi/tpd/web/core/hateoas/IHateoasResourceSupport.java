package com.pmi.tpd.web.core.hateoas;

import java.lang.reflect.Method;
import java.util.Map;

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
public interface IHateoasResourceSupport<T> {

    /**
     * @return
     */
    Class<? extends T> getJaxrsResource();

    /**
     * @param jaxrsResource
     * @param entityClass
     * @param resourceType
     * @return
     */
    <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        Class<? extends T> jaxrsResource,
        Class<E> entityClass,
        Class<D> resourceType);

    /**
     * @param entityClass
     * @param resourceType
     * @return
     */
    <E extends IIdentityEntity<?>, D extends RepresentationModel<?>> RepresentationModelAssembler<E, D> assembler(
        Class<E> entityClass,
        Class<D> resourceType);

    /**
     * @param page
     * @return
     */
    <E> CollectionModel<E> resources(Page<E> page);

    /**
     * @param items
     * @return
     */
    <E> CollectionModel<E> resources(Iterable<E> items);

    /**
     * @param entity
     * @return
     */
    <E extends IIdentityEntity<?>> EntityModel<E> resource(E entity);

    /**
     * @return
     */
    JaxRsLinkBuilder linkTo();

    /**
     * @param parameters
     * @return
     */
    JaxRsLinkBuilder linkTo(Object... parameters);

    /**
     * @param parameters
     * @return
     */
    JaxRsLinkBuilder linkTo(Map<String, ?> parameters);

    /**
     * @param method
     * @param parameters
     * @return
     */
    JaxRsLinkBuilder linkTo(Method method, Object... parameters);

    /**
     * @param invocationValue
     * @return
     */
    JaxRsLinkBuilder linkTo(Object invocationValue);

    T methodOn(Object... parameters);

}
