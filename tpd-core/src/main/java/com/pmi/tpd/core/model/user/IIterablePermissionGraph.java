package com.pmi.tpd.core.model.user;

import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.IEffectivePermission;

/**
 * A {@link PermissionGraph} that permits iteration of its {@link EffectivePermission}s.
 */
public interface IIterablePermissionGraph extends IPermissionGraph, Iterable<IEffectivePermission> {

}
