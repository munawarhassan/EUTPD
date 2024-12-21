package com.pmi.tpd.web.security.jwt;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static org.joda.time.Duration.standardSeconds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * @author devacfr
 * @since 1.0
 */
public class JwtTokenProvider {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** */
    private static final String AUTHORITIES_KEY = "auth";

    /** */
    private static final String IDENTIFIER_KEY = "id";

    /** */
    private String secretKey;

    /** */
    private long tokenValidityInMilliseconds;

    /** */
    private long tokenValidityInMillisecondsForRememberMe;

    /** */
    private final JwtConfiguration config;

    /**
     * @param userService
     * @param config
     */
    @Inject
    public JwtTokenProvider(@Nonnull final JwtConfiguration config) {
        this.config = checkNotNull(config, "config");
    }

    /**
     *
     */
    @PostConstruct
    public void init() {
        this.secretKey = config.getSecret();

        this.tokenValidityInMilliseconds = standardSeconds(config.getTokenValidityInSeconds()).getMillis();
        this.tokenValidityInMillisecondsForRememberMe = standardSeconds(config.getTokenValidityInSecondsForRememberMe())
                .getMillis();
    }

    /**
     * @param authentication
     * @param rememberMe
     * @return
     */
    public String createToken(final Authentication authentication, final Boolean rememberMe) {
        final String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        final long now = new Date().getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }
        final UserAuthenticationToken auth = authentication instanceof UserAuthenticationToken
                ? (UserAuthenticationToken) authentication : null;
        Long id = null;
        UserDirectory directory = null;
        if (auth != null) {
            final IUser user = auth.getPrincipal();
            id = user.getId();
            directory = user.getDirectory();
            if (directory == null) {
                directory = UserDirectory.Internal;
            }
        }

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .claim(IDENTIFIER_KEY, id)
                .claim("dir", directory)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .setExpiration(validity)
                .compact();
    }

    /**
     * @param token
     * @return
     */
    public Authentication getAuthentication(final String token, HttpServletRequest request) {
        final Claims claims = createClaim(token).getBody();

        final String auths = claims.get(AUTHORITIES_KEY, String.class);
        final Collection<? extends GrantedAuthority> authorities = Strings.isNullOrEmpty(auths)
                ? Collections.emptyList()
                : Arrays.stream(auths.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        final User principal = new User(claims.getSubject(), "", authorities);
        final String dir = claims.get("dir", String.class);
        final IUser user = com.pmi.tpd.api.user.User.builder()
                .activated(true)
                .directory(dir != null ? UserDirectory.valueOf(dir) : null)
                .id(claims.get("id", Integer.class).longValue())
                .password("")
                .username(claims.getSubject())
                .slug(claims.getSubject())
                .build();
        UserAuthenticationToken authentication = UserAuthenticationToken.forUser(user, principal);
        if (request != null) {
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        return authentication;
        /*
         * return new UsernamePasswordAuthenticationToken(principal, token, authorities);
         */
    }

    /**
     * @param authToken
     * @return
     */
    public boolean validateToken(final String authToken) {
        try {
            createClaim(authToken);
            return true;
        } catch (final SignatureException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Invalid JWT signature.");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Invalid JWT signature trace: {}", e);
            }
        } catch (final MalformedJwtException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Invalid JWT token.");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Invalid JWT token trace: {}", e);
            }
        } catch (final ExpiredJwtException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Expired JWT token.");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Expired JWT token trace: {}", e);
            }
        } catch (final UnsupportedJwtException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unsupported JWT token.");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Unsupported JWT token trace: {}", e);
            }
        } catch (final IllegalArgumentException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("JWT token compact of handler are invalid.");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("JWT token compact of handler are invalid trace: {}", e);
            }
        }
        return false;
    }

    private Jws<Claims> createClaim(final String authToken) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(authToken);
    }
}
