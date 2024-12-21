package com.pmi.tpd.help;

import org.apache.commons.lang3.StringUtils;

/**
 * This class contains the general HelpPathService logic for help paths, and only {@link #getProperty(String)} needs to
 * be implemented, which may defer to a properties file or i18nService.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractHelpPathService implements IHelpPathService {

    /**
     * @param key
     * @return
     */
    protected abstract String getProperty(String key);

    @Override
    public String getPageAlt(final String topicKey) {
        return topicKey == null ? null : getProperty(topicKey + ".alt");
    }

    @Override
    public String getPageKey(final String topicKey) {
        return topicKey == null ? null : getProperty(topicKey);
    }

    @Override
    public String getPageTitle(final String topicKey) {
        return topicKey == null ? null : getProperty(topicKey + ".title");
    }

    @Override
    public String getPageUrl(final String topicKey) {
        final String helpPrefix = getPrefix(topicKey);
        final String helpPageKey = getPageKey(topicKey);
        if (helpPageKey != null && helpPrefix != null) {
            return String.format("%s/%s", helpPrefix, helpPageKey);
        } else {
            return null;
        }
    }

    @Override
    public String getPrefix() {
        return getProperty("app.help.prefix");
    }

    private String getPrefix(final String topicKey) {
        if (StringUtils.startsWith(topicKey, "app.kb.")) {
            return getProperty("app.kb.prefix");
        } else if (StringUtils.startsWith(topicKey, "app.wac.")) {
            return getProperty("app.wac.prefix");
        } else if (StringUtils.startsWith(topicKey, "app.go.")) {
            return getProperty("app.go.prefix");
        }
        return getPrefix();
    }

}
