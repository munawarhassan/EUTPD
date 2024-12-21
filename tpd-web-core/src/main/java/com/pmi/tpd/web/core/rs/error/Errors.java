package com.pmi.tpd.web.core.rs.error;

import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.exception.ServiceException;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@JsonSerialize
public class Errors {

    /** */
    private final ArrayList<ErrorMessage> errors;

    /**
     *
     */
    public Errors() {
        errors = Lists.newArrayList();
    }

    /**
     * @param errors
     */
    public Errors(final Collection<ErrorMessage> errors) {
        this.errors = Lists.newArrayList(errors);
    }

    /**
     * @param error
     */
    public Errors(final String error) {
        this(new ErrorMessage(error));
    }

    /**
     * Construct a {@code Errors} instance with a single error containing {@code context} and {@code message}.
     *
     * @param context
     *            the error context
     * @param error
     *            error message
     */
    public Errors(final String context, final String error) {
        this(new ErrorMessage(context, error, null, null));
    }

    /**
     * @param e
     */
    public Errors(final ServiceException e) {
        this(new ErrorMessage(e));
    }

    /**
     * @param message
     */
    public Errors(final ErrorMessage message) {
        this(Collections.singletonList(message));
    }

    /**
     * @return
     */
    public List<ErrorMessage> getErrors() {
        return from(errors).filter(Predicates.notNull()).toList();
    }

    /**
     * @author Christophe Friederich
     * @since 2.0
     */
    public static class Builder {

        /** */
        private final ImmutableList.Builder<ErrorMessage> errorsBuilder = ImmutableList.builder();

        /**
         * @param value
         * @return
         */
        public Builder add(final String value) {
            errorsBuilder.add(new ErrorMessage(value));
            return this;
        }

        /**
         * @param value
         * @return
         */
        public Builder add(final ErrorMessage value) {
            errorsBuilder.add(value);
            return this;
        }

        /**
         * @param values
         * @return
         */
        public Builder addErrors(final Iterable<String> values) {
            errorsBuilder.addAll(Iterables.transform(values, input -> new ErrorMessage(input)));
            return this;
        }

        /**
         * @param values
         * @return
         */
        public Builder addErrorMessages(final Iterable<ErrorMessage> values) {
            errorsBuilder.addAll(values);
            return this;
        }

        /**
         * @return
         */
        public Errors build() {
            return new Errors(errorsBuilder.build());
        }
    }

    // REST Documentation Examples
    public static final Errors EXAMPLE = new Errors("A detailed error message.");

    /**
     *
     */
    public static final Errors VALIDATION_EXAMPLE = new Errors(
            ImmutableList.of(new ErrorMessage("field_a", "A detailed validation error message for field_a."),
                new ErrorMessage("A detailed error message.")));
}