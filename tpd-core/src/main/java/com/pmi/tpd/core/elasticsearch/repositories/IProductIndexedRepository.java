package com.pmi.tpd.core.elasticsearch.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface IProductIndexedRepository extends ElasticsearchRepository<ProductIndexed, String> {

}
