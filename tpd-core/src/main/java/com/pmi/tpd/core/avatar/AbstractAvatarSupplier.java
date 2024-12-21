package com.pmi.tpd.core.avatar;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * @since 2.4
 */
public abstract class AbstractAvatarSupplier implements IAvatarSupplier {

    private final String contentType;

    protected AbstractAvatarSupplier() {
        this(null);
    }

    protected AbstractAvatarSupplier(final String contentType) {
        this.contentType = trimToNull(contentType);
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
