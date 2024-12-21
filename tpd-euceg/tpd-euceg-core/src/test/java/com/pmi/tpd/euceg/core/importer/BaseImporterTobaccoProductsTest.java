package com.pmi.tpd.euceg.core.importer;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.PropertyException;

import org.eu.ceg.CasNumber;
import org.eu.ceg.Manufacturer;
import org.eu.ceg.Product;
import org.eu.ceg.TobaccoIngredient;
import org.eu.ceg.TobaccoOtherIngredient;
import org.eu.ceg.TobaccoProduct;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;
import com.pmi.tpd.testing.junit5.TestCase;

public class BaseImporterTobaccoProductsTest extends TestCase {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseImporterTobaccoProductsTest.class);

  @BeforeEach
  public void forceIndent() throws PropertyException {
    Eucegs.indentMarshalling(true);
  }

  @AfterEach
  public void restoreIndentMarshalling() throws PropertyException {
    Eucegs.indentMarshalling(false);
  }

  /**
   * Simple non-regression test.
   */
  @Test
  public void importNovel() throws IOException, Exception {
    final List<EucegProduct> list = importExcel("tobacco-product-submission-novel.xls");
    assertEquals(1, list.size());

    final TobaccoProduct product = extractTobaccoProduct(list, "POM.135640");

    final String xmlProduct = Eucegs.marshal(Eucegs.wrap(product, Product.class));
    // Files.write(xmlProduct, new File("test.txt"), Charsets.UTF_8);
    approve(xmlProduct);

  }

  @Test
  public void shouldTobaccoGroupsHaveCorrectKeys() {
    final ListDescriptor descriptors = EucegExcelSchema.TobaccoProduct.DESCRIPTORS;
    assertThat(transform(descriptors.get("ProductDetail").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Internal_Product_Number"));

    assertThat(transform(descriptors.get("Manufacturer").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Internal_Product_Number", "Manufacturer_ID", "Company_Name"));
    assertThat(
        transform(
            descriptors.get("Manufacturer").orElseThrow().getChildren("ProductionSite").getForeignKeyColumns(),
            this::toColumnName),
        contains("Internal_Product_Number", "Manufacturer_ID", "Company_Name", "Production_Site_Address"));
    assertThat(
        transform(descriptors.get("MarketResearchFile").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Internal_Product_Number"));

    assertThat(
        Iterables.transform(descriptors.get("Presentation").orElseThrow().getForeignKeyColumns(),
            this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number", "Product_Submitter_Number", "Product_National_Market"));
    assertThat(
        transform(descriptors.get("Presentation").orElseThrow().getChildren("SaleData").getForeignKeyColumns(),
            this::toColumnName),
        contains("Submitter_ID",
            "Internal_Product_Number",
            "Product_Submitter_Number",
            "Product_National_Market",
            "Product_Sales_Volume_Year"));

    assertThat(transform(descriptors.get("Ingredient").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID",
            "Internal_Product_Number",
            "Tobacco_Part_Type",
            "Tobacco_Leaf_Type",
            "Tobacco_Leaf_Cure_Method"));
    assertThat(
        transform(descriptors.get("Ingredient").orElseThrow().getChildren("Supplier").getForeignKeyColumns(),
            this::toColumnName),
        contains("Submitter_ID",
            "Internal_Product_Number",
            "Tobacco_Part_Type",
            "Tobacco_Leaf_Type",
            "Tobacco_Leaf_Cure_Method",
            "Supplier_ID",
            "Company_Name"));

    assertThat(
        transform(descriptors.get("OtherIngredient").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number", "Ingredient_Name"));

    assertThat(transform(descriptors.get("Emission").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number", "Emission_Other_Name"));

    assertThat(transform(descriptors.get("Cigarette").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number"));

    assertThat(transform(descriptors.get("Smokeless").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number"));

    assertThat(transform(descriptors.get("RollOwn").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number"));

    assertThat(transform(descriptors.get("Novel").orElseThrow().getForeignKeyColumns(), this::toColumnName),
        contains("Submitter_ID", "Internal_Product_Number"));
  }

  private String toColumnName(final ColumnDescriptor<?> col) {
    return col.getName();
  }

  /**
   * Check split correctly the Ingredient_CAS_Additional list of cas number.
   * <p>
   * The Ingredient_CAS_Additional field is multi-values with ';' separator.
   * <p/>
   *
   * @throws Exception
   * @see TPD-69
   */
  @Test
  public void TPD_69_IngredientAdditionnalIsStringArray() throws Exception {
    final List<EucegProduct> list = importExcel("tobacco-product-submission-novel.xls");
    assertEquals(1, list.size());

    final TobaccoProduct product = extractTobaccoProduct(list, "POM.135640");

    final TobaccoOtherIngredient ingredient = Iterables.find(product.getOtherIngredients().getIngredient(),
        input -> "7732-28-5".equals(input.getCasNumber().getValue()));

    assertThat(Iterables.transform(ingredient.getAdditionalCasNumbers().getCasNumber(), CasNumber::getValue),
        IsIterableContaining.hasItems("7440-50-8", "7440-66-6"));
  }

  /**
   * Check Submitter ID has been import for manufacturer.
   *
   * @throws Exception
   * @see TPD-273
   */
  @Test
  public void TPF_273_ShouldSubmitterIdExistForManufacturer() throws Exception {
    final List<EucegProduct> list = importExcel("tobacco-product-submission-submitter_id.xlsx");

    final TobaccoProduct product = extractTobaccoProduct(list, "POM.received");

    final List<Manufacturer> manufacturers = product.getManufacturers().getManufacturer();
    assertEquals("99962", Iterables.getFirst(manufacturers, null).getSubmitterID());
  }

  /**
   * Check Submitter ID has been import for supplier.
   *
   * @throws Exception
   * @see TPD-273
   */
  @Test
  public void TPD_273_ShouldSubmitterIdExistForSupplier() throws Exception {
    final List<EucegProduct> list = importExcel("tobacco-product-submission-submitter_id.xlsx");

    final TobaccoProduct product = extractTobaccoProduct(list, "POM.received");

    final TobaccoIngredient ingredient = Iterables.getFirst(product.getTobaccoIngredients().getTobaccoIngredient(),
        null);

    assertEquals("99962", Iterables.getFirst(ingredient.getSuppliers().getSupplier(), null).getSubmitterID());
  }

  @Test
  public void shouldAcceptMultipleSameIngredients() throws Exception {
    final List<EucegProduct> list = importExcel("tobacco-product-multiple-ingredient.xlsx");

    final TobaccoProduct product = extractTobaccoProduct(list, "POM.135640");

    final List<TobaccoOtherIngredient> ingredients = product.getOtherIngredients().getIngredient();

    assertEquals(4, ingredients.size());
  }

  private List<EucegProduct> importExcel(final String filename) throws Exception {

    try (InputStream in = getResourceAsStream(filename)) {

      final IImporterResult<EucegProduct> result = new SimpleExcelImporterTobaccoProducts(new SimpleI18nService())
          .importFromExcel(in, null);

      if (!result.getValidationResult().isEmpty()) {
        LOGGER.error(result.getValidationResult().toString());
        fail("the import has failed");
      }

      return result.getResults();
    }
  }

  private TobaccoProduct extractTobaccoProduct(final List<EucegProduct> list, final String productNumber) {
    return (TobaccoProduct) Maps
        .uniqueIndex(list,
            (Function<EucegProduct, String>) input -> Iterables
                .getFirst(((TobaccoProduct) input.getProduct()).getPresentations().getPresentation(), null)
                .getProductNumber()
                .getValue())
        .get(productNumber)
        .getProduct();
  }

  public static class SimpleExcelImporterTobaccoProducts extends BaseExcelmporterTobaccoProducts {

    public SimpleExcelImporterTobaccoProducts(final I18nService i18nService) {
      super(i18nService);
    }

    @Override
    protected boolean submitterExists(final String submitterId) {
      return true;
    }

    @Override
    protected String findAttachmentIdByFilename(final String filename) {
      return filename;
    }

    @Override
    public TobaccoProduct getCurrentProduct(final String productNumber) {
      return null;
    }

  }

}
