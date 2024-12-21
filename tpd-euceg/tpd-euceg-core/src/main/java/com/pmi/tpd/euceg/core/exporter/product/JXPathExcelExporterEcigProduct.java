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
public class JXPathExcelExporterEcigProduct extends BaseJXPathExcelExporterProduct {

    private final BulkRequest request;

    /** */
    private final ObjectMapper objectMapper;

    /**
     * Default Constructor
     */
    public JXPathExcelExporterEcigProduct(@Nonnull final IDataProvider<EucegProduct> dataProvider,
            final BulkRequest request, final ObjectMapper objectMapper) {
        super(EucegExcelSchema.EcigProduct.DESCRIPTORS, EucegExcelSchema.EcigProduct.getExportedSheets(),
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
                    addAttachments();
                    addPresentation();
                    addIngredient();
                    addEmission();
                    addDesign();
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

        this.entrySheet(EucegExcelSchema.EcigProduct.SUMMARIZE_SHEET);
        createRow();
        String json;
        try {
            json = this.objectMapper.writeValueAsString(request.getFilters());
            setValue("USED_FILTER", json);
        } catch (final JsonProcessingException e) {

        }

    }

    protected void addAdditionnalInformation() {
        this.entrySheet(EucegExcelSchema.EcigProduct.ADDITIONNAL_SHEET);
        createRow();
        setValues("AdditionnalInformation");
    }

    private void addProductDetail() {
        this.entrySheet(EucegExcelSchema.EcigProduct.PRODUCT_DETAILS_SHEET);
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

    private void addAttachments() {

        this.entrySheet(EucegExcelSchema.EcigProduct.ATTACHMENT_SHEET);
        createRow();
        setValues("Attachment");
    }

    private void addPresentation() {
        this.entrySheet(EucegExcelSchema.EcigProduct.PRESENTATION_SHEET);
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
        this.entrySheet(EucegExcelSchema.EcigProduct.INGREDIENT_SHEET);
        final int size = getValue("fmt:size(ingredients/ingredient)", Integer.class);
        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            relative("ingredients/ingredient[$i]", () -> {
                createRow();
                setValues("Ingredient");
            });
        }
    }

    private void addEmission() {
        this.entrySheet(EucegExcelSchema.EcigProduct.EMISSION_SHEET);
        final int size = getValue("fmt:size(emissions/emission)", Integer.class);
        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            relative("emissions/emission[$i]", () -> {
                createRow();
                setValues("Emission");
            });
        }
    }

    private void addDesign() {
        this.entrySheet(EucegExcelSchema.EcigProduct.DESIGN_SHEET);
        relative("design", () -> {
            createRow();
            setValues("Design");
        });

    }

}
