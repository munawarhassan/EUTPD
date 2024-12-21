package com.pmi.tpd.database.hibernate.envers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.hibernate.envers.DefaultRevisionEntity;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultRevisionMetadata}.
 */
public class DefaultRevisionMetadataUnitTest {

    private static final Instant NOW = Instant.now();;

    @Test
    public void createsLocalDateTimeFromTimestamp() {

        final DefaultRevisionEntity entity = new DefaultRevisionEntity();
        entity.setTimestamp(NOW.toEpochMilli());

        final DefaultRevisionMetadata metadata = new DefaultRevisionMetadata(entity);

        final LocalDateTime revisionDate = metadata.getRevisionInstant()
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault()))
                .get();

        assertThat(revisionDate,
            is(LocalDateTime.ofInstant(NOW, ZoneOffset.systemDefault()).truncatedTo(ChronoUnit.MILLIS)));
    }
}
