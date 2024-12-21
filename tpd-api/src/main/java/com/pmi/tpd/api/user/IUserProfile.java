package com.pmi.tpd.api.user;

import java.net.URI;

public interface IUserProfile {

    UserKey getUserKey();

    String getUsername();

    String getDisplayName();

    String getEmail();

    URI getProfilePictureUri(int width, int height);

    URI getProfilePictureUri();

}
