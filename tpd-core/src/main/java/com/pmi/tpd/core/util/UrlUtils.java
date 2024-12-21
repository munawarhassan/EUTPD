package com.pmi.tpd.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.core.avatar.INavBuilder;

/**
 * Utilities to handle URL and URI.
 */
public class UrlUtils {

    private static final Pattern REDUNDANT_SLASHES = Pattern.compile("//+");

    private static final Map<String, Integer> DEFAULT_PORTS = ImmutableMap.<String, Integer> builder()
            .put("http", 80)
            .put("https", 443)
            .build();

    private UrlUtils() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * Encode a part of a URL to {@code application/x-www-form-urlencoded} format (percent escape).
     *
     * @param fragment
     *            the URL fragment to encode
     * @return the encoded URL fragment
     */
    public static String encodeURL(final String fragment) {
        try {
            return URLEncoder.encode(fragment, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param fragment
     *            the URL fragment that was encoded according to {@link #encodeURL}
     * @return the decoded URL fragment
     */
    public static String decodeURL(final String fragment) {
        try {
            return URLDecoder.decode(fragment, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param uri
     *            a valid URI
     * @throws IllegalArgumentException
     *             if the supplied String is not a valid URI
     * @return an {@link URI} initialised with the supplied String
     */
    public static URI uncheckedCreateURI(@Nullable final String uri) {
        if (uri != null) {
            try {
                return new URI(uri);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(uri + " is not a valid URI.");
            }
        }

        return null;
    }

    public static String concatenate(final String base, final String... paths) {
        return StringUtils.stripEnd(base, "/") + removeRedundantSlashes("/" + StringUtils.join(paths, "/"));
    }

    public static URI concatenate(final URI base, final String... paths) throws URISyntaxException {
        return new URI(concatenate(base.toASCIIString(), paths));
    }

    /**
     * @param base
     *            the base URI that will form the start of the URL
     * @param paths
     *            one or more paths that will form the end of the URL
     * @throws IllegalArgumentException
     *             if the supplied base and path segments do not form a valid URI when concatenated
     * @return a URL formed by concatenating the supplied base and paths
     */
    public static URI uncheckedConcatenate(final URI base, final URI... paths) {
        try {
            final String[] pathStrings = Iterables
                    .toArray(Lists.transform(Lists.newArrayList(paths), from -> from.toASCIIString()), String.class);
            return concatenate(base, pathStrings);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Failed to concatenate URIs", e);
        }
    }

    /**
     * @param base
     *            the base URI that will form the start of the URL
     * @param paths
     *            one or more paths that will form the end of the URL
     * @throws IllegalArgumentException
     *             if the supplied base and path segments do not form a valid URI when concatenated
     * @return a URL formed by concatenating the supplied base and paths
     */
    public static URI uncheckedConcatenateAndToUri(final String base, final String... paths) {
        return uncheckedCreateURI(concatenate(base, paths));
    }

    /**
     * @param base
     *            the base URI that will form the start of the URL
     * @param paths
     *            one or more paths that will form the end of the URL
     * @throws IllegalArgumentException
     *             if the supplied base and path segments do not form a valid URI when concatenated
     * @return a URL formed by concatenating the supplied base and paths
     */
    public static URI uncheckedConcatenate(final URI base, final String... paths) {
        try {
            return concatenate(base, paths);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to concatenate %s to form URI (%s)", base, e.getReason()), e);
        }
    }

    /**
     * <p>
     * Reduces sequences of more than one consecutive forward slash ("/") to a single slash (see:
     * https://studio.atlassian.com/browse/PLUG-597).
     * </p>
     *
     * @param path
     *            any string, including {@code null} (e.g. {@code "foo//bar"})
     * @return the input string, with all sequences of more than one consecutive slash removed (e.g. {@code "foo/bar"})
     */
    public static String removeRedundantSlashes(final String path) {
        return path == null ? null : REDUNDANT_SLASHES.matcher(path).replaceAll("/");
    }

    /**
     * @param uri
     *            a URI
     * @return a copy of the supplied URI
     */
    public static URI copyOf(final URI uri) {
        if (uri == null) {
            return null;
        }

        try {
            return new URI(uri.toASCIIString());
        } catch (final URISyntaxException e) {
            // this should never happen, but is there a better way to copy URIs?
            throw new IllegalArgumentException("Failed to copy URI: " + uri.toASCIIString());
        }
    }

    /**
     * Trims any trailing slashes from a URL path (see {@link URI#getPath()}.
     *
     * @param uri
     *            a URI with a path component
     * @return the URI with any trailing slashes from the path removed
     * @throws IllegalArgumentException
     *             if the value is no longer a valid URI after the slashes have been trimmed
     */
    public static URI trimTrailingSlashesFromPath(URI uri) {
        try {
            String urlPath = uri.getPath();
            if (urlPath.endsWith("/")) {
                while (urlPath.endsWith("/")) {
                    urlPath = urlPath.substring(0, urlPath.length() - 1);
                }
                uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), urlPath, uri.getQuery(),
                        uri.getFragment());
            }
            return uri;
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Trimming slashes from path of " + uri + " resulted in invalid URI", e);
        }
    }

    /**
     * An unfortunate method which is designed to replace the request base url with the configured base url. Used in
     * situations such as email rendering when {@link NavBuilder#buildConfigured()} cannot be used
     *
     * @param navBuilder
     *            the nav builder
     * @param absoluteUrl
     *            the url which requires the baseUrl to be replaced
     * @return the adjusted base url
     */
    public static String replaceBaseUrlWithConfigured(final INavBuilder navBuilder, final String absoluteUrl) {
        final String requestBaseUrl = navBuilder.buildAbsolute();
        final String configuredBaseUrl = navBuilder.buildConfigured();
        return configuredBaseUrl + absoluteUrl.substring(requestBaseUrl.length());
    }

    /**
     * Add a userInfo component to a URL as per <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396 3.2.2</a>.
     *
     * @param url
     *            a valid URL
     * @param username
     *            the userInfo be added to the URL
     * @return the URL with the supplied userInfo component interpolated, or the supplied URL if the supplied user is
     *         null
     * @throws URISyntaxException
     *             if the supplied URL is not a valid URL
     */
    @Nullable
    public static String interpolateUserInfo(@Nullable final String url, @Nullable final String username)
            throws URISyntaxException {
        if (url == null) {
            return null;
        }

        if (username == null) {
            return url;
        }

        final URI uri = new URI(url);
        return new URI(uri.getScheme(), username, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                uri.getFragment()).toASCIIString();
    }

    @Nonnull
    public static String buildQueryParams(final Object... params) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            final Object paramName = params[i];
            final Object paramValue = params[i + 1];
            if (paramName != null && paramValue != null) {
                sb.append(paramName).append('=').append(encodeURL(String.valueOf(paramValue)));
                if (i + 2 < params.length) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    /**
     * @since 4.1
     */
    @Nonnull
    public static String buildQueryParamsFromMap(final Map<?, List<Object>> params) {
        final StringBuilder sb = new StringBuilder();
        boolean separatorNeeded = false;
        for (final Map.Entry<?, List<Object>> entry : params.entrySet()) {
            final Object paramName = entry.getKey();
            if (paramName != null) {
                final List<Object> paramList = entry.getValue();
                if (paramList.isEmpty()) {
                    sb.append(separatorNeeded ? "&" : "").append(paramName);
                    separatorNeeded = true;
                }
                for (final Object paramValue : paramList) {
                    if (paramValue != null) {
                        sb.append(separatorNeeded ? "&" : "")
                                .append(paramName)
                                .append('=')
                                .append(encodeURL(String.valueOf(paramValue)));
                        separatorNeeded = true;
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the canonical version of a URL.
     *
     * @param url
     *            a valid URL
     * @return the canonical version of the given URL
     */
    public static String getCanonicalUrl(final String url) {
        final URI uri = URI.create(url);

        return getCanonicalUri(uri).toString();
    }

    /**
     * Returns the canonical version of a URI.
     *
     * @param uri
     *            a valid URL
     * @return the canonical version of the given URI
     */
    public static URI getCanonicalUri(final URI uri) {
        final StringBuilder builder = new StringBuilder().append(StringUtils.lowerCase(uri.getScheme()))
                .append("://")
                .append(uri.getHost());

        // only include a port if it's not the default port
        if (hasCustomPort(uri)) {
            builder.append(":").append(uri.getPort());
        }

        builder.append(uri.getPath());
        return URI.create(builder.toString());
    }

    /**
     * Returns the path and query from a supplied URI
     *
     * @param uri
     *            The uri from which to extract the path and query
     * @return <code>String</code> the extracted path and query
     */
    public static String getPathAndQuery(final String uri) {
        String path = "/";
        try {
            final URI url = new URI(uri);
            final String p = url.getPath();
            final String q = url.getQuery();
            if (p.length() > 0) {
                path = p;
            }
            if (!path.startsWith("/") && path.contains("/")) {
                path = path.substring(path.indexOf('/'));
            }
            if (p.length() > 0 && q != null && q.length() > 0) {
                path += "?" + q;
            }
            if (!url.isAbsolute()) {
                if (p.length() == 0 && (q == null || q.length() == 0)) {
                    path = uri;
                }
            }
        } catch (final URISyntaxException e) {
            // Ignored
        }
        return path;
    }

    @VisibleForTesting
    protected static boolean isLocalPath(final URI uri) {
        // note: the path is allowed to have a non-null query (uri.getQuery())
        return StringUtils.isNotEmpty(uri.getPath()) && uri.getAuthority() == null && uri.getHost() == null
                && uri.getPort() == -1 && uri.getScheme() == null;
    }

    private static boolean hasCustomPort(final URI uri) {
        if (uri.getPort() < 0) {
            // no port provided
            return false;
        }

        final Integer defaultPort = DEFAULT_PORTS.get(StringUtils.lowerCase(uri.getScheme(), Locale.ROOT));
        return defaultPort == null || defaultPort != uri.getPort();
    }
}
