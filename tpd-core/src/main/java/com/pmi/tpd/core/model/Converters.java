package com.pmi.tpd.core.model;

import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.eu.ceg.SubmissionTypeEnum;
import org.joda.time.DateTime;

import com.pmi.tpd.security.permission.Permission;

public class Converters {

    @Converter(autoApply = true)
    public static class PermissionConverter implements AttributeConverter<Permission, Integer> {

        @Override
        public Integer convertToDatabaseColumn(final Permission value) {
            return value == null ? null : value.getId();
        }

        @Override
        public Permission convertToEntityAttribute(final Integer value) {
            return value == null ? null : Permission.fromId(value);
        }

    }

    @Converter(autoApply = true)
    public static class SubmissionTypeConverter implements AttributeConverter<SubmissionTypeEnum, Integer> {

        @Override
        public Integer convertToDatabaseColumn(final SubmissionTypeEnum value) {
            return value == null ? null : value.value();
        }

        @Override
        public SubmissionTypeEnum convertToEntityAttribute(final Integer value) {
            return value == null ? null : SubmissionTypeEnum.fromValue(value);
        }

    }

    @Converter(autoApply = true)
    public static class JodaDateTimeConverter implements AttributeConverter<DateTime, Date> {

        @Override
        public Date convertToDatabaseColumn(final DateTime dateTime) {
            return dateTime == null ? null : dateTime.toDate();
        }

        @Override
        public DateTime convertToEntityAttribute(final Date date) {
            return date == null ? null : new DateTime(date);
        }
    }

}
