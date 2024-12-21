package com.pmi.tpd.euceg.backend.core.domibus.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private String username;

    private String password;
}
