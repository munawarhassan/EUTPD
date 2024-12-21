package com.pmi.tpd.euceg.api.entity;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;

public interface IPayloadEntity extends IInitializable, IIdentityEntity<Long>, IAuditEntity {

    String getData();

}