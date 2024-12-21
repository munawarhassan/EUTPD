package com.pmi.tpd.core.backup;

/**
 * Receives callbacks for updates on the progress of the backup client.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBackupClientProgressCallback {

    /**
     * Called when new progress is known about the backup client.
     *
     * @param percentage
     *            the percentage complete of the backup client
     */
    void onProgressUpdate(int percentage);
}
