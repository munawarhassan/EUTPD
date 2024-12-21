package com.pmi.tpd.security.permission;

/**
 * An {@link EffectivePermission} associated with a {@link com.pmi.tpd.core.model.euceg.ProductEntity}.
 *
 * @since 2.0
 */
public interface IEffectiveSubmissionPermission extends IEffectivePermission {

    /**
     * @return the ID of the project
     */
    Long getProductId();
}
