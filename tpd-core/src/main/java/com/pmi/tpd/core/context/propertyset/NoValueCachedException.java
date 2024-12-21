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
package com.pmi.tpd.core.context.propertyset;

/**
 * <p>
 * NoValueCachedException class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class NoValueCachedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2497306200969724730L;

    /** {@inheritDoc} */
    @Override
    public synchronized Throwable fillInStackTrace() {
        // This Exception is used for control flow only.
        // Don't do this expensive operation.
        return this;
    }
}
