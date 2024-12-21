package com.pmi.tpd.euceg.core.importer;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Common interface for all importer.
 *
 * @author devacfr christophefriederich@mac.com
 * @param <T>
 *            the type of result objects of import.
 * @since 1.0
 */
public interface IImporter<T> {

    /**
     * import from excel file.
     *
     * @param input
     *                       the stream used (can <b>not</b> be {@code null}).
     * @param selectedSheets
     *                       selected sheets (can be {@code null}).
     * @return Returns the result of import.
     */
    @Nonnull
    IImporterResult<T> importFromExcel(@Nonnull InputStream input, @Nullable int[] selectedSheets);
}
