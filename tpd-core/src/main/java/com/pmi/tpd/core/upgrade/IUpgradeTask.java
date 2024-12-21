package com.pmi.tpd.core.upgrade;

import java.util.Collection;

/**
 * <p>
 * IUpgradeTask interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUpgradeTask {

    /**
     * This determines if the upgrade should be run as part of the SetupComplete task.
     *
     * @author Christophe Friederich
     */
    enum SetupType {
        /**
         * Means the upgrade will ONLY be run on clean setup instances and never on general upgrade paths.
         */
        onlySetup,
        /**
         * if this should be run as part of setup as well as general upgrade paths.
         */
        setupAndBuild,
        /**
         * Means the task will be run only on general upgrade paths.
         */
        onlyBuild
    }

    /**
     * Gets indicating whether this task is setup.
     *
     * @return a {@link com.pmi.tpd.core.upgrade.IUpgradeTask.SetupType} object.
     */
    SetupType getSetup();

    /**
     * <p>
     * getVersion.
     * </p>
     *
     * @return Returns the version that this upgrade is applicable to.
     */
    String getVersion();

    /**
     * <p>
     * getBuildNumber.
     * </p>
     *
     * @return The build number that this upgrade is applicable to.
     */
    String getBuildNumber();

    /**
     * A short (&lt;50 chars) description of the upgrade action.
     *
     * @return a {@link java.lang.String} object.
     */
    String getShortDescription();

    /**
     * Perform the upgrade.
     *
     * @throws java.lang.Exception
     *             if any.
     */
    void doUpgrade() throws Exception;

    /**
     * <p>
     * getErrors.
     * </p>
     *
     * @return Returns any errors that occur. Each entry is a string.
     */
    Collection<String> getErrors();

    /**
     * Track status of a task this session, if isTaskDone(String) returns true you don't need to do it again.
     */
    class Status {

        private Status() {
        }

        /**
         * @param taskId
         */
        public static void setTaskDone(final String taskId) {
            System.setProperty("app.task." + taskId + ".complete", "true");
        }

        /**
         * @param taskId
         * @return
         */
        public static boolean isTaskDone(final String taskId) {
            return System.getProperty("app.task." + taskId + ".complete") != null;
        }
    }

}
