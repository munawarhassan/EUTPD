package com.pmi.tpd.startup.check;

import java.io.File;
import java.io.IOException;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class HomeLockUtils {

    private HomeLockUtils() {
    }

    /**
     * @param home
     * @return
     * @throws IOException
     */
    public static boolean lockHome(final File home) throws IOException {
        return getLockFile(home).createNewFile();
    }

    /**
     * @param home
     * @throws IOException
     */
    public static void unLockHome(final File home) throws IOException {
        final File lockFile = getLockFile(home);
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    /**
     * @param home
     * @return
     * @throws IOException
     */
    public static File getLockFile(final File home) {
        return new File(home, ".app-home.lock");
    }
}
