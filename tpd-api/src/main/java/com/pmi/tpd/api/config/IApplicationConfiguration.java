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
package com.pmi.tpd.api.config;

import java.io.File;
import java.nio.file.Path;

import com.pmi.tpd.api.Environment;
import com.pmi.tpd.api.versioning.IBuildUtilsInfo;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IApplicationConfiguration {

    /**
     * @return the log directory. The directory is created if needed and is guaranteed to exists.
     */
    Path getLogDirectory();

    /**
     * Gets the location of log file.
     *
     * @return Returns the location of log file.
     */
    File getLogFile();

    /**
     * @return the configuration directory.
     */
    Path getConfigurationDirectory();

    /**
     * Creates if needed and returns a subdirectory relative the "home directory".
     *
     * @param subpath
     *                the subpath to create.
     * @return the directory.
     */
    Path getHomeDirectory(String subpath);

    /**
     * @return the home directory as file. The directory is created if needed and is guaranteed to exists.
     */
    Path getHomeDirectory();

    /**
     * @return the shared home directory as file. The directory is created if needed and is guaranteed to exists.
     * @since 1.3
     */
    Path getSharedHomeDirectory();

    /**
     * @return
     */
    Path getAttachmentsDirectory();

    /**
     * @return
     * @since 2.4
     */
    Path getDataDirectory();

    /**
     * @return
     * @since 1.4
     */
    Path getIndexDirectory();

    /**
     * @return the temporary directory.
     */
    Path getTemporaryDirectory();

    /**
     * @return the wastebasket directory.
     */
    Path getWastebasketDirectory();

    /**
     * @return the working directory as file. The directory is created if needed and is guaranteed to exists.
     */
    Path getWorkingDirectory();

    /**
     * @return the plugins directory as file. The directory is created if needed and is guaranteed to exists.
     */
    Path getInstalledPluginsDirectory();

    /**
     * @return the blackup directory as file. The directory is created if needed and is guaranteed to exists.
     */
    Path getBackupDirectory();

    /**
     * @return the report directory as file. The directory is created if needed and is guaranteed to exists.
     */
    Path getReportDirectory();

    /**
     * Gets the current active Environment.
     *
     * @return Return the current active Environment if exists, otherwise {@link Environment#PRODUCTION}.
     */
    Environment getEnvironment();

    /**
     * Gets the indicating whether the application is configured in Development mode.
     *
     * @return Returns {@code true} whether the application is configured in Development mode, otherwise {@code false}.
     */
    boolean isDevMode();

    /**
     * Gets a build information.
     *
     * @return Returns a instance {@link IBuildUtilsInfo} containing the last build information from CMS.
     */
    IBuildUtilsInfo getBuildUtilsInfo();

}
