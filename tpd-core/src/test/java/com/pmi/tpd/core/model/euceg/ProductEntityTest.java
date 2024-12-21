package com.pmi.tpd.core.model.euceg;

import java.util.List;

import org.eu.ceg.Product;
import org.eu.ceg.TobaccoProduct;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.testing.junit5.TestCase;

public class ProductEntityTest extends TestCase {

  @Test
  public void lastestSubmissionWithEmptySubmissions() {
    final ProductEntity productEntity = ProductEntity.builder().build();
    assertNull(productEntity.getLastestSubmission());
  }

  @Test
  public void lastestSubmission() {
    final DateTime expectedDate = DateTime.now();
    final List<SubmissionEntity> list = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      final DateTime date = expectedDate.minusDays(i);
      list.add(SubmissionEntity.builder().lastModifiedDate(date).build());
    }
    final ProductEntity productEntity = ProductEntity.builder().submissions(list).build();
    final SubmissionEntity lastest = productEntity.getLastestSubmission();
    assertNotNull(lastest);
    assertEquals(expectedDate, lastest.getLastModifiedDate());
  }

  @Test
  public void shouldRemoveInvalidCharacters() {
    final ProductEntity productEntity = ProductEntity.builder()
        .product(new TobaccoProduct().withProductID(Eucegs.productNumber("pom\u0002.12345"))).build();

    Product product = Eucegs.unmarshal(productEntity.getXmlProduct(), Product.class);
    assertEquals("pom\uFFFD.12345", product.getProductID().getValue());
  }
}
