package com.pmi.tpd.database.hibernate.envers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;
import org.springframework.data.history.AnnotationRevisionMetadata;
import org.springframework.data.history.RevisionMetadata;

/**
 * Unit tests for {@link EnversRevisionRepositoryImpl}.
 */
public class EnversRevisionRepositoryImplUnitTest {

    @Test
    public void revisionTypeOfAnnotationRevisionMetadataIsProperlySet() {

        final Object[] data = new Object[] { "a", "some metadata", RevisionType.DEL };

        final DefaultJpaEnversRevisionRepository.QueryResult<Object> result = new DefaultJpaEnversRevisionRepository.QueryResult<>(
                data);

        final RevisionMetadata<?> revisionMetadata = result.createRevisionMetadata();

        assertThat(revisionMetadata, instanceOf(AnnotationRevisionMetadata.class));
        assertThat(revisionMetadata.getRevisionType(), is(RevisionMetadata.RevisionType.DELETE));
    }

    @Test
    public void revisionTypeOfDefaultRevisionMetadataIsProperlySet() {

        final Object[] data = new Object[] { "a", mock(DefaultRevisionEntity.class), RevisionType.DEL };

        final DefaultJpaEnversRevisionRepository.QueryResult<Object> result = new DefaultJpaEnversRevisionRepository.QueryResult<>(
                data);

        final RevisionMetadata<?> revisionMetadata = result.createRevisionMetadata();

        assertThat(revisionMetadata, instanceOf(DefaultRevisionMetadata.class));
        assertThat(revisionMetadata.getRevisionType(), is(RevisionMetadata.RevisionType.DELETE));
    }

}
