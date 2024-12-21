package com.pmi.tpd.core.security.provider.ldap;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.hamcrest.collection.ArrayMatching;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.pmi.tpd.core.security.provider.ldap.ActiveDirectoryLdapAuthenticationProvider.ContextFactory;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ActiveDirectoryLdapAuthenticationProviderTest extends MockitoTestCase {

  ActiveDirectoryLdapAuthenticationProvider provider;

  UsernamePasswordAuthenticationToken joe = new UsernamePasswordAuthenticationToken("joe", "password");

  @BeforeEach
  public void setUp() throws Exception {
    provider = new ActiveDirectoryLdapAuthenticationProvider("mydomain.eu", "ldap://192.168.1.200/");
  }

  @Test
  public void bindPrincipalIsCreatedCorrectly() throws Exception {
    assertThat(provider.createBindPrincipal("joe"), equalTo("joe@mydomain.eu"));
    assertThat(provider.createBindPrincipal("joe@mydomain.eu"), equalTo("joe@mydomain.eu"));
  }

  @Test
  public void successfulAuthenticationProducesExpectedAuthorities() throws Exception {
    checkAuthentication("dc=mydomain,dc=eu", provider);
  }

  @Test
  public void customSearchFilterIsUsedForSuccessfulAuthentication() throws Exception {
    // given
    final String customSearchFilter = "(&(objectClass=user)(sAMAccountName={0}))";

    final DirContext ctx = mock(DirContext.class);
    when(ctx.getNameInNamespace()).thenReturn("");

    final DirContextAdapter dca = new DirContextAdapter();
    final SearchResult sr = new SearchResult("CN=Joe Jannsen,CN=Users", dca, dca.getAttributes());
    when(ctx.search(any(Name.class), eq(customSearchFilter), any(Object[].class), any(SearchControls.class)))
        .thenReturn(new MockNamingEnumeration(sr));

    final ActiveDirectoryLdapAuthenticationProvider customProvider = new ActiveDirectoryLdapAuthenticationProvider(
        "mydomain.eu", "ldap://192.168.1.200/");
    customProvider.contextFactory = createContextFactoryReturning(ctx);

    // when
    customProvider.setSearchFilter(customSearchFilter);
    final Authentication result = customProvider.authenticate(joe);

    // then
    assertThat(result.isAuthenticated(), Is.is(true));
  }

  @Test
  public void defaultSearchFilter() throws Exception {
    // given
    final String defaultSearchFilter = "(&(objectClass=user)(userPrincipalName={0}))";

    final DirContext ctx = mock(DirContext.class);
    when(ctx.getNameInNamespace()).thenReturn("");

    final DirContextAdapter dca = new DirContextAdapter();
    final SearchResult sr = new SearchResult("CN=Joe Jannsen,CN=Users", dca, dca.getAttributes());
    when(ctx.search(any(Name.class), eq(defaultSearchFilter), any(Object[].class), any(SearchControls.class)))
        .thenReturn(new MockNamingEnumeration(sr));

    final ActiveDirectoryLdapAuthenticationProvider customProvider = new ActiveDirectoryLdapAuthenticationProvider(
        "mydomain.eu", "ldap://192.168.1.200/");
    customProvider.contextFactory = createContextFactoryReturning(ctx);

    // when
    final Authentication result = customProvider.authenticate(joe);

    // then
    assertThat(result.isAuthenticated(), is(true));
    verify(ctx).search(any(DistinguishedName.class),
        eq(defaultSearchFilter),
        any(Object[].class),
        any(SearchControls.class));
  }

  @Test
  public void bindPrincipalAndUsernameUsed() throws Exception {
    // given
    final String defaultSearchFilter = "(&(objectClass=user)(userPrincipalName={0}))";
    final ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

    final DirContext ctx = mock(DirContext.class);
    when(ctx.getNameInNamespace()).thenReturn("");

    final DirContextAdapter dca = new DirContextAdapter();
    final SearchResult sr = new SearchResult("CN=Joe Jannsen,CN=Users", dca, dca.getAttributes());
    when(ctx.search(any(Name.class), eq(defaultSearchFilter), captor.capture(), any(SearchControls.class)))
        .thenReturn(new MockNamingEnumeration(sr));

    final ActiveDirectoryLdapAuthenticationProvider customProvider = new ActiveDirectoryLdapAuthenticationProvider(
        "mydomain.eu", "ldap://192.168.1.200/");
    customProvider.contextFactory = createContextFactoryReturning(ctx);

    // when
    final Authentication result = customProvider.authenticate(joe);

    // then
    assertThat(captor.getValue(), ArrayMatching.arrayContaining("joe@mydomain.eu", "joe"));
    assertThat(result.isAuthenticated(), is(true));
  }

  @Test
  public void setSearchFilterNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      provider.setSearchFilter(null);
    });
  }

  @Test
  public void setSearchFilterEmpty() {
    assertThrows(IllegalArgumentException.class, () -> {
      provider.setSearchFilter(" ");
    });
  }

  @Test
  public void nullDomainIsSupportedIfAuthenticatingWithFullUserPrincipal() throws Exception {
    provider = new ActiveDirectoryLdapAuthenticationProvider(null, "ldap://192.168.1.200/");
    final DirContext ctx = mock(DirContext.class);
    when(ctx.getNameInNamespace()).thenReturn("");

    final DirContextAdapter dca = new DirContextAdapter();
    final SearchResult sr = new SearchResult("CN=Joe Jannsen,CN=Users", dca, dca.getAttributes());
    when(ctx.search(eq(new DistinguishedName("DC=mydomain,DC=eu")),
        any(String.class),
        any(Object[].class),
        any(SearchControls.class))).thenReturn(new MockNamingEnumeration(sr));
    provider.contextFactory = createContextFactoryReturning(ctx);

    try {
      provider.authenticate(joe);
      fail("Expected BadCredentialsException for user with no domain information");
    } catch (final BadCredentialsException expected) {
    }

    provider.authenticate(new UsernamePasswordAuthenticationToken("joe@mydomain.eu", "password"));
  }

  @Test
  public void failedUserSearchCausesBadCredentials() throws Exception {
    assertThrows(BadCredentialsException.class, () -> {
      final DirContext ctx = mock(DirContext.class);
      when(ctx.getNameInNamespace()).thenReturn("");
      when(ctx.search(any(Name.class), any(String.class), any(Object[].class), any(SearchControls.class)))
          .thenThrow(new NameNotFoundException());

      provider.contextFactory = createContextFactoryReturning(ctx);

      provider.authenticate(joe);
    });
  }

  @Test
  public void noUserSearchCausesUsernameNotFound() throws Exception {
    assertThrows(BadCredentialsException.class, () -> {
      final DirContext ctx = mock(DirContext.class);
      when(ctx.getNameInNamespace()).thenReturn("");
      when(ctx.search(any(Name.class), any(String.class), any(Object[].class), any(SearchControls.class)))
          .thenReturn(new EmptyNamingEnumeration<SearchResult>());

      provider.contextFactory = createContextFactoryReturning(ctx);

      provider.authenticate(joe);
    });
  }

  @Test
  public void sec2500PreventAnonymousBind() {
    assertThrows(BadCredentialsException.class, () -> {
      provider.authenticate(new UsernamePasswordAuthenticationToken("rwinch", ""));
    });

  }

  @SuppressWarnings("unchecked")
  @Test
  public void duplicateUserSearchCausesError() throws Exception {
    assertThrows(IncorrectResultSizeDataAccessException.class, () -> {
      final DirContext ctx = mock(DirContext.class);
      when(ctx.getNameInNamespace()).thenReturn("");
      final NamingEnumeration<SearchResult> searchResults = mock(NamingEnumeration.class);
      when(searchResults.hasMore()).thenReturn(true, true, false);
      final SearchResult searchResult = mock(SearchResult.class);
      when(searchResult.getObject()).thenReturn(new DirContextAdapter("ou=1"), new DirContextAdapter("ou=2"));
      when(searchResults.next()).thenReturn(searchResult);
      when(ctx.search(any(Name.class), any(String.class), any(Object[].class), any(SearchControls.class)))
          .thenReturn(searchResults);

      provider.contextFactory = createContextFactoryReturning(ctx);

      provider.authenticate(joe);
    });
  }

  static final String msg = "[LDAP: error code 49 - 80858585: LdapErr: DSID-DECAFF0, comment: AcceptSecurityContext error, data ";

  @Test
  public void userNotFoundIsCorrectlyMapped() {
    assertThrows(BadCredentialsException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "525, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void incorrectPasswordIsCorrectlyMapped() {
    assertThrows(BadCredentialsException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "52e, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void notPermittedIsCorrectlyMapped() {
    assertThrows(BadCredentialsException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "530, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void passwordNeedsResetIsCorrectlyMapped() {
    final String dataCode = "773";
    provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + dataCode + ", xxxx]"));
    provider.setConvertSubErrorCodesToExceptions(true);

    assertThrows(BadCredentialsException.class, () -> {
      provider.authenticate(joe);
    });
  }

  @Test
  public void expiredPasswordIsCorrectlyMapped() {
    assertThrows(CredentialsExpiredException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "532, xxxx]"));

      try {
        provider.authenticate(joe);
        fail("BadCredentialsException should had been thrown");
      } catch (final BadCredentialsException expected) {
      }

      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });

  }

  @Test
  public void accountDisabledIsCorrectlyMapped() {
    assertThrows(DisabledException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "533, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void accountExpiredIsCorrectlyMapped() {
    assertThrows(AccountExpiredException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "701, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });

  }

  @Test
  public void accountLockedIsCorrectlyMapped() {
    assertThrows(LockedException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "775, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });

  }

  @Test
  public void unknownErrorCodeIsCorrectlyMapped() {
    assertThrows(BadCredentialsException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg + "999, xxxx]"));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void errorWithNoSubcodeIsHandledCleanly() throws Exception {
    assertThrows(BadCredentialsException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new AuthenticationException(msg));
      provider.setConvertSubErrorCodesToExceptions(true);
      provider.authenticate(joe);
    });
  }

  @Test
  public void nonAuthenticationExceptionIsConvertedToSpringLdapException() throws Exception {
    assertThrows(org.springframework.ldap.CommunicationException.class, () -> {
      provider.contextFactory = createContextFactoryThrowing(new CommunicationException(msg));
      provider.authenticate(joe);
    });

  }

  @Test
  public void rootDnProvidedSeparatelyFromDomainAlsoWorks() throws Exception {
    final ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
        "mydomain.eu", "ldap://192.168.1.200/", "dc=ad,dc=eu,dc=mydomain");
    checkAuthentication("dc=ad,dc=eu,dc=mydomain", provider);

  }

  ContextFactory createContextFactoryThrowing(final NamingException e) {
    return new ContextFactory() {

      @Override
      DirContext createContext(final Hashtable<?, ?> env) throws NamingException {
        throw e;
      }
    };
  }

  ContextFactory createContextFactoryReturning(final DirContext ctx) {
    return new ContextFactory() {

      @Override
      DirContext createContext(final Hashtable<?, ?> env) throws NamingException {
        return ctx;
      }
    };
  }

  private void checkAuthentication(final String rootDn, final ActiveDirectoryLdapAuthenticationProvider provider)
      throws NamingException {
    final DirContext ctx = mock(DirContext.class);
    when(ctx.getNameInNamespace()).thenReturn("");

    final DirContextAdapter dca = new DirContextAdapter();
    final SearchResult sr = new SearchResult("CN=Joe Jannsen,CN=Users", dca, dca.getAttributes());
    final DistinguishedName searchBaseDn = new DistinguishedName(rootDn);
    when(ctx.search(eq(searchBaseDn), any(String.class), any(Object[].class), any(SearchControls.class)))
        .thenReturn(new MockNamingEnumeration(sr))
        .thenReturn(new MockNamingEnumeration(sr));

    provider.contextFactory = createContextFactoryReturning(ctx);

    Authentication result = provider.authenticate(joe);

    assertThat(result.getAuthorities(), empty());

    dca.addAttributeValue("memberOf", "CN=Admin,CN=Users,DC=mydomain,DC=eu");

    result = provider.authenticate(joe);

    assertThat(result.getAuthorities(), hasSize(1));
  }

  static class MockNamingEnumeration implements NamingEnumeration<SearchResult> {

    private SearchResult sr;

    public MockNamingEnumeration(final SearchResult sr) {
      this.sr = sr;
    }

    @Override
    public SearchResult next() {
      final SearchResult result = sr;
      sr = null;
      return result;
    }

    @Override
    public boolean hasMore() {
      return sr != null;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasMoreElements() {
      return hasMore();
    }

    @Override
    public SearchResult nextElement() {
      return next();
    }
  }
}
