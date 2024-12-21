package com.pmi.tpd.spring.convert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * {@link Formatter} for {@link InetAddress}.
 *
 * @author Phillip Webb
 */
final class InetAddressFormatter implements Formatter<InetAddress> {

    @Override
    public String print(final InetAddress object, final Locale locale) {
        return object.getHostAddress();
    }

    @Override
    public InetAddress parse(final String text, final Locale locale) throws ParseException {
        try {
            return InetAddress.getByName(text);
        } catch (final UnknownHostException ex) {
            throw new IllegalStateException("Unknown host " + text, ex);
        }
    }

}