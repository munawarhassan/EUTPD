package com.pmi.tpd.core.model.user;

import com.pmi.tpd.core.model.user.GroupEntity.Builder;

public class Groups {

    private static Long inc = 0L;

    private static Long nextId() {
        return ++inc;
    }

    private static GroupTestBuilder builder() {
        return new GroupTestBuilder();
    }

    public static GroupEntity.Builder ok() {
        return builder().id(nextId()).name("users");
    }

    public static GroupEntity.Builder users() {
        return builder().id(nextId()).name("users");
    }

    public static GroupEntity.Builder bad() {
        return builder().id(nextId()).name("noname");
    }

    public static class GroupTestBuilder extends GroupEntity.Builder {

        protected GroupTestBuilder() {
            super();
        }

        @Override
        public Builder id(final Long value) {
            return super.id(value);
        }
    };
}
