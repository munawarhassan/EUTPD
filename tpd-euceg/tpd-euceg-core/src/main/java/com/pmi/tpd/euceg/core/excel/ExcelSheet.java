package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;

/**
 * @author Christohe Friederich
 * @since 2.5
 */
public final class ExcelSheet {

    public enum ConvertType {
        importExcel,
        exportExcel,
        both
    }

    /** */
    private final int index;

    /** */
    private final String name;

    private final boolean required;

    private final ConvertType convertType;

    @Nonnull
    public static ExcelSheet create(@Nonnull final String name,
        final int index,
        final boolean required,
        @Nonnull final ConvertType convertType) {
        return new ExcelSheet(name, index, required, convertType);
    }

    @Nonnull
    public static ExcelSheet create(@Nonnull final String name, final int index, final boolean required) {
        return new ExcelSheet(name, index, required, ConvertType.both);
    }

    @Nonnull
    public static ExcelSheet create(@Nonnull final String name, final int index) {
        return new ExcelSheet(name, index, false, ConvertType.both);
    }

    private ExcelSheet(@Nonnull final String name, final int index, final boolean required,
            @Nonnull final ConvertType convertType) {
        checkNotNull(convertType, "convertType");
        checkHasText(name, "name can not be null or empty");
        state(index > -1, "sheet index must be positive");
        this.name = name;
        this.index = index;
        this.required = required;
        this.convertType = convertType;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ConvertType getConvertType() {
        return convertType;
    }

    /**
     * @return Returns {@code true} if the group is required during import, {@code false} otherwise.
     */
    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("name", name)
                .add("required", required)
                .add("index", index)
                .toString();
    }
}
