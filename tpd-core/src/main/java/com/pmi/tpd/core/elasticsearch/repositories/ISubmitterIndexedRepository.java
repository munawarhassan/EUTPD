package com.pmi.tpd.core.elasticsearch.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface ISubmitterIndexedRepository extends ElasticsearchRepository<SubmitterIndexed, String> {

}
