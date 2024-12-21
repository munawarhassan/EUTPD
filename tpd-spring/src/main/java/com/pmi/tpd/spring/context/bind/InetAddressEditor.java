package com.pmi.tpd.spring.context.bind;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * {@link PropertyEditor} for {@link InetAddress} objects.
 *
 * @author Dave Syer
 */
public class InetAddressEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        return ((InetAddress) getValue()).getHostAddress();
    }

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        try {
            setValue(InetAddress.getByName(text));
        } catch (final UnknownHostException ex) {
            throw new IllegalArgumentException("Cannot locate host", ex);
        }
    }

}
