package com.pmi.tpd.euceg.api.entity;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.euceg.api.ProductType;

public interface IProductEntity extends IInitializable, IIdentityEntity<String>, IAuditEntity {

    /**
     * <p>
     * accept.
     * </p>
     *
     * @param visitor
     *            a {@link ISubmissionVisitor} object.
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    <T> T accept(@Nonnull IProductVisitor<T> visitor);

    /**
     * @return Returns the current version of product.
     * @since 2.4
     */
    int getVersion();

    /**
     * Gets the indicating whether the {@link ProductEntity} instance is read only.
     *
     * @return Returns {@code true} whether the {@link ProductEntity} instance is read only, otherwise {@code false}.
     */
    boolean isReadOnly();

    /**
     * Gets the indicating whether the product is sendable.
     * <p>
     * A product can be send if:
     * </p>
     * <ul>
     * <li>product is {@link ProductStatus#VALID VALID}</li>
     * <li>latest submission doesn't exist, it is {@link SubmissionStatus#NOT_SEND send}</li>
     * <li>or latest submission has been {@link SubmissionStatus#CANCELLED cancelled}</li>
     * </ul>
     *
     * @return Returns {@code true} whether the product is sendable, otherwise {@code false}.
     */
    boolean isSendable();

    /**
     * Gets the product number.
     *
     * @return Returns a {@link String} representing the product number.
     */
    String getProductNumber();

    /**
     * Gets the internal product number.
     *
     * @return Returns a {@link String} representing the internal product number.
     */
    String getInternalProductNumber();

    /**
     * @return Returns a child.
     * @since 1.7
     */
    IProductEntity getChild();

    /**
     * Gets the previous product number.
     *
     * @return Returns a string representing the the previous product number.
     */
    String getPreviousProductNumber();

    /**
     * Gets the {@link ProductType}.
     *
     * @return Returns the {@link ProductType}.
     */
    ProductType getProductType();

    /**
     * Gets the specific product type.
     *
     * @return Returns the specific product type.
     */
    int getType();

    /**
     * Gets the {@link ProductStatus}.
     *
     * @return Returns the {@link ProductStatus}.
     */
    ProductStatus getStatus();

    /**
     * Gets the {@link ProductPirStatus}.
     *
     * @return Returns the {@link ProductPirStatus}.
     */
    ProductPirStatus getPirStatus();

    /**
     * @return Returns a {@link String} representing the possible submission type.
     */
    SubmissionTypeEnum getPreferredSubmissionType();

    /**
     * @return Returns a {@link String} representing the submitter ID.
     */
    String getSubmitterId();

    /**
     * @return Returns a {@link String} representing the possible general comment.
     */
    String getPreferredGeneralComment();

    /**
     * @return Returns a {@link String} representing the source file name, if any.
     */
    String getSourceFilename();

    /**
     * @return Returns the {@link PayloadEntity} associated to this product.
     */
    IPayloadEntity getPayloadProduct();

    /**
     * @return Returns a {@link String} representing the xml representation of product.
     * @see #getProduct().
     */
    String getXmlProduct();

    /**
     * @return Returns the EUCEG {@link Product} associated to this product.
     */
    @CheckForNull
    Product getProduct();

    /**
     * @return Returns a list of submissions associated to this product.
     */
    List<ISubmissionEntity> getSubmissions();

    /**
     * @return Returns the last submission associated to this product if exist, {@code null} otherwise.
     */
    ISubmissionEntity getLastestSubmission();

    /**
     * @return Returns the last submitted submission associated to this product if exist, {@code null} otherwise.
     */
    ISubmissionEntity getLastestSubmittedSubmission();

    /** **/
    Set<String> getAttachments();

}
