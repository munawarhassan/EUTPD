package com.pmi.tpd.core.model.user;

public class Users {

    public static final String NOTADMIN_USER = "notadmin";

    public static final String ADMIN_USER = "admin";

    private static Long inc = 0L;

    private static Long nextId() {
        return ++inc;
    }

    private static UserTestBuilder builder() {
        return new UserTestBuilder();
    }

    public static UserEntity.Builder adminUser() {
        return builder().id(nextId()).username(ADMIN_USER);
    }

    public static UserEntity.Builder noAdminUser() {
        return builder().id(nextId()).username(NOTADMIN_USER);
    }

    public static UserEntity.Builder testerUser() {
        return builder().id(nextId())
                .activated(true)
                .displayName("tester Unit")
                .email("tester@com.com")
                .username("tester")
                .password("ubuntuLucidLynx2010");
    }

    public static UserEntity.Builder userUser() {
        return builder().id(nextId())
                .activated(true)
                .displayName("user")
                .email("user@com.com")
                .username("user")
                .password("12w3d55uLud");

    }

    public static UserEntity.Builder ok() {
        return builder().id(nextId()).username("toto");
    }

    public static UserEntity.Builder bad() {
        return UserEntity.builder();
    }

    public static class UserTestBuilder extends UserEntity.Builder {

        @Override
        public UserEntity.Builder id(final Long value) {
            return super.id(value);
        }
    };
}
