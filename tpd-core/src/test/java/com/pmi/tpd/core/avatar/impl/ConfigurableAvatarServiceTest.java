package com.pmi.tpd.core.avatar.impl;

import static org.mockito.ArgumentMatchers.same;

import java.awt.Image;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.AvatarStoreException;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.AvatarUrlDecorator;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;
import com.pmi.tpd.core.avatar.spi.IAvatarSource;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.context.MemoryApplicationProperties;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

public class ConfigurableAvatarServiceTest extends AbstractServiceTest {

    @Mock(lenient = true)
    private IAvatarSource defaultSource;

    @Mock
    private INavBuilder navBuilder;

    private IApplicationProperties applicationProperties;

    @Mock
    private IAvatarRepository repository;

    @Mock
    private IRequestContext requestContext;

    @Mock
    private AvatarUrlDecorator urlDecorator;

    private List<IAvatarSource> sources;

    public ConfigurableAvatarServiceTest() {
        super(ConfigurableAvatarService.class, IInternalAvatarService.class);
    }

    @Test
    public void testCreateSupplierFromDataUri() throws Exception {
        // This data URI embeds a newline (which should be stripped by createSupplierFromDataUri), and also contains
        // forward slashes and ends in padding equals, exercising most of the possibilities for the regular expression.
        // It also contains an _, which will be replaced with a /.
        final IAvatarSupplier supplier = buildService().createSupplierFromDataUri(
            "data:image/png;base64,iVBORw0KGgoAAAANS\n"
                    + "UhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4_/8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==");
        assertNotNull(supplier);
        assertEquals(MediaType.IMAGE_PNG_VALUE, supplier.getContentType());

        // The PNG data in the stream should be correctly Base64-decoded to produce a valid image
        final Image image = ImageIO.read(supplier.open());
        assertNotNull(image);
    }

