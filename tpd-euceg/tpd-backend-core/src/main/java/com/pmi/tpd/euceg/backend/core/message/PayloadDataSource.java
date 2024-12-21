package com.pmi.tpd.euceg.backend.core.message;

import javax.activation.DataSource;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PayloadDataSource extends Payload {

    private DataSource content;

    @Override
    public DataSource getDataSource() {
        return content;
    }
}
