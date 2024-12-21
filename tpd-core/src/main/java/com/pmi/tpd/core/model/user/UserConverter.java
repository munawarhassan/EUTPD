package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.user.spi.IUserRepository;

public final class UserConverter {

    /** */
    IUserRepository userRepository;

    /**
     * @param userRepository
     */
    public UserConverter(@Nonnull final IUserRepository userRepository) {
        this.userRepository = Assert.checkNotNull(userRepository, "userRepository");
    }

    public UserEntity convertToEntityUser(final IUser user) {
        if (user == null) {
            return null;
        }
        if (user instanceof UserEntity) {
            return (UserEntity) user;
        }

        return this.userRepository.getById(user.getId());
    }
}
