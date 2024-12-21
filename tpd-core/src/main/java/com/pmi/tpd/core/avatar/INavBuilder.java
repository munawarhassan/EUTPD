package com.pmi.tpd.core.avatar;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.avatar.impl.NavBuilderImpl.NavBuilder;

public interface INavBuilder {

    /**
     * A key which is wrapped in {@link #TOKEN_PREFIX} and {@link #TOKEN_SUFFIX} to produce a base URL token.
     * <p>
     * The key used needs to contain (preferably end with) something which is escaped when URL-encoded. Doing so causes
     * the end token to be different depending on whether the base URL has been encoded and allows callers to encode the
     * value they replace it with. That, in turn, ensures the resulting full URL has correct encoding from beginning to
     * end.
     * <p>
     * Over and above containing something which requires encoding, the value needs to encode <i>reliably</i>. That
     * means if the token has been encoded 4 times, any caller performing the substitution should be able to detect that
     * count, because they will need to encode the replacement value 4 times over as well.
     * <p>
     * The current choice is a single trailing %. On first encoding, this produces "%25"; second produces "%2525", third
     * produces "%252525", etc. This allows for two use cases:
     * <ol>
     * <li><i>I don't care about encoding</i>: Use a regular expression which ignores any number of 25s after the
     * trailing % and before the closing {@link #TOKEN_SUFFIX}</li>
     * <li><i>I want to encode by depth</i>: Use the index of {@link #TOKEN_PREFIX} + this key to find the first 25, if
     * any are present, and the location of the first {@link #TOKEN_SUFFIX} to find the end. Use the number of 25s
     * present to determine the depth</li>
     * </ol>
     */
    String BASE_URL_KEY = "app.baseUrl%";

    /**
     * A prefix which is applied to the beginning of tokens which are emitted in built URLs.
     */
    String TOKEN_PREFIX = "$$$$";

    /**
     * A suffix which is appended to the ending of tokens which are emitted in built URLs.
     */
    String TOKEN_SUFFIX = "$$$$";

    /**
     * The fully-computed base URL token, comprised of {@link #TOKEN_PREFIX}, {@link #BASE_URL_KEY} and
     * {@link #TOKEN_SUFFIX}.
     */
    String BASE_URL_TOKEN = TOKEN_PREFIX + BASE_URL_KEY + TOKEN_SUFFIX;

    /**
     * The query parameter on the request which indicates the base URL on emitted URLs should be replaced with
     * {@link #BASE_URL_TOKEN} instead. Using this token allows callers to format in their own base URL for producing
     * links which are correct based on their context.
     * <p>
     * For example, to better explain the need for this, consider the following:
     * <ul>
     * <li>A JIRA instance is running with a public address of http://jira.example.com</li>
     * <li>A Stash instance is running with a public address of http://stash.example.com</li>
     * <li>These two machines, in addition to having public-facing URLs, have a private backplane network which connects
     * them as jira.private.example.com and stash.private.example.com</li>
     * <li>The application link from JIRA to Stash has an RPC URL of http://stash.private.example.com, so that RPC uses
     * the fast backplane network, and a display URL of http://stash.example.com so that end users get valid links</li>
     * <li>When requests come into Stash, if the base URL calculated by {@link #buildAbsolute()} uses the HTTP request
     * (which is the default behaviour), links are emitted pointing to http://stash.private.example.com. From an end
     * user's browser, however, such links will not work</li>
     * <li>Rather than trying to manually munge the URLs in the client, which is problematic and brittle, the caller can
     * add this parameter to their request and a token suitable for replacement will be emitted in the URLs instead of a
     * fixed base URL</li>
     * </ul>
     */
    String USE_BASE_URL_TOKEN = "useBaseUrlToken";

    /**
     * @return
     */
    boolean isSecure();

    /**
     * This method respects the {@link #USE_BASE_URL_TOKEN} parameter if present on the context request. Use
     * {@link #buildBaseUrl} if you with to avoid this behaviour. Note: if the {@link #USE_BASE_URL_TOKEN} is present
     * this method may return a {@link String} that is not a syntactically valid URI. The base URL (the scheme, host,
     * port etc and webapp context) will be taken from the context HTTP request, if one is present, or otherwise fall
     * back to the {@link ApplicationPropertiesService#getBaseUrl() configured base URL} for the server.
     *
     * @return the absolute base url (e.g. http://hostname:7990/context), with no trailing slash
     * @see #buildBaseUrl
     * @see #USE_BASE_URL_TOKEN
     * @see #buildRelative()
     */
    String buildAbsolute();

    /**
     * This method is equivalent to {@link #buildAbsolute()}, but ignores any context HTTP request and always uses the
     * server's {@link ApplicationPropertiesService#getBaseUrl() configured base URL} for generating the scheme, host,
     * port etc and webapp context components of the URL.
     *
     * @return the absolute base url {@link ApplicationPropertiesService#getBaseUrl() configured} for this server
     */
    String buildConfigured();

    /**
     * This method does not respect the {@link #USE_BASE_URL_TOKEN} parameter. Unless you have a specific need to avoid
     * the token substitution behaviour for this particular URL, you should use {@link #buildAbsolute()}.
     *
     * @return the absolute base url (e.g. http://hostname:7990/context), with no trailing slash
     * @see #buildAbsolute()
     * @see #USE_BASE_URL_TOKEN
     * @see #buildRelative()
     */
    String buildBaseUrl();

    /**
     * @return the relative base url (e.g. /context), with no trailing slash
     * @see #buildAbsolute()
     * @see #buildBaseUrl
     */
    String buildRelative();

    /** Base interface for all terminal builders (builders which can produce a url) */
    interface Builder<B extends Builder<?>> {

        /**
         * @return a builder with the supplied name and value parameters. Overwrites any previous parameter of the same
         *         name.
         */
        // TODO fix in 5.0. The proper return type of this method is B, as in: B withParam(...). BuilderImpl _should_
        // still compile after that change
        Builder<B> withParam(@Nonnull String name, String value);

        /** @return the url path without scheme, host, port etc e.g. /context/rest/of/url */
        String buildRelative();

        /** @return the url path without scheme, host, port etc or webapp context e.g. /rest/of/url */
        String buildRelNoContext();

        /**
         * @return the full url including scheme, host, port etc and webapp context e.g. http://localhost/rest/of/url
         *         all components up to and including the webapp context will be taken from the context HTTP request (if
         *         one is present) or otherwise taken the configured base URL of the server.
         **/
        String buildAbsolute();

        /**
         * @return the same as {@link #buildAbsolute()}, but will ignore any context HTTP request, and always use the
         *         server's configured base URL
         **/
        String buildConfigured();
    }

    /**
     * /users/SLUG or /bots/SLUG
     *
     * @return a builder for the url to the profile page of the supplied user
     */
    Profile user(IUser user);

    interface Profile extends Builder<Profile> {

        /**
         * @param size
         *             the size for the avatar, in pixels
         * @return a builder for constructing avatar URLs
         */
        Builder<?> avatar(int size);
    }

    Builder<NavBuilder> builder(String... components);

    Builder<NavBuilder> builder(List<String> components);
}
