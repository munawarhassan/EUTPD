package com.pmi.tpd.core.startup;

/**
 * Represents an individual check to be done on startup.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public interface IStartupCheck {

    /**
     * <p>
     * getOrder.
     * </p>
     *
     * @return a int.
     */
    int getOrder();

    /**
     * Implement this method to return the name of this check.
     *
     * @return name
     */
    String getName();

    /**
     * Implement this method to return true if the check is positive and false in the case of negative result.
     *
     * @return true if positive
     */
    boolean isOk();

    /**
     * Implement this method to return the description of the fault. This method should return null in the case the
     * check was positive. This message is used to present the user with a message to the console
     *
     * @return fault description
     */
    String getFaultDescription();

    /**
     * Implement this method to return the error message of the fault. This method should return null in the case the
     * check was positive. This message is used to present the user with a message viewable in a web browser.
     *
     * @return HTML formatted fault description
     */
    String getHtmlFaultDescription();

}
