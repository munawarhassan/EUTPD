package com.pmi.tpd.help;

/**
 * A service for retrieving help urls and related help information for a given help topic.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IHelpPathService {

    /**
     * @param topicKey
     *            the non-null key for the help topic
     * @return the alt (tool tip) to be displayed for the help topic or null if one was not found for the help topic
     */
    String getPageAlt(String topicKey);

    /**
     * @param topicKey
     *            the non-null key for the help topic
     * @return the Confluence page key for the help topic or null if one was not found for the help topic
     */
    String getPageKey(String topicKey);

    /**
     * @param topicKey
     *            the non-null key for the help topic
     * @return the title to be displayed for the help topic or null if one was not found for the help topic
     */
    String getPageTitle(String topicKey);

    /**
     * @param topicKey
     *            the non-null key for the help topic
     * @return the url for the help topic or null if one was not found for the help topic
     */
    String getPageUrl(String topicKey);

    /** @return the prefix url for all help topics */
    String getPrefix();
}
