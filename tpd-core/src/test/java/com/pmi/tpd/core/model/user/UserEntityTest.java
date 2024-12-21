package com.pmi.tpd.core.model.user;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class UserEntityTest extends TestCase {

    @Test
    public void testBuilderSlug() {
        builder();
    }

    private UserEntity.Builder builder() {
        return new UserEntity.Builder();
    }

}
