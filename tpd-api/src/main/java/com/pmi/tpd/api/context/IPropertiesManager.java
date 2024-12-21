package com.pmi.tpd.api.context;

import java.util.Optional;

import com.pmi.tpd.api.exception.InfrastructureException;

public interface IPropertiesManager {

    static final String PROPERTIES_MANAGER_BEAN_NAME = "propertiesManager";

    /**
     * @return Returns the property accessor.
     * @throws InfrastructureException
     *             error if the {@link IPropertyAccessor} used can not retrieves value from persistent layer.
     */
    Optional<IPropertyAccessor> getPropertyAccessor();

    void flush();

    /**
     * Refresh the properties from the database.
     */
    void refresh();

}