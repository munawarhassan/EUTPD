package com.pmi.tpd.euceg.backend.core.message;

import javax.activation.DataSource;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
public abstract class Payload {

    private String name;

    private String mimeContentId;

    private String mimeType;

    private String fileName;

    public abstract DataSource getDataSource();

}
