package com.pmi.tpd.euceg.core.util.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Represents a result of a validation execution. Contains a set of {@link ValidationFailure ValidationFailures}that
 * occured in a given context. All failures are kept in the same order they were added.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public class ValidationResult implements Serializable {

    /** */
    @Nonnull
    private static final ValidationResult EMPTY = new ValidationResult();

    /**
     *
     */
    private static final long serialVersionUID = 1579697072241636599L;

    /** */
    private final List<ValidationFailure> failures;

    /**
     * @return Returns a instance of {@link ValidationResult} representing a empty result.
     */
    @Nonnull
    public static ValidationResult empty() {
        return EMPTY;
    }

    /**
     * <p>
     * Constructor for ValidationResult.
     * </p>
     */
    public ValidationResult() {
        failures = new ArrayList<>();
    }

    /**
     * Gets indicating whether there is not error.
     *
     * @return Returns {@code true} whether there is not error, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return this.failures.isEmpty();
    }

    /**
     * Add a failure to the validation result.
     *
     * @param failure
     *                failure to be added. It may not be null.
     * @see ValidationFailure
     */
    public void addFailure(final ValidationFailure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("failure cannot be null.");
        }

        failures.add(failure);
    }

    /**
     * Returns all failures added to this result, or empty list is result has no failures.
     *
     * @return a {@link java.util.List} object.
     */
    public List<ValidationFailure> getFailures() {
        return Collections.unmodifiableList(failures);
    }

    /**
     * Returns all failures related to the <code>source</code> object, or an empty list if there are no such failures.
     *
     * @param source
     *               it may be null.
     * @see ValidationFailure#getSource()
     * @return a {@link java.util.List} object.
     */
    public List<ValidationFailure> getFailures(final Object source) {
        final ArrayList<ValidationFailure> matchingFailures = new ArrayList<>(5);
        for (final ValidationFailure failure : failures) {
            if (nullSafeEquals(source, failure.getSource())) {
                matchingFailures.add(failure);
            }
        }

        return matchingFailures;
    }

    /**
     * Returns true if at least one failure has been added to this result. False otherwise.
     *
     * @return a boolean.
     */
    @JsonGetter()
    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    /**
     * <p>
     * hasFailures.
     * </p>
     *
     * @param source
     *               it may be null.
     * @return true if there is at least one failure for <code>source</code>. False otherwise.
     */
    public boolean hasFailures(final Object source) {
        for (final ValidationFailure failure : failures) {
            if (nullSafeEquals(source, failure.getSource())) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();
        final String separator = System.getProperty("line.separator");

        for (final ValidationFailure failure : failures) {
            if (ret.length() > 0) {
                ret.append(separator);
            }
            ret.append(failure);
        }

        return ret.toString();
    }

    /**
     * Compares two objects similar to "Object.equals(Object)". Unlike Object.equals(..), this method doesn't throw an
     * exception if any of the two objects is null.
     *
     * @param o1
     *           a {@link java.lang.Object} object.
     * @param o2
     *           a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public static boolean nullSafeEquals(final Object o1, final Object o2) {

        if (o1 == null) {
            return o2 == null;
        }

        // Arrays must be handled differently since equals() only does
        // an "==" for an array and ignores equivalence. If an array, use
        // the Jakarta Commons Language component EqualsBuilder to determine
        // the types contained in the array and do individual comparisons.
        if (o1.getClass().isArray()) {
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(o1, o2);
            return builder.isEquals();
        } else { // It is NOT an array, so use regular equals()
            return o1.equals(o2);
        }
    }
}
