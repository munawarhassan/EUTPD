/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.core.mail;

import javax.annotation.Nonnull;

import org.springframework.mail.javamail.JavaMailSender;

import com.pmi.tpd.api.exception.MailException;

/**
 * @author Christophe Friederich
 */
public interface IMailService {

    /**
     * @return {@code true} if the mail host is configured.
     */
    boolean isHostConfigured();

    /**
     * @return
     */
    JavaMailSender getJavaMailSender();

    /**
     * Sends an email in the calling thread. Gives synchronous feedback if the sending failed.
     *
     * @param message
     *            the message to send
     * @throws MailException
     *             if there was an error sending the mail.
     */
    void sendNow(MailMessage message) throws MailException;

    /**
     * Sends a test email using the supplied mail host configuration. Useful for verifying a configuration is valid
     * before saving it.
     *
     * @param mailProperties
     *            the mail host configuration.
     * @param from
     *            address email from.
     * @param to
     *            address email to.
     */
    void sendTest(@Nonnull final MailProperties mailProperties, @Nonnull final String from, @Nonnull final String to);

}
