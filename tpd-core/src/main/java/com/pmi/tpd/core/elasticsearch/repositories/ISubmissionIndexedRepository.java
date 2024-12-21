package com.pmi.tpd.core.elasticsearch.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface ISubmissionIndexedRepository extends ElasticsearchRepository<SubmissionIndexed, Long> {

}
