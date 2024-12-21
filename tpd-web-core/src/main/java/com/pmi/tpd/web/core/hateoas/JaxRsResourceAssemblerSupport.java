package com.pmi.tpd.web.core.hateoas;

import javax.ws.rs.core.UriInfo;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.1
 * @param <T>
 * @param <D>
 */
public class JaxRsResourceAssemblerSupport<T extends IIdentityEntity<?>, D extends RepresentationModel<?>>
        extends RepresentationModelAssemblerSupport<T, D> {

    /** */
    @SuppressWarnings("rawtypes")
    private final IHateoasResourceSupport hateoasSupport;

    /**
     * @param uriInfo
     * @param jaxrsResource
     * @param entityClass
     * @param resourceType
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JaxRsResourceAssemblerSupport(final UriInfo uriInfo, final Class<?> jaxrsResource,
            final Class<T> entityClass, final Class<D> resourceType) {

        super(jaxrsResource, resourceType);
        this.hateoasSupport = new HateoasResourceSupport(jaxrsResource, uriInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected D createModelWithId(final Object id, final T entity, final Object... parameters) {
        Assert.notNull(entity);
        Assert.notNull(id);

        final D instance = instantiateModel(entity);

        instance.add(hateoasSupport.linkTo().slash(id).withSelfRel());
        return instance;
    }

    @Override
    public D toModel(final T entity) {
        final D resource = createModelWithId(entity.getId(), entity);
        return resource;
    }
}
