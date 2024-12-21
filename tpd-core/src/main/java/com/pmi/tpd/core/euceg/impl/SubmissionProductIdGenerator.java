package com.pmi.tpd.core.euceg.impl;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.joda.time.LocalDate;

import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;

public class SubmissionProductIdGenerator extends TableGenerator {

    public static final String NAME = SubmissionProductIdGenerator.class.getName();

    @Override
    public void configure(final Type type, final Properties params, final ServiceRegistry serviceRegistry)
            throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
    }

    @Override
    public Serializable generate(final SharedSessionContractImplementor session, final Object obj) {
        final ISubmissionEntity entity = (ISubmissionEntity) obj;
        final Serializable id = super.generate(session, obj);
        return generate(entity.getSubmitterId(), id);
    }

    public static String generate(final String submitterId, final Serializable id) {
        final LocalDate date = LocalDate.now();
        final int year = date.getYearOfCentury();
        return String.format("%s-%02d-%05d", submitterId, year, id);
    }

    public static Long toId(final String productId) {
        final String id = productId.substring(9);
        return Long.valueOf(id);
    }

}
