package com.pmi.tpd.core.security;

import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.security.provider.IAuthenticationProviderService;
import com.pmi.tpd.core.user.UserPreferenceKeys;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultAuthenticationSynchroniserTest extends MockitoTestCase {

    /** */
    @Mock
    private volatile IUserRepository userRepository;

    /** */
    @Mock
    private volatile IGroupRepository groupRepository;

    /** */
    @Mock
    private volatile IUserPreferencesManager userPreferencesManager;

    /** */
    @Mock
    private volatile IEventPublisher eventPublisher;

    /** */
    @Mock
    private volatile IAuthenticationProviderService authenticationProviderService;

    @InjectMocks
    private DefaultAuthenticationSynchroniser authenticationSynchroniser;

    @Test
    public void shouldReturnNullWhenLastUpdatePropertyExistsButIsNull() throws Exception {
        final IUser user = mock(IUser.class);
        final IPreferences pref = mock(IPreferences.class);

        when(pref.exists(UserPreferenceKeys.USER_LAST_UPDATE)).thenReturn(true);
        when(pref.getDate(UserPreferenceKeys.USER_LAST_UPDATE)).thenReturn(Optional.empty());

        when(userPreferencesManager.getPreferences(user)).thenReturn(Optional.of(pref));
        final DateTime lastUpdate = authenticationSynchroniser.getLastUpdate(user);

        assertNull(lastUpdate, "Last Update date should be null");
    }

    @Test
    public void shouldReturnNotNullWhenLastUpdatePropertyExists() throws Exception {
        final IUser user = mock(IUser.class);
        final IPreferences pref = mock(IPreferences.class);

        when(pref.exists(UserPreferenceKeys.USER_LAST_UPDATE)).thenReturn(true);
        when(pref.getDate(UserPreferenceKeys.USER_LAST_UPDATE)).thenReturn(Optional.of(new Date()));

        when(userPreferencesManager.getPreferences(user)).thenReturn(Optional.of(pref));
        final DateTime lastUpdate = authenticationSynchroniser.getLastUpdate(user);

        assertNotNull(lastUpdate, "Last Update date should be not null");
    }
}
