package com.pmi.tpd.euceg.core.support;

/**
 * Defines all possible change types for a diff patch.
 *
 * @see com.pmi.tpd.api.euceg.support.ProductXmlDiff.DiffResult
 * @see com.pmi.tpd.api.euceg.support.ProductXmlDiff
 * @author devacfr
 * @since 2.4
 */
public enum DiffChange {
    /** indicates is unchanged patch. */
    Unchanged,
    /** indicates is added patch. */
    Added,
    /** indicates is Added patch. */
    Modified,
    /** indicates is deleted patch (Not used). */
    Deleted
}
