package com.pmi.tpd.web.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class InetAddressUtil {

    /** */
    private static final String IP_ADDRESS_REGEX = "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";

    /** */
    private static final String PRIVATE_IP_ADDRESS_REGEX = "(^127\\.0\\.0\\.1)|(^10\\.)|"
            + "(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)";

    /** */
    private static Pattern ip_address_pattern = null;

    /** */
    private static Pattern private_ip_address_pattern = null;

    private InetAddressUtil() {
        throw new UnsupportedOperationException("Private class");
    }

    private static String findNonPrivateIpAddress(final String s) {
        if (ip_address_pattern == null) {
            ip_address_pattern = Pattern.compile(IP_ADDRESS_REGEX);
            private_ip_address_pattern = Pattern.compile(PRIVATE_IP_ADDRESS_REGEX);
        }
        final Matcher matcher = ip_address_pattern.matcher(s);
        while (matcher.find()) {
            if (!private_ip_address_pattern.matcher(matcher.group(0)).find()) {
                return matcher.group(0);
            }
            matcher.region(matcher.end(), s.length());
        }
        return null;
    }

    /**
     * @param request
     * @return
     */
    public static String getAddressFromRequest(final HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null) {
            forwardedFor = findNonPrivateIpAddress(forwardedFor);
            if (forwardedFor != null) {
                return forwardedFor;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * @param request
     * @return
     */
    public static String getHostnameFromRequest(final HttpServletRequest request) {
        final String addr = getAddressFromRequest(request);
        try {
            return Inet4Address.getByName(addr).getHostName();
        } catch (final Exception e) {
        }
        return addr;
    }

    /**
     * @param request
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress getInet4AddressFromRequest(final HttpServletRequest request) throws UnknownHostException {
        return Inet4Address.getByName(getAddressFromRequest(request));
    }
}
