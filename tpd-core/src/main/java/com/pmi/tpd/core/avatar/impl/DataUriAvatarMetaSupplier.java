package com.pmi.tpd.core.avatar.impl;

import java.util.function.Supplier;

import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;

/**
 * Supplies an {@link IAvatarSupplier} {@link IInternalAvatarService#createSupplierFromDataUri(String) created} from a
 * data URI.
 * <p>
 * This supplier is <i>lazy</i>. It transforms the data URI to an {@link IAvatarSupplier} on-demand, rather than in the
 * constructor. This is really an implementation detail of how the supplier is used; lazily transforming the data URI
 * ensures validation happens in the correct order.
 */
public class DataUriAvatarMetaSupplier implements Supplier<IAvatarSupplier> {

    private final IInternalAvatarService avatarService;

    private final String dataUri;

    /**
     * Constructs a new {@code DataUriAvatarMetaSupplier} which will return an {@link IAvatarSupplier} created from the
     * provided {@code dataUri}.
     * <p>
     * The data URI is required to be non-{@code null}. The null check is left to the {@link IInternalAvatarService
     * avatar service}.
     *
     * @param avatarService
     *            the {@link IInternalAvatarService service} to use to create a supplier from the URI
     * @param dataUri
     *            the data URI containing the Base64-encoded avatar
     */
    public DataUriAvatarMetaSupplier(final IInternalAvatarService avatarService, final String dataUri) {
        this.avatarService = avatarService;
        this.dataUri = dataUri;
    }

    /**
     * Constructs an {@link IAvatarSupplier} from the data URI provided during object construction.
     *
     * @return the new supplier
     */
    @Override
    public IAvatarSupplier get() {
        return avatarService.createSupplierFromDataUri(dataUri);
    }
}
