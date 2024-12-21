package com.pmi.tpd.security.permission;

/**
 * An {@link EffectivePermission} associated with a {@link com.pmi.tpd.core.model.euceg.SubmissionEntity}.
 *
 * @since 2.0
 */
public interface IEffectiveProductPermission extends IEffectivePermission {

    /**
     * @return the ID of the submission
     */
    Long getSubmissionId();
}
