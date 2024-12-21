package com.pmi.tpd.core.avatar.impl;

import static java.util.Arrays.asList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.cluster.concurrent.IStatefulService;
import com.pmi.tpd.cluster.concurrent.ITransferableState;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.web.core.request.event.RequestStartedEvent;

@Named("navBuilder")
public class NavBuilderImpl implements INavBuilder, IStatefulService {

    private static final Function<String, String> ENCODE_PATH = component -> UriUtils.encodePathSegment(component,
        "UTF-8");

    private static final String API = "api";

    @SuppressWarnings("unused")
    private static final String AVATARS = "avatars";

    private static final String AVATAR = "avatar";

    private static final String AVATAR_EXTENSION = ".png";

    private static final String REST = "rest";

    private static final String USERS = "users";

    private static final Logger log = LoggerFactory.getLogger(NavBuilderImpl.class);

    private final IApplicationProperties applicationProperties;

    private final ThreadLocal<RequestConfiguration> requestConfiguration;

    @Autowired
    public NavBuilderImpl(final IApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.requestConfiguration = new ThreadLocal<>();
    }

    @EventListener
    public void onRequestStarted(final RequestStartedEvent event) {
        if (event.isHttp()) {
            final HttpServletRequest request = (HttpServletRequest) event.getRequestContext().getRawRequest();
            URI uri;
            try {
                int port = request.getServerPort();
                if ("http".equals(request.getScheme()) && port == 80
                        || "https".equals(request.getScheme()) && port == 443) {
                    // default port for the scheme, don't make it explicit in the URL
                    port = -1;
                }
                uri = new URI(request.getScheme(), null, request.getServerName(), port, request.getContextPath(), null,
                        null);
            } catch (final URISyntaxException e) {
                log.error(
                    "Failed to construct a URI for the NavBuilder from the current requests. Defaulting to application properties.",
                    e);
                uri = getBaseUrl().orElse(null);
            }
            final String requestUrl = request.getRequestURI();
            final String queryString = request.getQueryString();
            requestConfiguration.set(new RequestConfiguration(uri,
                    "true".equals(request.getParameter(USE_BASE_URL_TOKEN)), requestUrl, queryString));
            event.getRequestContext().addCleanupCallback(() -> requestConfiguration.remove());
        }
    }

