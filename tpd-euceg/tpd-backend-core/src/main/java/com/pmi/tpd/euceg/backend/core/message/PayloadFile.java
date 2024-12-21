package com.pmi.tpd.euceg.backend.core.message;

import java.io.File;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PayloadFile extends Payload {

    private File content;

    @Override
    public DataSource getDataSource() {
        return new FileDataSource(content);
    }
}
