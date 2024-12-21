package com.pmi.tpd.euceg.core.excel;

import javax.annotation.concurrent.Immutable;

/**
 * Class immutable describing a sheet in Excel workbook.
 *
 * @author devacfr christophefriederich@mac.com
 * @since 1.6
 */
@Immutable
public final class SheetDescriptor {

  /** **/
  private final int index;

  /** **/
  private final String name;

  /** **/
  private final boolean required;

  /**
   * Create a new instance of {@link SheetDescriptor}.
   *
   * @param index
   *                 the index.
   * @param name
   *                 the name.
   * @param required
   *                 indicate if the sheet is required for import.
   */
  SheetDescriptor(final int index, final String name, final boolean required) {
    super();
    this.index = index;
    this.name = name;
    this.required = required;
  }

  /**
   * @return Returns the index of sheet.
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Returns {@code true} whether the sheet is required during import,
   *         {@code false} otherwise.
   * @see GroupDescriptor#getRequiredImportedSheets(org.apache.poi.ss.usermodel.Workbook)
   */
  public boolean isRequired() {
    return required;
  }

}
