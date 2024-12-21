package com.pmi.tpd.web.rest.rsrc.api.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeystoreValidationResponse  {

    public String Message;
    public String Alias;
    public boolean isOk;
}
