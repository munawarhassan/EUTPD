package com.pmi.tpd.core.user.permission;

import java.util.Collection;

/**
 * Handles the partitioning of groups in batch sizes.
 * <p>
 * This is intended to handle large numbers of groups that may not fit into memory, and may not be handled by some DB
 * queries (such as Oracle).
 *
 * @since 2.4
 */
public interface PartitionedGroups extends Iterable<Collection<String>> {
}
