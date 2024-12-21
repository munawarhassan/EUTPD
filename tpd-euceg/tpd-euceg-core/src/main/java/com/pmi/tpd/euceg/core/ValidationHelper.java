package com.pmi.tpd.euceg.core;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.eu.ceg.SubmitterDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.util.validation.ValidationFailure;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

public final class ValidationHelper {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHelper.class);

    /** */
    @Nonnull
    public static Schema SUBMISSION_SCHEMA;

    @Nonnull
    public static Schema SUBMITTER_SCHEMA;

    static {
        try {
            final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Resource resource = resourceLoader.getResource("classpath:euceg/submissions.xsd");
            SUBMISSION_SCHEMA = sf.newSchema(resource.getURL());
            resource = resourceLoader.getResource("classpath:euceg/submitters.xsd");
            SUBMITTER_SCHEMA = sf.newSchema(resource.getURL());

        } catch (final SAXException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private ValidationHelper() {
    }

    public static boolean validateSubmission(@Nonnull final Object obj,
        @Nonnull final ValidationResult validationResult) throws JAXBException {
        return validate(SUBMISSION_SCHEMA, obj, validationResult);
    }

    public static boolean validateSubmitter(@Nonnull final SubmitterDetails obj,
        @Nonnull final ValidationResult validationResult) throws JAXBException {
        return validate(SUBMITTER_SCHEMA, obj, validationResult);
    }

    private static boolean validate(@Nonnull final Schema schema,
        @Nonnull final Object obj,
        @Nonnull final ValidationResult validationResult) throws JAXBException {
        Preconditions.checkNotNull(validationResult, "validationResult");
        final Marshaller marshaller = Eucegs.createMarshaller();
        final MutableBoolean valid = new MutableBoolean(true);

        marshaller.setSchema(schema);
        marshaller.setEventHandler(event -> {
            switch (event.getSeverity()) {
                case ValidationEvent.ERROR:
                    validationResult.addFailure(new JaxbValidationFailure(obj, event));
                    valid.setValue(false);
                    break;
                case ValidationEvent.FATAL_ERROR:
                    validationResult.addFailure(new JaxbValidationFailure(obj, event));
                    valid.setValue(false);
                    break;
                case ValidationEvent.WARNING:
                    break;
                default:

                    break;
            }
            return true;
        });
        final StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        return valid.booleanValue();
    }

    public static class JaxbValidationFailure implements ValidationFailure {

        /**
         *
         */
        private static final long serialVersionUID = 8464118115080226892L;

        protected Object source;

        protected ValidationEvent error;

        /**
         * <p>
         * Constructor for SimpleValidationFailure.
         * </p>
         *
         * @param source
         *               a {@link java.lang.Object} object.
         * @param error
         *               a {@link java.lang.Object} object.
         */
        public JaxbValidationFailure(final Object source, final ValidationEvent error) {
            this.source = source;
            this.error = error;
        }

        /**
         * {@inheritDoc} Returns the error converted to String.
         */
        @Override
        public String getDescription() {
            return error.getMessage();
        }

        /**
         * {@inheritDoc} Returns object that failed the validation.
         */
        @Override
        @JsonIgnore
        public Object getSource() {
            return source;
        }

        /** {@inheritDoc} */
        @Override
        @JsonIgnore
        public ValidationEvent getError() {
            return error;
        }

        /**
         * {@inheritDoc} Returns a String representation of the failure.
         */
        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();

            buffer.append("Validation failure for ");
            final Object source = getSource();

            if (source == null) {
                buffer.append("[General]");
            } else {
                final String sourceLabel = source instanceof String ? source.toString() : source.getClass().getName();
                buffer.append(sourceLabel);
            }
            buffer.append(": ");
            buffer.append(getDescription());
            return buffer.toString();
        }
    }
}
