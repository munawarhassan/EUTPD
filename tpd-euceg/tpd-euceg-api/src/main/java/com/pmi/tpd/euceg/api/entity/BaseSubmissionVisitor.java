package com.pmi.tpd.euceg.api.entity;

import java.util.function.Function;

import javax.annotation.Nonnull;

import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.TobaccoProductSubmission;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class BaseSubmissionVisitor<T> implements ISubmissionVisitor<T>, Function<ISubmissionEntity, T> {

    @Override
    public T apply(final ISubmissionEntity entity) {
        return entity.accept(this);
    }

    @Override
    public T visit(@Nonnull final ISubmissionEntity entity) {
        if (entity.getSubmission() != null) {
            visit(entity.getSubmission());
        }
        return null;
    }

    @Override
    public Submission visit(@Nonnull final Submission submission) {
        if (submission instanceof TobaccoProductSubmission) {
            return visit((TobaccoProductSubmission) submission);
        } else if (submission instanceof EcigProductSubmission) {
            return visit((EcigProductSubmission) submission);
        }
        return submission;
    }

    @Override
    public Submission visit(@Nonnull final EcigProductSubmission submission) {
        return submission;
    }

    @Override
    public Submission visit(@Nonnull final TobaccoProductSubmission submission) {
        return submission;
    }

}
