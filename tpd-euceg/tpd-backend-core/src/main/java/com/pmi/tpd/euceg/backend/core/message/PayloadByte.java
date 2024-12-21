package com.pmi.tpd.euceg.backend.core.message;

import javax.activation.DataSource;

import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PayloadByte extends Payload {

    private byte[] content;

    @Override
    public DataSource getDataSource() {
        return new ByteArrayDataSource(content, getMimeType());
    }
}