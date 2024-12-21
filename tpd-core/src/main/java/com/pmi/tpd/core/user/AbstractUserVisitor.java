package com.pmi.tpd.core.user;

import com.pmi.tpd.api.user.IUserVisitor;
import com.pmi.tpd.core.model.user.UserEntity;

/**
 * @since 2.0
 */
public abstract class AbstractUserVisitor<T> implements IUserVisitor<T> {

    public T visit(final UserEntity user) {
        return null;
    }

}
