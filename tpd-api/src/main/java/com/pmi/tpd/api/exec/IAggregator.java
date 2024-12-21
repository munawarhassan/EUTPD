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
package com.pmi.tpd.api.exec;

/**
 * Callback interface allowing to retrieve returned value from {@link IRunnableTask#call()} method.
 * <p>
 * <b>Note:</b>The implementation SHOULD be thread safe. Using the high-level APIs in the {@code java.util.concurrent}
 * packages.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 * @param <R>
 *            the type to aggregate returned value from {@link IRunnableTask#call()} method.
 */
public interface IAggregator<R> {

    /**
     * Called after each completed {@link IRunnableTask task} with this returned value.
     *
     * @param aggregatable
     *            the returned value by a completed {@link IRunnableTask task}.
     */
    void aggregate(R aggregatable);

}
