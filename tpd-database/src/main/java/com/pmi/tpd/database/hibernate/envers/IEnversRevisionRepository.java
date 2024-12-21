package com.pmi.tpd.database.hibernate.envers;

import java.io.Serializable;

import org.springframework.data.repository.history.RevisionRepository;

/**
 * @author Christophe Friederich
 * @since 1.0
 * @param <T>
 *            the type of entitiy
 * @param <ID>
 *            the type of unique identifier of type T.
 * @param <N>
 *            the type of revision number
 */
public interface IEnversRevisionRepository<T, ID extends Serializable, N extends Number & Comparable<N>>
        extends RevisionRepository<T, ID, N> {

}
