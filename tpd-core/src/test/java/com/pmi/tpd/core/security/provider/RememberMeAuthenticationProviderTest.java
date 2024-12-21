package com.pmi.tpd.core.security.provider;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.spring.RememberMeUserAuthenticationToken;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests {@link RememberMeAuthenticationProvider}.
 *
 * @author Ben Alex
 */
public class RememberMeAuthenticationProviderTest extends MockitoTestCase {

    // ~ Methods
    // ========================================================================================================
    @Test
    public void testDetectsAnInvalidKey() throws Exception {
        final RememberMeAuthenticationProvider aap = new RememberMeAuthenticationProvider("qwerty");

        final IUser user = mock(IUser.class);
        final UserDetails userDetails = mock(UserDetails.class);

        final RememberMeUserAuthenticationToken token = RememberMeUserAuthenticationToken.forUser(user,
            userDetails,
            "WRONG_KEY");

        try {
            aap.authenticate(token);
            fail("Should have thrown BadCredentialsException");
        } catch (final BadCredentialsException expected) {
        }
    }

    @Test
    public void testDetectsMissingKey() throws Exception {
        try {
            new RememberMeAuthenticationProvider(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {

        }
    }

    @Test
    public void testGettersSetters() throws Exception {
        final RememberMeAuthenticationProvider aap = new RememberMeAuthenticationProvider("qwerty");
        aap.afterPropertiesSet();
        assertThat(aap.getKey(), equalTo("qwerty"));
    }

    @Test
    public void testIgnoresClassesItDoesNotSupport() throws Exception {
        final RememberMeAuthenticationProvider aap = new RememberMeAuthenticationProvider("qwerty");

        final TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password", "ROLE_A");
        assertThat(aap.supports(TestingAuthenticationToken.class), equalTo(false));

        // Try it anyway
        assertThat(aap.authenticate(token), nullValue());
    }

    @Test
    public void testNormalOperation() throws Exception {
        final RememberMeAuthenticationProvider aap = new RememberMeAuthenticationProvider("qwerty");

        final IUser user = mock(IUser.class);
        final UserDetails userDetails = mock(UserDetails.class);

        final RememberMeUserAuthenticationToken token = RememberMeUserAuthenticationToken.forUser(user,
            userDetails,
            "qwerty");

        final Authentication result = aap.authenticate(token);

        assertThat(token, equalTo(result));
    }

    @Test
    public void testSupports() {
        final RememberMeAuthenticationProvider aap = new RememberMeAuthenticationProvider("qwerty");
        assertThat(aap.supports(RememberMeUserAuthenticationToken.class), equalTo(true));
        assertThat(aap.supports(TestingAuthenticationToken.class), equalTo(false));
    }
}
