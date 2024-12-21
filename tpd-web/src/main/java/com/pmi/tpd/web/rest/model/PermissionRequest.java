package com.pmi.tpd.web.rest.model;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.PermissionI18n;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize
public class PermissionRequest {

    /** */
    @Nonnull
    private final String name;

    /** */
    private final String description;

    /**
     * @param permission
     * @param i18nService
     * @return
     */
    public static PermissionRequest forPermission(final Permission permission, final I18nService i18nService) {
        final PermissionI18n i18n = permission.getI18n();
        return new PermissionRequest(i18nService.getMessage(i18n.name().getKey(), Product.getName()),
                i18nService.getMessage(i18n.description().getKey(), Product.getName()));
    }

}
