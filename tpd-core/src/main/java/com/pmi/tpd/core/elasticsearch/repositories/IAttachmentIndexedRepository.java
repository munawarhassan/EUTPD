package com.pmi.tpd.core.elasticsearch.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;

/**
 * @author Christophe Friederich
 * @since 2.2
 */
public interface IAttachmentIndexedRepository extends ElasticsearchRepository<AttachmentIndexed, String> {

}
