package com.pmi.tpd.core.dbunit;

import java.util.Arrays;
import java.util.Collection;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized factory that recognizes Drby data types.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DerbyDataTypeFactory extends DefaultDataTypeFactory {

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DerbyDataTypeFactory.class);

    /**
     * Database product names supported.
     */
    private static final Collection<String> DATABASE_PRODUCTS = Arrays.asList(new String[] { "Derby" });

    /**
     * @see org.dbunit.dataset.datatype.IDbProductRelatable#getValidDbProducts()
     */
    @Override
    public Collection<String> getValidDbProducts() {
        return DATABASE_PRODUCTS;
    }

    @Override
    public DataType createDataType(final int sqlType, final String sqlTypeName) throws DataTypeException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createDataType(sqlType={}, sqlTypeName={}) - start", String.valueOf(sqlType), sqlTypeName);
        }

        return super.createDataType(sqlType, sqlTypeName);
    }
}