    /**
     * Returns the base URL from the {@link ApplicationPropertiesService}. Ensure the context class loader is set to the
     * host applications class loader when retrieving the app property.
     */
    @Nonnull
    private Optional<URI> getBaseUrl() {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader original = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(NavBuilderImpl.class.getClassLoader());
            return this.applicationProperties.getBaseUrl();
        } finally {
            currentThread.setContextClassLoader(original);
        }
    }

    private Optional<URI> buildBaseUri() {
        final RequestConfiguration config = requestConfiguration.get();
        return config != null ? Optional.ofNullable(config.getRequestBaseUri()) : getBaseUrl();
    }

    @Override
    public boolean isSecure() {
        return buildBaseUrl().startsWith("https");
    }

    @Override
    public String buildBaseUrl() {
        return buildBaseUri().orElseThrow().toASCIIString();
    }

    protected String buildAbsoluteUrl(final URI defaultBaseUrl) {
        final RequestConfiguration config = requestConfiguration.get();
        if (config != null && config.isBaseUriTokenRequested()) {
            return BASE_URL_TOKEN;
        }
        if (defaultBaseUrl == null) {
            return null;
        }
        return defaultBaseUrl.toASCIIString();
    }

    @Override
    public String buildAbsolute() {
        return buildAbsoluteUrl(buildBaseUri().orElseThrow());
    }

    @Override
    public String buildConfigured() {
        final var baseUrl = getBaseUrl();
        // BaseUrl can be null during installation (used by nav-links)
        return buildAbsoluteUrl(buildBaseUri().orElseThrow());
    }

    @Override
    public String buildRelative() {
        return buildBaseUri().orElseThrow().getRawPath();
    }

    @Nonnull
    @Override
    public ITransferableState getState() {
        return new ITransferableState() {

            private final RequestConfiguration configuration = requestConfiguration.get();

            @Override
            public void apply() {
                requestConfiguration.set(configuration);
            }

            @Override
            public void remove() {
                requestConfiguration.remove();
            }
        };
    }

    private List<String> append(final List<String> components, final String... others) {
        return ImmutableList.<String> builder().addAll(components).add(others).build();
    }

    @SuppressWarnings("unused")
    private List<String> components(final String... others) {
        return asList(others);
    }

    private List<String> restComponents(final String... others) {
        return append(asList(REST, API), others);
    }

    abstract class AbstractBuilder<B extends Builder<B>> implements Builder<B> {

        protected Map<String, String> params;

        protected String anchor;

        protected AbstractBuilder() {
            this(Collections.<String, String> emptyMap());
        }

        protected AbstractBuilder(final Map<String, String> params) {
            this.params = params;
        }

        @Override
        public Builder<B> withParam(@Nonnull final String name, final String value) {
            // copy, so same builder can be used to define multiple links under some common path
            params = Maps.newLinkedHashMap(params);
            params.put(name, value);
            return self();
        }

        public B withAnchor(@Nonnull final String anchor) {
            this.anchor = anchor;
            return self();
        }

        @Override
        public String buildRelative() {
            return NavBuilderImpl.this.buildRelative() + buildRelNoContext();
        }

        @Override
        public String buildAbsolute() {
            return NavBuilderImpl.this.buildAbsolute() + buildRelNoContext();
        }

        @Override
        public String buildConfigured() {
            return NavBuilderImpl.this.buildConfigured() + buildRelNoContext();
        }

        @Override
        public String buildRelNoContext() {
            String path = StringUtils.join(buildPath(), '/');
            path = path.isEmpty() ? path : "/" + path;
            final String queryString = buildQueryString();
            return path + (StringUtils.isNotEmpty(queryString) ? '?' + queryString : "")
                    + (StringUtils.isNotEmpty(anchor)
                            ? '#' + UriUtils.encodeFragment(anchor, "UTF-8").replace("%23", "#") : "");
        }

        public abstract List<String> buildPath();

        protected String buildQueryString() {
            final StringBuilder query = new StringBuilder("");
            boolean firstParam = true;
            for (final Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().length() > 0) {
                    if (!firstParam) {
                        query.append("&");
                    } else {
                        firstParam = false;
                    }
                    query.append(UriUtils.encodeQuery(entry.getKey(), "UTF-8"));
                    if (entry.getValue() != null && entry.getValue().length() > 0) {
                        query.append("=");
                        query.append(UriUtils.encodeQueryParam(entry.getValue(), "UTF-8"));
                    }
                }
            }

            return query.length() == 0 ? null : query.toString();
        }

        protected List<String> encode(final List<String> components) {
            return Lists.transform(components, ENCODE_PATH);
        }

        protected List<String> encode(final String... components) {
            return encode(Arrays.asList(components));
        }

        protected List<String> parentComponents(final AbstractBuilder<?> parent, final String... others) {
            return append(parent.buildPath(), others);
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }
    }

    public class NavBuilder extends AbstractBuilder<NavBuilder> {

        private final List<String> components;

        public NavBuilder(final String... components) {
            this(Arrays.asList(components == null ? new String[0] : components));
        }

        public NavBuilder(final List<String> components) {

            this.components = components;
        }

        public NavBuilder append(final String component) {
            components.add(component);
            return this;
        }

        @Override
        public List<String> buildPath() {
            return encode(components);
        }
    }

    @Override
    public Builder<NavBuilder> builder(final String... components) {
        return new NavBuilder(components);
    }

    @Override
    public Builder<NavBuilder> builder(final List<String> components) {
        return new NavBuilder(components);
    }

    @Override
    public Profile user(final IUser user) {
        return new UserProfileImpl(user.getSlug());
    }

    class UserProfileImpl extends AbstractBuilder<Profile> implements Profile {

        private final String slug;

        UserProfileImpl(final String slug) {
            this.slug = slug;
        }

        @Override
        public Builder<?> avatar(final int size) {
            return new NavBuilder(restComponents(USERS, slug, AVATAR + AVATAR_EXTENSION)).withParam("s",
                String.valueOf(size));
        }

        @Override
        public List<String> buildPath() {
            return encode(restComponents(USERS, slug));
        }

    }

    private static class RequestConfiguration {

        private final URI requestBaseUri;

        private final boolean baseUriTokenRequested;

        private final String requestUrl;

        private final String queryString;

        public RequestConfiguration(final URI requestBaseUri, final boolean baseUriTokenRequested,
                final String requestUrl, final String queryString) {
            this.baseUriTokenRequested = baseUriTokenRequested;
            this.requestBaseUri = requestBaseUri;
            this.requestUrl = requestUrl;
            this.queryString = queryString;
        }

        public URI getRequestBaseUri() {
            return requestBaseUri;
        }

        public boolean isBaseUriTokenRequested() {
            return baseUriTokenRequested;
        }

        @SuppressWarnings("unused")
        public String getRequestUrl() {
            return requestUrl;
        }

        @SuppressWarnings("unused")
        public String getQueryString() {
            return queryString;
        }
    }
}
