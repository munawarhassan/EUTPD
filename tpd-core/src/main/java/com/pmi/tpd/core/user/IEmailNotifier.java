package com.pmi.tpd.core.user;

import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.user.UserRequest;

/**
 * Encapsulates functionality for sending e-mails to users to notify them of changes related to their account.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IEmailNotifier {

    /**
     * Sends the specified user an e-mail containing a link to allow them to activate their new account and set their
     * password, rather than requiring the administrator creating the account to set a password for them.
     *
     * @param user
     *            the user to send the e-mail to, used to derive name and e-mail address details
     * @param token
     *            the token to use in the account activation URL
     * @throws MailException
     *             if an e-mail server has been configured but the e-mail could not be sent
     */
    void sendCreatedUser(UserRequest user, String token) throws MailException;

    /**
     * Sends the specified user an e-mail containing a password reset link, which can be used one time to allow them to
     * change their current password without knowing what it currently is.
     *
     * @param user
     *            the user to send the e-mail to, used to derive name and e-mail address details
     * @param token
     *            the token to use in the password reset URL
     * @throws MailException
     *             if an e-mail server has been configured but the e-mail could not be sent
     * @throws NoMailHostConfigurationException
     *             if an e-mail server has not been configured
     */
    void sendPasswordReset(UserRequest user, String token) throws MailException;

    /**
     * Validates an e-mail server has been configured, allowing e-mails to be sent. If the method returns without
     * throwing an exception, it means the configuration is in place.
     *
     * @throws NoMailHostConfigurationException
     *             if an e-mail server has not been configured
     */
    void validateCanSendEmails() throws NoMailHostConfigurationException;
}
