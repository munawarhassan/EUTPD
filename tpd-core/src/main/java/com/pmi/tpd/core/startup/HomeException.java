package com.pmi.tpd.core.startup;

/**
 * Thrown when errors occur trying to validate the home path.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public class HomeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8470094106188614973L;

    /** */
    private final String htmlText;

    /**
     * <p>
     * Constructor for HomeException.
     * </p>
     *
     * @param message
     *            a {@link java.lang.String} object.
     */
    public HomeException(final String message) {
        super(message);
        htmlText = message;
    }

    /**
     * <p>
     * Constructor for HomeException.
     * </p>
     *
     * @param plainText
     *            a {@link java.lang.String} object.
     * @param htmlText
     *            a {@link java.lang.String} object.
     */
    public HomeException(final String plainText, final String htmlText) {
        super(plainText);
        this.htmlText = htmlText;
    }

    /**
     * <p>
     * getHtmlMessage.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHtmlMessage() {
        return this.htmlText;
    }
}
