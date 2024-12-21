package com.pmi.tpd.euceg.core.support;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.cloudbees.diff.Diff;
import com.pmi.tpd.euceg.api.Eucegs;

public class EucegXmlDiff {

    /** */
    private final String originalId;

    /** */
    private String revisedId = null;

    /** */
    private final int numContextLines;

    /** */
    private final String original;

    /** */
    private final String revised;

    /***
     * get XML representation of the object. XML formatted for display purpose
     *
     * @param <T>
     *                base type of the object
     * @param obj
     *                the object
     * @param wrapped
     *                object is wrapped ?
     * @return an XML representation suited for display
     */
    @Nullable
    public static <T> String getXml(@Nullable final T obj, @Nonnull final Class<T> clazz, final boolean wrapped) {
        if (obj == null) {
            return null;
        }
        return Eucegs.marshal(wrapped ? Eucegs.wrap(obj, clazz) : obj, true);
    }

    /***
     * create a new XML difference tool to compare original and revised
     *
     * @param originalId
     *                   item original id
     * @param original
     *                   original item
     * @param revised
     *                   revised item
     * @throws IOException
     */
    public EucegXmlDiff(@Nonnull final String originalId, @Nullable final String original,
            @Nonnull final String revised) throws IOException {
        this(originalId, original, revised, 10);
    }

    /***
     * create a new XML difference tool to compare original and revised
     *
     * @param originalId
     *                        item original id
     * @param original
     *                        original item
     * @param revised
     *                        revised item
     * @param numContextLines
     *                        number of lines to include in diff
     * @throws IOException
     */
    public EucegXmlDiff(@Nonnull final String originalId, @Nullable final String original,
            @Nonnull final String revised, final int numContextLines) throws IOException {

        this.original = original;
        this.revised = requireNonNull(revised);

        this.originalId = requireNonNull(originalId);
        this.numContextLines = numContextLines;
    }

    /**
     * set revised Id.
     *
     * @param revisedId
     * @return this EucegXmlDiff
     */
    @Nonnull
    public EucegXmlDiff withRevisedId(final String revisedId) {
        this.revisedId = revisedId;
        return this;
    }

    /**
     * Generate a diff result between two items.
     *
     * @return Returns a {@link DiffResult} representing a diff result between two items.
     */
    @Nonnull
    public DiffResult result() {
        try {
            DiffChange change = DiffChange.Unchanged;
            final Diff diff = Diff.diff(new StringReader(this.original == null ? "" : this.original),
                new StringReader(this.revised),
                false);
            if (diff.size() > 0) {
                change = DiffChange.Modified;
            }
            String patch = diff.toUnifiedDiff(originalId,
                revisedId != null ? revisedId : originalId,
                new StringReader(this.original == null ? "" : this.original),
                new StringReader(revised),
                numContextLines);

            // fix header for added, Diff.diff doesn't accept null reader as parameter.
            if (this.original == null) {
                change = DiffChange.Added;
                patch = String.format("diff --git a/%s b/%s\nnew file mode 100644\n",
                    originalId,
                    revisedId != null ? revisedId : originalId) + patch;
                patch = patch.replace("--- " + (revisedId != null ? revisedId : originalId), "--- /dev/null");
                patch = patch.replace("+++ " + originalId, "+++ b/" + originalId);
            }
            return new DiffResult(patch, change);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Represent the diff result of method {@link EucegXmlDiff#result()}.
     *
     * @author devacfr
     * @since 2.4
     */
    public static class DiffResult {

        private final DiffChange change;

        private final String patch;

        private DiffResult(@Nonnull final String patch, @Nonnull final DiffChange change) {
            this.patch = requireNonNull(patch);
            this.change = requireNonNull(change);
        }

        /**
         * @return Returns the change status of patch.
         */
        @Nonnull
        public DiffChange getChange() {
            return change;
        }

        /**
         * @return Returns a {@link String} representing the diff patch.
         */
        @Nonnull
        public String getPatch() {
            return patch;
        }
    }

}
