package com.pmi.tpd.euceg.core.importer;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

/**
 * Represents the result of import.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @param <T>
 *            the type of result objects of import.
 * @param <V>
 *            the type of validation result.
 * @see IImporter#importFromExcel(java.io.InputStream, String[])
 */
public interface IImporterResult<T> {

  /**
   * Gets the list of results.
   *
   * @return Returns a list of T representing the result of import
   */
  @Nonnull
  List<T> getResults();

  /**
   * Gets the validation result of import.
   *
   * @return Returns the validation result.
   */
  @Nonnull
  ValidationResult getValidationResult();
}
