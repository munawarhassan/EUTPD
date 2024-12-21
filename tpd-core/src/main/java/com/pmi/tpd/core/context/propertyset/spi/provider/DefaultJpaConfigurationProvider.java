/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.core.context.propertyset.spi.provider;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;

/**
 * Default implementation of {@link IJpaConfigurationProvider} interface.
 *
 * @see IJpaConfigurationProvider
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultJpaConfigurationProvider implements IJpaConfigurationProvider {

    /** property dao used. */
    private final IPropertySetDAO propertySetDAO;

    /**
     * Create new instance of {@link DefaultJpaConfigurationProvider}.
     *
     * @param propertySetDAO
     *            a property dao to use.
     */
    public DefaultJpaConfigurationProvider(@Nonnull final IPropertySetDAO propertySetDAO) {
        this.propertySetDAO = Assert.checkNotNull(propertySetDAO, "propertySetDAO");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IPropertySetDAO getPropertySetDAO() {
        return propertySetDAO;
    }

}
