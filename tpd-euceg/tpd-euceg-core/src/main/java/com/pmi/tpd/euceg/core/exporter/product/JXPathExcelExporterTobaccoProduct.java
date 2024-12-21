package com.pmi.tpd.euceg.core.exporter.product;

import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.mutable.MutableInt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.exporter.ICallbackExport;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

/**
 * Tool class allowing export one or more tobacco products in excel file.
 *
 * @author Christophe Friederich
 * @since 2.5
 */
public class JXPathExcelExporterTobaccoProduct extends BaseJXPathExcelExporterProduct {

    private final BulkRequest request;

    /** */
    private final ObjectMapper objectMapper;

    /**
     * Default Constructor
     */
    public JXPathExcelExporterTobaccoProduct(@Nonnull final IDataProvider<EucegProduct> dataProvider,
            final BulkRequest request, final ObjectMapper objectMapper) {
        super(EucegExcelSchema.TobaccoProduct.DESCRIPTORS, EucegExcelSchema.TobaccoProduct.getExportedSheets(),
                ExportOption.builder().build(), dataProvider);
        this.request = request;
        this.objectMapper = objectMapper;
    }

    /**
     * export list of tobacco products in excel file
     *
     * @param stream
     *                the stream used to.
     * @param request
     *                the list of tobacco product to export
     * @throws Throwable
     *                   if export failed.
     */
    public void export(@Nonnull final OutputStream stream) throws Throwable {
        super.export(stream, null, new ICallbackExport<EucegProduct>() {

            @Override
            public void forEach(final @Nonnull EucegProduct product) {
                relative("product", () -> {
                    addProductDetail();
                    addMarketResearch();
                    addPresentation();
                    addIngredient();
                    addOtherIngredient();
                    addOtherEmission();
                    addCigarette();
                    addSmokeless();
                    addRollOwn();
                    addNovel();
                    addAdditionnalInformation();
                });

            }

            @Override
            public void summarize() {
                if (objectMapper == null) {
                    return;
                }
                addSummarize();

            }
        });
    }

    protected void addSummarize() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.SUMMARIZE_SHEET);
        createRow();
        String json;
        try {
            json = this.objectMapper.writeValueAsString(request.getFilters());
            setValue("USED_FILTER", json);
        } catch (final JsonProcessingException e) {

        }
    }

    protected void addAdditionnalInformation() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.ADDITIONNAL_SHEET);
        createRow();
        setValues("AdditionnalInformation");
    }

    private void addProductDetail() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.PRODUCT_DETAILS_SHEET);
        createRow();
        setValues("ProductDetail");

        final int sizeManufacturer = getValue("fmt:size(manufacturers/manufacturer)", Integer.class);
        if (sizeManufacturer > 0) {
            final MutableInt i = new MutableInt(1);
            setVariable("$i", i.getValue());
            do {
                if (i.getValue() > 1) {
                    createRow();
                    setValuesWithForeignKeys("Manufacturer");
                }
                relative("manufacturers/manufacturer[$i]", () -> {
                    setValues("Manufacturer");

                    final int sizeSite = getValue("fmt:size(productionSiteAddresses/productionSiteAddress)",
                        Integer.class);
                    if (sizeSite > 0) {
                        final MutableInt j = new MutableInt(1);
                        setVariable("$j", j.getValue());
                        do {
                            if (j.getValue() > 1) {
                                createRow();
                                setValuesWithForeignKeys("ProductionSite");
                            }
                            relative("productionSiteAddresses/productionSiteAddress[$j]", () -> {
                                setValues("ProductionSite");
                            });
                            setVariable("$j", j.incrementAndGet());
                        } while (j.getValue() <= sizeSite);
                    }
                });
                setVariable("$i", i.incrementAndGet());
            } while (i.getValue() <= sizeManufacturer);
        }

    }

    private void addMarketResearch() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.MARKET_RESEARCH_SHEET);
        final int size = getValue("fmt:size(marketResearchFiles/attachment)", Integer.class);
        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            createRow();
            setValues("MarketResearchFile");
        }
    }

    private void addPresentation() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.PRESENTATION_SHEET);
        final int size = getValue("fmt:size(presentations/presentation)", Integer.class);

        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            createRow();

            relative("presentations/presentation[$i]", () -> {
                setValues("Presentation");

                final int sizeSale = getValue("fmt:size(annualSalesDataList/annualSalesData)", Integer.class);
                if (sizeSale > 0) {
                    final MutableInt j = new MutableInt(1);
                    setVariable("$j", j.getValue());
                    do {
                        if (j.getValue() > 1) {
                            createRow();
                            setValuesWithForeignKeys("SaleData");
                        }
                        relative("annualSalesDataList/annualSalesData[$j]", () -> {
                            setValues("SaleData");
                        });
                        setVariable("$j", j.incrementAndGet());
                    } while (j.getValue() <= sizeSale);

                }
            });
        }

    }

    private void addIngredient() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.INGREDIENT_SHEET);
        final int size = getValue("fmt:size(tobaccoIngredients/tobaccoIngredient)", Integer.class);

        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);

            relative("tobaccoIngredients/tobaccoIngredient[$i]", () -> {
                createRow();
                setValues("Ingredient");
                final int sizeSale = getValue("fmt:size(suppliers/supplier)", Integer.class);
                if (sizeSale > 0) {
                    final MutableInt j = new MutableInt(1);
                    setVariable("$j", j.getValue());
                    do {
                        if (j.getValue() > 1) {
                            createRow();
                            setValuesWithForeignKeys("Supplier");
                        }
                        relative("suppliers/supplier[$j]", () -> {
                            setValues("Supplier");

                        });
                        setVariable("$j", j.incrementAndGet());
                    } while (j.getValue() <= sizeSale);

                }
            });
        }

    }

    private void addOtherIngredient() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.OTHER_INGREDIENT_SHEET);
        final int size = getValue("fmt:size(otherIngredients/ingredient)", Integer.class);
        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            relative("otherIngredients/ingredient[$i]", () -> {
                createRow();
                setValues("OtherIngredient");
            });
        }
    }

    private void addOtherEmission() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.OTHER_EMISSION_SHEET);
        final boolean tncoEmissionExist = getValue("fmt:exists(/tncoEmission)", Boolean.class);

        int size = getValue("fmt:size(otherEmissions/emission)", Integer.class);
        if (size == 0 && tncoEmissionExist) {
            size = 1;
        }
        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            relative("otherEmissions/emission[$i]", () -> {
                createRow();
                setValues("Emission");
            }, true);
        }

    }

    private void addCigarette() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.CIGARETTE_SPEC_SHEET);
        relative("cigaretteSpecific", () -> {
            createRow();
            setValues("Cigarette");
        });
    }

    private void addSmokeless() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.SMOKELESS_SPEC_SHEET);
        relative("smokelessSpecific", () -> {
            createRow();
            setValues("Smokeless");
        });
    }

    private void addRollOwn() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.ROLL_OWN_SPEC_SHEET);
        relative("ryoPipeSpecific", () -> {
            createRow();
            setValues("RollOwn");
        });
    }

    private void addNovel() {
        this.entrySheet(EucegExcelSchema.TobaccoProduct.NOVEL_SPEC_SHEET);
        relative("novelSpecific", () -> {
            createRow();
            setValues("Novel");
        });
    }

}
