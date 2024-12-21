package com.pmi.tpd.cluster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate {@link com.pmi.tpd.api.exec.IRunnableTask} which support running in a <i>multi-node</i>
 * cluster. Even tasks which do not have this annotation will be allowed in a single- node cluster, on the assumption
 * that such a cluster is no different from standalone.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClusterableTask {
}
