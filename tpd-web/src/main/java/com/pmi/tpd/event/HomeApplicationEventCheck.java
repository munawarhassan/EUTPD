package com.pmi.tpd.event;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorAccessor;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.IApplicationEventCheck;
import com.pmi.tpd.api.util.FileUtils;
import com.pmi.tpd.startup.HomeDirectoryDetails;
import com.pmi.tpd.startup.HomeDirectoryResolver;

/**
 * Ensures the home environment variable has been set.
 * <p>
 * Note: Spring is not available in {@code ApplicationEventCheck}s--they are executed while is starting up, which
 * happens before the main application.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HomeApplicationEventCheck implements IApplicationEventCheck<ServletContext> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeApplicationEventCheck.class);

    @Override
    public void check(@Nonnull final IEventAdvisorAccessor advisorAccessor,
        @Nonnull final ServletContext servletContext) {
        HomeDirectoryDetails homeDirectoryDetails;
        try {
            homeDirectoryDetails = resolveHomeDirectory();
        } catch (final IllegalStateException e) {
            addError(advisorAccessor, e.getMessage());
            return;
        }

        final File home = homeDirectoryDetails.getHome();
        if (!home.exists()) {
            // Note: By this point, the initialisation of Logback should have created the home directory, if it
            // was possible to create it. While that's relying on a side effect from another framework, it
            // is a consistent side-effect: Logging will always be initialised before.
            final String message = formatErrorMessage(
                "%1$s %3$s [%2$s] cannot be created or %1$s does not have permission to access it",
                home,
                "Home");
            addError(advisorAccessor, message);
            return;
        }

        if (!verify(advisorAccessor, home, "Home")) {
            return;
        }

        final File sharedHome = homeDirectoryDetails.getSharedHome();
        if (!sharedHome.exists()) {
            // Create the app.shared.home if it does not exist. Contrast this with the app.home directory treatment,
            // which we expect to have been created by Logback initialization by the time we get here.
            try {
                FileUtils.mkdir(sharedHome);
            } catch (final IllegalStateException e) {
                addError(advisorAccessor, e.getMessage());
                return;
            }
        }

        verify(advisorAccessor, sharedHome, "Shared Home");
    }

    private boolean verify(final IEventAdvisorAccessor advisorAccessor, final File dir, final String dirName) {
        String messageTemplate = null;
        if (!dir.isDirectory()) {
            messageTemplate = "%1$s %3$s [%2$s] is not a directory";
        } else if (!dir.canRead()) {
            messageTemplate = "%1$s %3$s [%2$s] exists but is not readable by %1$s";
        } else if (!isWritable(dir)) {
            messageTemplate = "%1$s %3$s [%2$s] exists but is not writable by %1$s";
        } else if (!dir.canExecute()) {
            messageTemplate = "%1$s %3$s [%2$s] exists but is not accessible by %1$s";
        }

        if (messageTemplate != null) {
            final String message = formatErrorMessage(messageTemplate, dir, dirName);
            addError(advisorAccessor, message);
        }

        return messageTemplate == null;
    }

    private String formatErrorMessage(final String template, final File dir, final String dirName) {
        return String.format(template, Product.getName(), dir, dirName);
    }

    private void addError(final IEventAdvisorAccessor advisorAccessor, final String message) {
        LOGGER.error(message);
        advisorAccessor.publishEvent(new Event(advisorAccessor.getEventType("home").orElse(null), message,
                advisorAccessor.getEventLevel(EventLevel.FATAL).orElse(null)));
    }

    private boolean isWritable(final File home) {
        if (SystemUtils.IS_OS_WINDOWS) {
            // Windows can report that the directory is writable with File.canWrite(),
            // whereas it is not because of the ACLs: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
            // so instead, this checks whether application is able to create a file within the home directory
            try {
                final File f = File.createTempFile(Product.getName(), ".tmp", home);
                if (!f.delete()) {
                    f.deleteOnExit();
                }
                return true;
            } catch (final IOException e) {
                return false;
            }
        } else {
            return home.canWrite();
        }
    }

    protected HomeDirectoryDetails resolveHomeDirectory() {
        return new HomeDirectoryResolver().resolve();
    }

}
