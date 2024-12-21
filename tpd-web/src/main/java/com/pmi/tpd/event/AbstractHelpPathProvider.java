package com.pmi.tpd.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.pmi.tpd.help.AbstractHelpPathService;
import com.pmi.tpd.help.IHelpPathService;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractHelpPathProvider {

    /** */
    private IHelpPathService helpPathService;

    /** */
    private final Logger log;

    /** */
    private boolean uninitialised;

    /**
     *
     */
    public AbstractHelpPathProvider() {
        log = LoggerFactory.getLogger(getClass());
        uninitialised = true;
    }

    protected HelpDetails getHelp(final String key, final String defaultTitle, final String defaultUrl) {
        String title = null;
        String url = null;

        final IHelpPathService service = getHelpPathService();
        if (service != null) {
            title = service.getPageTitle(key);
            url = service.getPageUrl(key);
        }

        return new HelpDetails(StringUtils.defaultIfBlank(title, defaultTitle),
                StringUtils.defaultIfBlank(url, defaultUrl));
    }

    private IHelpPathService getHelpPathService() {
        if (uninitialised) {
            // No matter what happens, we only try to initialise the HelpPathService once
            uninitialised = false;

            try {
                final InputStream resourceAsStream = new ClassPathResource("help-paths.properties").getInputStream();

                final Properties helpPaths = new Properties();
                helpPaths.load(resourceAsStream);

                helpPathService = new AbstractHelpPathService() {

                    @Override
                    protected String getProperty(final String key) {
                        return helpPaths.getProperty(key);
                    }
                };
            } catch (final IOException e) {
                log.warn("Could not load help path configuration. Help links will use default URLs", e);
            }
        }
        return helpPathService;
    }

    /**
     * @author Christophe Friederich
     */
    protected static final class HelpDetails {

        /** */
        private final String title;

        /** */
        private final String url;

        private HelpDetails(final String title, final String url) {
            this.title = title;
            this.url = url;
        }

        /**
         * @return
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return
         */
        public String getUrl() {
            return url;
        }
    }
}
