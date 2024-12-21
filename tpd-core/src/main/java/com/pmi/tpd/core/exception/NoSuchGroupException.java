package com.pmi.tpd.core.exception;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Specialisation of {@link NoSuchEntityException} thrown to indicate the named group does not exist.
 *
 * @since 2.0
 */
public class NoSuchGroupException extends NoSuchEntityException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String groupName;

    public NoSuchGroupException(@Nonnull final KeyedMessage message, @Nonnull final String groupName) {
        super(message);

        this.groupName = checkNotNull(groupName, "groupName");
    }

    /**
     * Retrieves the name of the group which was requested but not found.
     *
     * @return the group name
     */
    @Nonnull
    public String getGroupName() {
        return groupName;
    }
}
