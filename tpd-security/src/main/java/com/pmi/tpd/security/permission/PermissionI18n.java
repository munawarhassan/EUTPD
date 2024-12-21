package com.pmi.tpd.security.permission;

import static java.lang.String.format;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.I18nKey;

/**
 * Provides access to i18n-ed name, short name and description of
 * {@link Permission}s.
 *
 * @since 2.0
 * @see Permission#getI18n()
 */
public class PermissionI18n {

  /** */
  private static final String I18N_KEY_BASE = "app.service.permission.%s.%s";

  /** */
  private static final String I18N_DESCRIPTION = "description";

  /** */
  private static final String I18N_NAME = "name";

  /** */
  private static final String I18N_SHORT = "short";

  /** */
  private final I18nKey description;

  /** */
  private final I18nKey name;

  /** */
  private final I18nKey shortName;

  PermissionI18n(final Permission permission, final Object[] arguments) {
    final String permissionForI18n = permission.name().toLowerCase().replace("_", ".");
    this.description = new I18nKey(i18nKeyFor(permissionForI18n, I18N_DESCRIPTION), arguments);
    this.name = new I18nKey(i18nKeyFor(permissionForI18n, I18N_NAME), arguments);
    this.shortName = new I18nKey(i18nKeyFor(permissionForI18n, I18N_SHORT), arguments);
  }

  @Nonnull
  public I18nKey description() {
    return description;
  }

  @Nonnull
  public I18nKey name() {
    return name;
  }

  @Nonnull
  public I18nKey shortName() {
    return shortName;
  }

  private static String i18nKeyFor(final String permissionKey, final String suffix) {
    return format(I18N_KEY_BASE, permissionKey, suffix);
  }
}
