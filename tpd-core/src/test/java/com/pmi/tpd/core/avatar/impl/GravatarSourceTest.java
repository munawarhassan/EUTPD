package com.pmi.tpd.core.avatar.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class GravatarSourceTest extends MockitoTestCase {

    private GravatarSource service;

    @BeforeEach
    public void setup() throws Exception {
        service = new GravatarSource("http|%1$s|%2$d|%3$s|%4$s", "https|%1$s|%2$d|%3$s|%4$s", "mm");
    }

    @Test
    public void testGetUrlForPersonWithStashUser() throws Exception {
        final IUser user = mock(IUser.class);
        when(user.getEmail()).thenReturn("bturner@company.com");

        final String url = service.getUrlForPerson(user, new AvatarRequest(true, AvatarSize.Medium));
        assertEquals("https|3730eaf874535149ae2a0c3b99a69286|96|mm|bturner@company.com", url);
    }

    @Test
    public void testGetUrlForPersonWithNullEmailAddress() throws Exception {
        final IPerson person = mock(IPerson.class);

        final String url = service.getUrlForPerson(person, new AvatarRequest(false, AvatarSize.ExtraSmall));
        assertEquals("http|00000000000000000000000000000000|48|mm|", url);
    }

    @Test
    public void testGetUrlForPersonWithPerson() throws Exception {
        final IPerson person = mock(IPerson.class);
        when(person.getEmail()).thenReturn("bturner@company.com");

        final String url = service.getUrlForPerson(person, new AvatarRequest(false, AvatarSize.ExtraSmall));
        assertEquals("http|3730eaf874535149ae2a0c3b99a69286|48|mm|bturner@company.com", url);
    }

    @Test
    public void testGetUrlForPersonIsCaseInsensitiveWithEmails() throws Exception {
        IPerson person = mock(IPerson.class);
        when(person.getEmail()).thenReturn("bturner@company.com");

        String url = service.getUrlForPerson(person, new AvatarRequest(false, AvatarSize.ExtraSmall));
        assertEquals("http|3730eaf874535149ae2a0c3b99a69286|48|mm|bturner@company.com", url);

        person = mock(IPerson.class);
        when(person.getEmail()).thenReturn("BtUrNeR@company.COM");

        url = service.getUrlForPerson(person, new AvatarRequest(false, AvatarSize.ExtraSmall));
        assertEquals("http|3730eaf874535149ae2a0c3b99a69286|48|mm|BtUrNeR@company.COM", url);
    }

    @Test
    public void testCustomDefaultUrl() throws Exception {
        final IUser user = mock(IUser.class);
        when(user.getEmail()).thenReturn("bturner@company.com");

        service = new GravatarSource("http|%1$s|%2$d|%3$s|%4$s", "https|%1$s|%2$d|%3$s|%4$s",
                "http://example.com/images/avatar.jpg");
        final String url = service.getUrlForPerson(user, new AvatarRequest(true, AvatarSize.Medium));
        assertEquals(
            "https|3730eaf874535149ae2a0c3b99a69286|96|http://example.com/images/avatar.jpg|bturner@company.com",
            url);
    }
}
