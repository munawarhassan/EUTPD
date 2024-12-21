package com.pmi.tpd.core.euceg.spi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eu.ceg.CasNumber;
import org.eu.ceg.CigaretteSpecific;
import org.eu.ceg.Company;
import org.eu.ceg.EcigAnnualSalesData;
import org.eu.ceg.EcigEmission;
import org.eu.ceg.EcigEmission.AdditionalProducts;
import org.eu.ceg.EcigPresentation;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProduct.Emissions;
import org.eu.ceg.EmissionName;
import org.eu.ceg.LeafCureMethod;
import org.eu.ceg.LeafType;
import org.eu.ceg.PartType;
import org.eu.ceg.Percentage;
import org.eu.ceg.Product;
import org.eu.ceg.Product.OtherProducts;
import org.eu.ceg.Product.SameCompositionProducts;
import org.eu.ceg.ProductIdentification;
import org.eu.ceg.RyoPipeSpecific;
import org.eu.ceg.SmokelessSpecific;
import org.eu.ceg.String100;
import org.eu.ceg.String40;
import org.eu.ceg.String500;
import org.eu.ceg.TobaccoAnnualSalesData;
import org.eu.ceg.TobaccoIngredient;
import org.eu.ceg.TobaccoIngredient.Suppliers;
import org.eu.ceg.TobaccoPresentation;
import org.eu.ceg.TobaccoPresentation.AnnualSalesDataList;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProduct.Presentations;
import org.eu.ceg.TobaccoProduct.TobaccoIngredients;
import org.eu.ceg.Year;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Throwables;
import com.pmi.tpd.core.model.euceg.ProductDifference;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.ProductRevision;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.binding.BooleanNullable;
import com.pmi.tpd.euceg.api.entity.BaseProductVisitor;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductStore {

    /**
     * @param pageRequest
     * @return
     */
    @Nonnull
    Page<ProductEntity> findAll(Pageable pageRequest);

    /**
     * @return
     */
    long count();

    /**
     * @return
     */
    @Nonnull
    List<ProductEntity> findAllNewProduct(@Nonnull ProductType productType);

    @Nonnull
    Page<ProductEntity> findAllValidProduct(@Nonnull final ProductType productType,
        @Nonnull final Pageable pageRequest);

    /**
     * @param pageable
     * @param uuid
     * @return
     * @since 3.0
     */
    @Nonnull
    Page<ProductEntity> findAllUseAttachment(@Nonnull Pageable pageable, @Nonnull final String uuid);

    /**
     * @param productId
     * @return
     */
    boolean exists(@Nonnull String productId);

    /**
     * @param entity
     * @return
     */
    ProductEntity detach(ProductEntity entity);

    /**
     * @param productNumber
     * @return
     * @throws javax.persistence.EntityNotFoundException
     *                                                                        if the entity state cannot be accessed
     * @throws org.springframework.orm.jpa.JpaObjectRetrievalFailureException
     *                                                                        if the entity state cannot be accessed
     */
    @Nonnull
    ProductEntity get(@Nonnull String productNumber);

    /**
     * @param productNumber
     * @return
     */
    @Nullable
    ProductEntity find(@Nonnull final String productNumber);

    /**
     * find product revisions
     *
     * @param productNumber
     *                      product number
     * @param pageRequest
     *                      paging parameters
     * @return a list of revisions
     * @since 2.4
     */
    Page<ProductRevision> findRevisions(@Nonnull final String productNumber, final Pageable pageRequest);

    /**
     * compare two revisions of a product
     *
     * @param productNumber
     *                         product number
     * @param originalRevision
     *                         original revision
     * @param revised
     *                         the revised product to be compared with the original.
     * @return Returns a new instance of {@link ProductDifference} representing the difference between the original
     *         product and the revised product.
     * @since 2.4
     */
    ProductDifference compareRevisions(@Nonnull final String productNumber,
        @Nullable Integer revisedRevision,
        @Nonnull Integer originalRevision) throws IOException;

    /**
     * Gets current revision of a product.
     *
     * @param productNumber
     *                      product number
     * @throws NoSuchElementException
     *                                if no value is present
     * @return Returns the current product revision.
     * @since 2.4
     */
    @Nonnull
    ProductRevision getCurrentRevision(@Nonnull final String productNumber);

    /**
     * @param submission
     */
    @Nonnull
    ProductEntity save(@Nonnull ProductEntity submission);

    /**
     * @param submission
     */
    ProductEntity saveAndFlush(@Nonnull ProductEntity submission);

    /**
     * @param entity
     * @return
     * @since 2.5
     */
    ProductEntity updateOnSubmission(ProductEntity entity);

    /**
     * @param submission
     */
    void remove(@Nonnull ProductEntity submission);

    /**
     * @param productNumber
     */
    void remove(@Nonnull String productNumber);

    /**
     * @param product
     * @return
     */
    ProductEntity create(@Nonnull ProductEntity product);

    /**
     * @param submission
     * @param result
     * @return
     */
    boolean validate(@Nonnull ProductEntity entity, @Nonnull ValidationResult result);

    /**
     * @param product
     * @return
     */
    ValidationResult validate(@Nonnull final Product product);

    /**
     * @param product
     * @return
     */
    Product normalize(Product product);

    /**
     * Gets the indicating whether the product child is already link to another product than {@code productNumber}.
     *
     * @param previousProductNumber
     *                              the previous product number to verify.
     * @param productNumber
     *                              the possible product number parent.
     * @return Returns {@code true} whether the product child is already link to another product, otherwise
     *         {@code false}.
     * @since 1.7
     */
    boolean hasChildWithAnotherProduct(String previousProductNumber, String productNumber);

    /**
    *
    */
    public static final CleanerProductVisitor CLEANER_PRODUCT = new CleanerProductVisitor();

    /**
    *
    */
    public static final NormalizeProductVisitor NORMALIZE_PRODUCT = new NormalizeProductVisitor();

    /**
    *
    */
    public static final Function<IProductEntity, ProductEntity> ALL_TRANSFORMATION_SUBMISSION = CLEANER_PRODUCT
            .andThen(NORMALIZE_PRODUCT);

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static final class NormalizeProductVisitor extends BaseProductVisitor<ProductEntity> {

        private NormalizeProductVisitor() {
        }

        @Override
        public ProductEntity visit(@Nonnull final IProductEntity entity) {
            return visit((ProductEntity) entity);
        }

        public ProductEntity visit(@Nonnull final ProductEntity entity) {
            final Product value = super.visit(entity.getProduct());
            return entity.copy().product(value).build();
        }

        @Override
        public Product visit(final @Nonnull TobaccoProduct product) {
            product.withOtherProductsExist(Eucegs.toBooleanNullable(product.getOtherProducts() != null
                    && CollectionUtils.isNotEmpty(product.getOtherProducts().getProductIdentification())));
            product.withSameCompositionProductsExist(
                Eucegs.toBooleanNullable(product.getSameCompositionProducts() != null && CollectionUtils
                        .isNotEmpty(product.getSameCompositionProducts().getProductIdentification())));
            if (product.getPresentations() != null && product.getPresentations().getPresentation() != null) {
                for (final TobaccoPresentation presentation : product.getPresentations().getPresentation()) {
                    presentation
                            .withHasOtherMarketData(Eucegs.toBooleanNullable(presentation.getOtherMarketData() != null
                                    && CollectionUtils.isNotEmpty(presentation.getOtherMarketData().getAttachment())));
                    presentation.withBrandSubtypeNameExists(Eucegs.toBoolean(presentation.getBrandSubtypeName() != null
                            && presentation.getBrandSubtypeName().getValue() != null
                            && !presentation.getBrandSubtypeName().getValue().isEmpty()));
                    presentation.withWithdrawalIndication(Eucegs.toBoolean(presentation.getWithdrawalDate() != null));
                }
            }
            product.withOtherIngredientsExist(Eucegs.toBoolean(product.getOtherIngredients() != null
                    && CollectionUtils.isNotEmpty(product.getOtherIngredients().getIngredient())));
            product.withOtherEmissionsAvailable(Eucegs.toBoolean(product.getOtherEmissions() != null
                    && CollectionUtils.isNotEmpty(product.getOtherEmissions().getEmission())));
            return forceConfidentiality(product);
        }

        private Product forceConfidentiality(final TobaccoProduct product) {
            final BooleanNullable otherProductsExist = product.getOtherProductsExist();
            if (otherProductsExist != null) {
                otherProductsExist.setConfidential(true);
            }
            final OtherProducts otherProducts = product.getOtherProducts();
            if (otherProducts != null) {
                final List<ProductIdentification> otherProductsIdentifications = otherProducts
                        .getProductIdentification();
                if (otherProductsIdentifications != null) {
                    for (final ProductIdentification otherProductsIdentification : otherProductsIdentifications) {
                        otherProductsIdentification.setConfidential(true);
                    }
                }
            }
            final BooleanNullable sameCompositionProductsExist = product.getSameCompositionProductsExist();
            if (sameCompositionProductsExist != null) {
                sameCompositionProductsExist.setConfidential(true);
            }
            final SameCompositionProducts sameCompositionProducts = product.getSameCompositionProducts();
            if (sameCompositionProducts != null) {
                final List<ProductIdentification> sameCompositionProductsIdentifications = sameCompositionProducts
                        .getProductIdentification();
                if (sameCompositionProductsIdentifications != null) {
                    for (final ProductIdentification sameComposition : sameCompositionProductsIdentifications) {
                        sameComposition.setConfidential(true);
                    }
                }
            }

            final org.eu.ceg.Double length = product.getLength();
            if (length != null) {
                length.setConfidential(true);
            }
            final org.eu.ceg.Double diameter = product.getDiameter();
            if (diameter != null) {
                diameter.setConfidential(true);
            }
            final org.eu.ceg.Integer filterLength = product.getFilterLength();
            if (filterLength != null) {
                filterLength.setConfidential(true);
            }
            final Presentations presentations = product.getPresentations();
            if (presentations != null) {
                final List<TobaccoPresentation> presentationsList = presentations.getPresentation();
                if (presentationsList != null) {
                    for (final TobaccoPresentation tobaccoPresentation : presentationsList) {
                        final BooleanNullable hasOtherMarketData = tobaccoPresentation.getHasOtherMarketData();
                        if (hasOtherMarketData != null) {
                            hasOtherMarketData.setConfidential(true);
                        }
                        final AnnualSalesDataList annualSalesDataList = tobaccoPresentation.getAnnualSalesDataList();
                        if (annualSalesDataList != null) {
                            final List<TobaccoAnnualSalesData> annualSalesData = annualSalesDataList
                                    .getAnnualSalesData();
                            for (final TobaccoAnnualSalesData tobaccoAnnualSalesData : annualSalesData) {
                                final org.eu.ceg.Double salesVolume = tobaccoAnnualSalesData.getSalesVolume();
                                if (salesVolume != null) {
                                    salesVolume.setConfidential(true);
                                }
                                final Year year = tobaccoAnnualSalesData.getYear();
                                if (year != null) {
                                    year.setConfidential(true);
                                }
                            }
                        }
                    }
                }
            }
            final TobaccoIngredients ingredients = product.getTobaccoIngredients();
            if (ingredients != null) {
                final List<TobaccoIngredient> ingredientsList = ingredients.getTobaccoIngredient();
                if (ingredientsList != null) {
                    for (final TobaccoIngredient tobaccoIngredient : ingredientsList) {
                        final LeafCureMethod leafCureMethode = tobaccoIngredient.getLeafCureMethod();
                        if (leafCureMethode != null) {
                            leafCureMethode.setConfidential(true);
                        }
                        final String100 leafCureMethodOther = tobaccoIngredient.getLeafCureMethodOther();
                        if (leafCureMethodOther != null) {
                            leafCureMethodOther.setConfidential(true);
                        }
                        final LeafType leafType = tobaccoIngredient.getLeafType();
                        if (leafType != null) {
                            leafType.setConfidential(true);
                        }
                        final String100 leafTypeOther = tobaccoIngredient.getLeafTypeOther();
                        if (leafTypeOther != null) {
                            leafTypeOther.setConfidential(true);
                        }
                        final PartType partType = tobaccoIngredient.getPartType();
                        if (partType != null) {
                            partType.setConfidential(true);
                        }
                        final String100 partTypeOther = tobaccoIngredient.getPartTypeOther();
                        if (partTypeOther != null) {
                            partTypeOther.setConfidential(true);
                        }
                        final org.eu.ceg.Double quantity = tobaccoIngredient.getQuantity();
                        if (quantity != null) {
                            quantity.setConfidential(true);
                        }
                        final Suppliers suppliers = tobaccoIngredient.getSuppliers();
                        if (suppliers != null) {
                            final List<Company> supplierList = suppliers.getSupplier();
                            if (supplierList != null) {
                                for (final Company company : supplierList) {
                                    company.setConfidential(true);
                                }
                            }
                        }
                    }
                }
            }
            final CigaretteSpecific cigaretteSpecific = product.getCigaretteSpecific();
            if (cigaretteSpecific != null) {
                final Percentage filterVentilation = cigaretteSpecific.getFilterVentilation();
                if (filterVentilation != null) {
                    filterVentilation.setConfidential(true);
                }
                final org.eu.ceg.Double filterDropPressureOpen = cigaretteSpecific.getFilterDropPressureOpen();
                if (filterDropPressureOpen != null) {
                    filterDropPressureOpen.setConfidential(true);
                }
                final org.eu.ceg.Double filterDropPressureClosed = cigaretteSpecific.getFilterDropPressureClosed();
                if (filterDropPressureClosed != null) {
                    filterDropPressureClosed.setConfidential(true);
                }
            }
            final SmokelessSpecific smokelessSpecific = product.getSmokelessSpecific();
            if (smokelessSpecific != null) {
                final org.eu.ceg.Double totalNicotineContent = smokelessSpecific.getTotalNicotineContent();
                if (totalNicotineContent != null) {
                    totalNicotineContent.setConfidential(true);
                }
            }
            final RyoPipeSpecific ryoPipeSpecific = product.getRyoPipeSpecific();
            if (ryoPipeSpecific != null) {
                final org.eu.ceg.Double totalNicotineContent = ryoPipeSpecific.getTotalNicotineContent();
                if (totalNicotineContent != null) {
                    totalNicotineContent.setConfidential(true);
                }
            }
            return product;
        }

        private Product forceConfidentiality(final EcigProduct product) {
            final BooleanNullable otherProductsExist = product.getOtherProductsExist();
            if (otherProductsExist != null) {
                otherProductsExist.setConfidential(true);
            }
            final OtherProducts otherProducts = product.getOtherProducts();
            if (otherProducts != null) {
                final List<ProductIdentification> otherProductsIdentifications = otherProducts
                        .getProductIdentification();
                if (otherProductsIdentifications != null) {
                    for (final ProductIdentification otherProductsIdentification : otherProductsIdentifications) {
                        otherProductsIdentification.setConfidential(true);
                    }
                }
            }
            final BooleanNullable sameCompositionProductsExist = product.getSameCompositionProductsExist();
            if (sameCompositionProductsExist != null) {
                sameCompositionProductsExist.setConfidential(true);
            }
            final SameCompositionProducts sameCompositionProducts = product.getSameCompositionProducts();
            if (sameCompositionProducts != null) {
                final List<ProductIdentification> sameCompositionProductsIdentifications = sameCompositionProducts
                        .getProductIdentification();
                if (sameCompositionProductsIdentifications != null) {
                    for (final ProductIdentification sameComposition : sameCompositionProductsIdentifications) {
                        sameComposition.setConfidential(true);
                    }
                }
            }
            final Emissions emissions = product.getEmissions();
            if (emissions != null) {
                final List<EcigEmission> emissionsList = emissions.getEmission();
                if (emissionsList != null) {
                    for (final EcigEmission ecigEmission : emissionsList) {
                        final AdditionalProducts additionalProducts = ecigEmission.getAdditionalProducts();
                        if (additionalProducts != null) {
                            final List<ProductIdentification> productIdentificationsList = additionalProducts
                                    .getProductIdentification();
                            if (productIdentificationsList != null) {
                                for (final ProductIdentification productIdentification : productIdentificationsList) {
                                    productIdentification.setConfidential(true);
                                }
                            }
                        }
                        final CasNumber casNumber = ecigEmission.getCasNumber();
                        if (casNumber != null) {
                            casNumber.setConfidential(true);
                        }
                        final String100 iupacName = ecigEmission.getIupacName();
                        if (iupacName != null) {
                            iupacName.setConfidential(true);
                        }
                        final EmissionName name = ecigEmission.getName();
                        if (name != null) {
                            name.setConfidential(true);
                        }
                        final String100 nameOther = ecigEmission.getNameOther();
                        if (nameOther != null) {
                            nameOther.setConfidential(true);
                        }
                        final String500 productCombination = ecigEmission.getProductCombination();
                        if (productCombination != null) {
                            productCombination.setConfidential(true);
                        }
                        final org.eu.ceg.Double quantity = ecigEmission.getQuantity();
                        if (quantity != null) {
                            quantity.setConfidential(true);
                        }
                        final String40 unit = ecigEmission.getUnit();
                        if (unit != null) {
                            unit.setConfidential(true);
                        }
                    }
                }
            }
            final org.eu.ceg.EcigProduct.Presentations presentations = product.getPresentations();
            if (presentations != null) {
                final List<EcigPresentation> presentationsList = presentations.getPresentation();
                if (presentationsList != null) {
                    for (final EcigPresentation ecigPresentation : presentationsList) {
                        final org.eu.ceg.EcigPresentation.AnnualSalesDataList annualSalesDataList = ecigPresentation
                                .getAnnualSalesDataList();
                        if (annualSalesDataList != null) {
                            final List<EcigAnnualSalesData> annualSalesData = annualSalesDataList.getAnnualSalesData();
                            if (annualSalesData != null) {
                                for (final EcigAnnualSalesData ecigAnnualSalesData : annualSalesData) {
                                    final org.eu.ceg.Double salesVolume = ecigAnnualSalesData.getSalesVolume();
                                    if (salesVolume != null) {
                                        salesVolume.setConfidential(true);
                                    }
                                    final Year year = ecigAnnualSalesData.getYear();
                                    if (year != null) {
                                        year.setConfidential(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return product;
        }

        @Override
        public Product visit(@Nonnull final EcigProduct product) {
            product.withOtherProductsExist(Eucegs.toBooleanNullable(product.getOtherProducts() != null
                    && CollectionUtils.isNotEmpty(product.getOtherProducts().getProductIdentification())));
            product.withSameCompositionProductsExist(
                Eucegs.toBooleanNullable(product.getSameCompositionProducts() != null && CollectionUtils
                        .isNotEmpty(product.getSameCompositionProducts().getProductIdentification())));
            if (product.getPresentations() != null && product.getPresentations().getPresentation() != null) {
                for (final EcigPresentation presentation : product.getPresentations().getPresentation()) {
                    presentation.withBrandSubtypeNameExists(Eucegs.toBoolean(presentation.getBrandSubtypeName() != null
                            && presentation.getBrandSubtypeName().getValue() != null
                            && !presentation.getBrandSubtypeName().getValue().isEmpty()));
                    presentation.withWithdrawalIndication(Eucegs.toBoolean(presentation.getWithdrawalDate() != null));
                }
            }
            return forceConfidentiality(product);
        }

    }

    public static final class CleanerProductVisitor extends BaseProductVisitor<ProductEntity> {

        public CleanerProductVisitor() {
        }

        @Override
        public ProductEntity visit(@Nonnull final IProductEntity entity) {
            return visit((ProductEntity) entity);
        }

        public ProductEntity visit(final ProductEntity entity) {
            final Product value = visit(entity.getProduct());
            return entity.copy().product(value).build();
        }

        @Override
        public Product visit(@Nonnull final Product product) {
            return cleanEmptyValue(product);
        }

        /**
         * This method go through object and detect "empty" value to delete them. Empty value are object with null or
         * empty "value" properties and only a confidentiality boolean. This need to be deleted in order not to ouput
         * empty XML tag (which would be invalid for the XSD)
         *
         * @param product
         * @return product, cleaned
         */
        public <U extends Product> U cleanEmptyValue(final U product) {
            return cleanEmptyValueRecursive(product);
        }

        private <U> U cleanEmptyValueRecursive(final U element) {
            if (element == null) {
                return element;
            }
            final Field[] elementFields = getAllField(element.getClass());
            for (final Field elementField : elementFields) {
                if (elementField.isAnnotationPresent(XmlElement.class)) {
                    try {
                        elementField.setAccessible(true);
                        final Object elementFieldValue = elementField.get(element);
                        if (elementFieldValue != null) {
                            if (isDeletableField(elementField, elementFieldValue)) {
                                // Delete field
                                elementField.set(element, null);
                            } else {
                                // Recurse
                                if (Collection.class.isAssignableFrom(elementField.getType())) {
                                    final Collection<?> fieldAsList = (Collection<?>) elementFieldValue;
                                    for (final Object fieldItem : fieldAsList) {
                                        cleanEmptyValueRecursive(fieldItem);
                                    }
                                } else {
                                    final Object cleanedValue = cleanEmptyValueRecursive(elementFieldValue);
                                    // Allow deep cleaning
                                    if (isDeletableField(elementField, cleanedValue)) {
                                        elementField.set(element, null);
                                    } else {
                                        elementField.set(element, cleanedValue);
                                    }
                                }

                            }
                        }
                    } catch (final Throwable e) {
                        Throwables.throwIfUnchecked(e);
                        throw new RuntimeException(e);
                    }
                }
            }
            return element;
        }

        /**
         * Deletable field have 2 element : value and confidentiality, and value is null or empty. Totally empty fields
         * are also deletables
         *
         * @param fieldValue
         * @return
         */
        private boolean isDeletableField(final Field field, final Object fieldValue) {
            final XmlElement el = field.getAnnotation(XmlElement.class);
            if (el != null && el.required()) {
                return false;
            }
            final Field[] fields = getAllField(fieldValue.getClass());
            boolean areAllFieldNull = true;
            final boolean hasOnlyTwoFields = fields.length == 2;
            boolean isValueFieldEmpty = false;
            for (final Field fieldChild : fields) {
                if (Modifier.isStatic(fieldChild.getModifiers())) {
                    continue;
                }
                final String fieldName = fieldChild.getName();
                try {

                    fieldChild.setAccessible(true);
                    final Object value = fieldChild.get(fieldValue);
                    final boolean isEmptyOrNull = value == null || value.equals("");
                    isValueFieldEmpty = isEmptyOrNull && fieldName.equals("value");
                    areAllFieldNull &= isEmptyOrNull;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (isValueFieldEmpty && hasOnlyTwoFields) {
                    return true;
                }
            }

            return areAllFieldNull;
        }

        /**
         * Get all fields (Public / private / protected / package) of a class and his ancestor
         *
         * @param classe
         * @return
         */
        private Field[] getAllField(final Class<?> classe) {
            final Field[] declaredField = Arrays.stream(classe.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .toArray(Field[]::new);
            final Class<?> superclass = classe.getSuperclass();
            if (superclass != null) {
                final Field[] inheritedField = getAllField(superclass);
                return ArrayUtils.addAll(declaredField, inheritedField);
            } else {
                return declaredField;
            }
        }

    }

}
