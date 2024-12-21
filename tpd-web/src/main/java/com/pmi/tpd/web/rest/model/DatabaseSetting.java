package com.pmi.tpd.web.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.database.DbTypeBean;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Getter
@Builder
@JsonDeserialize(builder = DatabaseSetting.DatabaseSettingBuilder.class)
@JsonSerialize
public class DatabaseSetting {

    /** */
    private final String type;

    /** */
    private final String databaseName;

    /** */
    private final String hostname;

    /** */
    private final int port;

    /** */
    private final String username;

    /** */
    private final String password;

    public static DatabaseSetting createDefault(final IApplicationProperties applicationProperties) {
        return DatabaseSetting.builder()
                .type(DbTypeBean.DEFAULT.getKey())
                .hostname(DbTypeBean.DEFAULT.getDefaultHostName())
                .databaseName(DbTypeBean.DEFAULT.getDefaultDatabaseName())
                .port(Integer.valueOf(DbTypeBean.DEFAULT.getDefaultPort()))
                .username(DbTypeBean.DEFAULT.getDefaultUserName())
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DatabaseSettingBuilder {

    }

}