    @Test
    public void testDeleteForUser() {
        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(42L);

        buildService().deleteForUser(user);

        verify(repository).delete(eq(AvatarType.USER), eq(42L));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void testDeleteForUserWithNullUser() {
        assertThrows(NullPointerException.class, () -> buildService().deleteForUser(null));
    }

    @Test
    public void testGetForUser() {
        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(1L);

        buildService().getForUser(user, 256);

        verify(repository).load(same(AvatarType.USER), eq(1L), eq(256));
    }

    @Test
    public void testGetUrlForPerson() throws Exception {
        final AvatarRequest request = AvatarRequest.from(navBuilder);
        final IPerson person = newPerson("foo");

        // propertiesService.getAvatarSource will return null -> defaultSource
        ConfigurableAvatarService service = buildService();
        assertTrue(service.isEnabled());
        service.getUrlForPerson(person, request);

        verify(defaultSource).getUrlForPerson(same(person), same(request));

        reset(defaultSource);
        applicationProperties.setAvatarSource(AvatarSourceType.Disable.name());

        // propertiesService.getAvatarSource will return "disabled" -> disabledSource
        service = buildService(false); // Recreate the service to pick up propertiesService training changes
        assertFalse(service.isEnabled());
        service.getUrlForPerson(person, request);

        verifyZeroInteractions(defaultSource);

        applicationProperties.setAvatarSource(AvatarSourceType.Gravatar.name());

        // propertiesService.getAvatarSource will return "default" -> defaultSource
        service = buildService(false); // Recreate the service to pick up propertiesService training changes
        assertTrue(service.isEnabled());
        service.getUrlForPerson(person, request);

        verify(defaultSource).getUrlForPerson(same(person), same(request));

        reset(defaultSource);

        applicationProperties.setAvatarSource(AvatarSourceType.Gravatar.name());

        // propertiesService.getAvatarSource will return "gravatar" (unexpected value) -> defaultSource
        service = buildService(false); // Recreate the service to pick up propertiesService training changes
        assertTrue(service.isEnabled());
        service.getUrlForPerson(person, request);

        verify(defaultSource).getUrlForPerson(same(person), same(request));

    }

    @Test
    public void testGetUrlForPersonRequestCaching() throws Exception {
        final AvatarRequest request = AvatarRequest.from(navBuilder);

        final IPerson fooWithoutEmail = newPerson("foo");
        final IPerson fooWithEmail = newPerson("foo", "foo@bar.com");
        final IPerson fooWithEmptyEmail = newPerson("foo", "");
        final IPerson barWithSameEmail = newPerson("bar", "foo@bar.com");
        final IUser user = newUser(1, "foo", "foo@bar.com");
        final IUser userWithDifferentId = newUser(2, "bar", "bar@foo.com");
        final IUser userWithSameId = newUser(1, "baz", "baz@foo.com");

        when(defaultSource.getUrlForPerson(fooWithoutEmail, request)).thenReturn("http://foo-without-email");
        when(defaultSource.getUrlForPerson(fooWithEmail, request)).thenReturn("http://foo-with-email");
        when(defaultSource.getUrlForPerson(fooWithEmptyEmail, request)).thenReturn("http://foo-with-empty-email");
        when(defaultSource.getUrlForPerson(barWithSameEmail, request)).thenReturn("http://bar-with-same-email");
        when(defaultSource.getUrlForPerson(user, request)).thenReturn("http://user");
        when(defaultSource.getUrlForPerson(userWithDifferentId, request)).thenReturn("http://user-with-different-id");
        when(defaultSource.getUrlForPerson(userWithSameId, request)).thenReturn("http://user-with-same-id");

        when(requestContext.isActive()).thenReturn(true);

        final ConfigurableAvatarService service = buildService();
        assertTrue(service.isEnabled());

        assertEquals("http://foo-without-email", service.getUrlForPerson(fooWithoutEmail, request)); // cache miss
        assertEquals("http://foo-with-email", service.getUrlForPerson(fooWithEmail, request)); // cache miss
        assertEquals("http://foo-without-email", service.getUrlForPerson(fooWithEmptyEmail, request)); // cache hit -
                                                                                                       // same as
                                                                                                       // fooWithoutEmail
        assertEquals("http://foo-with-email", service.getUrlForPerson(barWithSameEmail, request)); // cache hit - same
                                                                                                   // as fooWithEmail
        assertEquals("http://user", service.getUrlForPerson(user, request)); // cache miss
        assertEquals("http://user-with-different-id", service.getUrlForPerson(userWithDifferentId, request)); // cache
                                                                                                              // miss
        assertEquals("http://user", service.getUrlForPerson(userWithSameId, request)); // cache hit - same as user

        // repeat the same calls again - each should be a cache hit
        assertEquals("http://foo-without-email", service.getUrlForPerson(fooWithoutEmail, request));
        assertEquals("http://foo-with-email", service.getUrlForPerson(fooWithEmail, request));
        assertEquals("http://foo-without-email", service.getUrlForPerson(fooWithEmptyEmail, request));
        assertEquals("http://foo-with-email", service.getUrlForPerson(barWithSameEmail, request));
        assertEquals("http://user", service.getUrlForPerson(user, request));
        assertEquals("http://user-with-different-id", service.getUrlForPerson(userWithDifferentId, request));
        assertEquals("http://user", service.getUrlForPerson(userWithSameId, request));

        verify(defaultSource, times(4)).getUrlForPerson(any(IPerson.class), same(request)); // == number of cache misses
        verify(requestContext).addCleanupCallback(any(Runnable.class));
    }

    @Test
    public void testGetUrlForPersonAvatarRequestCaching() throws Exception {
        final AvatarRequest requestSmallUnsecured = new AvatarRequest(false, AvatarSize.ExtraSmall);
        final AvatarRequest requestSmallSecured = new AvatarRequest(true, AvatarSize.ExtraSmall);
        final AvatarRequest requestLargeUnsecured = new AvatarRequest(false, AvatarSize.Large);

        final IPerson foo = newPerson("foo", "foo@bar.com");

        when(defaultSource.getUrlForPerson(foo, requestSmallUnsecured)).thenReturn("http://foo-small-unsecured");
        when(defaultSource.getUrlForPerson(foo, requestLargeUnsecured)).thenReturn("http://foo-large-unsecured");
        when(defaultSource.getUrlForPerson(foo, requestSmallSecured)).thenReturn("https://foo-small-secured");

        when(requestContext.isActive()).thenReturn(true);

        final ConfigurableAvatarService service = buildService();
        assertTrue(service.isEnabled());

        // all should be cache misses - different avatar request
        assertEquals("http://foo-small-unsecured", service.getUrlForPerson(foo, requestSmallUnsecured));
        assertEquals("http://foo-large-unsecured", service.getUrlForPerson(foo, requestLargeUnsecured));
        assertEquals("https://foo-small-secured", service.getUrlForPerson(foo, requestSmallSecured));

        // repeat the same calls again - each should be a cache hit
        assertEquals("http://foo-small-unsecured", service.getUrlForPerson(foo, requestSmallUnsecured));
        assertEquals("http://foo-large-unsecured", service.getUrlForPerson(foo, requestLargeUnsecured));
        assertEquals("https://foo-small-secured", service.getUrlForPerson(foo, requestSmallSecured));

        verify(defaultSource, times(3)).getUrlForPerson(same(foo), any(AvatarRequest.class)); // == number of cache
                                                                                              // misses
        verify(requestContext).addCleanupCallback(any(Runnable.class));
    }

    @Test
    public void testGetUrlForPersonNoRequestContext() throws Exception {
        final AvatarRequest request = AvatarRequest.from(navBuilder);

        final IPerson foo = newPerson("foo", "foo@bar.com");

        when(defaultSource.getUrlForPerson(foo, request)).thenReturn("http://foo.com");
        when(requestContext.isActive()).thenReturn(false); // no active request context

        final ConfigurableAvatarService service = buildService();
        assertTrue(service.isEnabled());

        assertEquals("http://foo.com", service.getUrlForPerson(foo, request));
        assertEquals("http://foo.com", service.getUrlForPerson(foo, request));
        assertEquals("http://foo.com", service.getUrlForPerson(foo, request));

        verify(defaultSource, times(3)).getUrlForPerson(foo, request); // no cache, each call goes through avatar source
        verify(requestContext, times(0)).addCleanupCallback(any(Runnable.class));
    }

    @Test
    public void testGetUrlForPersonWithNullPerson() {
        assertThrows(NullPointerException.class, () -> {
            final AvatarRequest request = AvatarRequest.from(navBuilder);

            buildService().getUrlForPerson(null, request);
        });
    }

    @Test
    public void testGetUrlForPersonWithNullRequest() {
        assertThrows(NullPointerException.class, () -> {
            final IPerson person = mock(IPerson.class);

            buildService().getUrlForPerson(person, null);
        });
    }

    @Test
    public void testIsLocalForUser() throws Exception {
        final ConfigurableAvatarService service = buildService();
        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(42L);

        when(repository.isStored(eq(AvatarType.USER), eq(42L))).thenReturn(true);
        assertTrue(service.isLocalForUser(user));

        when(user.getId()).thenReturn(451L);
        assertFalse(service.isLocalForUser(user));
    }

    @Test
    public void testOnUserCleanup() throws Exception {
        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(42L);
        final UserCleanupEvent event = mock(UserCleanupEvent.class);
        when(event.getDeletedUser()).thenReturn(user);

        buildService().onUserCleanup(event);
        verify(repository).delete(same(AvatarType.USER), eq(42L));
        verify(urlDecorator).invalidate(same(user));

        reset(urlDecorator);

        // check any AvatarStoreException is not propagated
        when(user.getId()).thenReturn(43L);
        doThrow(AvatarStoreException.class).when(repository).delete(same(AvatarType.USER), eq(43L));
        buildService().onUserCleanup(event);
        verify(repository).delete(same(AvatarType.USER), eq(43L));
        verify(urlDecorator).invalidate(same(user));
    }

    @Test
    public void testSaveForUser() {
        final IAvatarSupplier supplier = mock(IAvatarSupplier.class);

        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(1L);

        buildService().saveForUser(user, supplier);

        verify(user).getId();
        verify(repository).store(same(AvatarType.USER), eq(1L), same(supplier));
        verify(urlDecorator).invalidate(same(user));
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(urlDecorator);
        verifyZeroInteractions(supplier);
    }

    @Test
    public void testSaveForUserWithNullUser() {
        assertThrows(NullPointerException.class, () -> {
            final IAvatarSupplier supplier = mock(IAvatarSupplier.class);

            try {
                buildService().saveForUser(null, supplier);
            } finally {
                verifyZeroInteractions(repository);
                verifyZeroInteractions(urlDecorator);
            }
        });

    }

    @Test
    public void testSetEnabled() {
        final AvatarRequest request = AvatarRequest.from(navBuilder);
        final IPerson person = newPerson("foo");

        // Should start on defaultSource
        final ConfigurableAvatarService service = buildService();
        service.getUrlForPerson(person, request);

        verify(defaultSource).getUrlForPerson(same(person), same(request));

        reset(defaultSource);
        when(defaultSource.getType()).thenReturn(AvatarSourceType.Gravatar);

        // Disable avatars and verify that the property is updated
        service.setEnabled(false);
        assertEquals(AvatarSourceType.Disable.name(), applicationProperties.getAvatarSource().get());

        // Now verify that the disabledSource responds to avatar requests
        service.getUrlForPerson(person, request);

        verifyZeroInteractions(defaultSource);

        // Re-enable avatars and verify that the property is updated
        service.setEnabled(true);
        assertEquals(AvatarSourceType.Gravatar.name(), applicationProperties.getAvatarSource().get());

        // Now verify that defaultSource responds to avatar requests
        service.getUrlForPerson(person, request);

        verify(defaultSource).getUrlForPerson(same(person), same(request));
    }

    private static IPerson newPerson(final String name) {
        final IPerson person = mock(IPerson.class, withSettings().lenient());
        when(person.getName()).thenReturn(name);

        return person;
    }

    private static IPerson newPerson(final String name, final String email) {
        final IPerson person = newPerson(name);
        when(person.getEmail()).thenReturn(email);

        return person;
    }

    private static IUser newUser(final long id, final String name, final String email) {
        final IUser stashUser = mock(IUser.class, withSettings().lenient());
        when(stashUser.getId()).thenReturn(id);
        when(stashUser.getName()).thenReturn(name);
        when(stashUser.getEmail()).thenReturn(email);

        return stashUser;
    }

    private ConfigurableAvatarService buildService(final boolean enforce) {
        if (enforce) {
            this.applicationProperties = new MemoryApplicationProperties();
        }
        when(defaultSource.getType()).thenReturn(AvatarSourceType.Gravatar);
        sources = Lists.newArrayList(defaultSource);
        return new ConfigurableAvatarService(new SimpleI18nService(), navBuilder, applicationProperties, repository,
                urlDecorator, sources, requestContext);
    }

    private ConfigurableAvatarService buildService() {
        return buildService(true);
    }

}
