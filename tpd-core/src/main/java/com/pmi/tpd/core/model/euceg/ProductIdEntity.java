package com.pmi.tpd.core.model.euceg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.database.support.Identifiable;
import com.pmi.tpd.api.util.Assert;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "ProductId")
@Table(name = ProductIdEntity.TABLE_NAME)
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class ProductIdEntity extends Identifiable<String> {

    /** table name. */
    public static final String TABLE_NAME = "t_product_id";

    /** */
    @Id
    @NotNull
    @Size(max = 20)
    @Column(name = "submitter_id", length = 20, nullable = false)
    private String submitterId;

    /** */
    @Column(name = "current_value", nullable = false, columnDefinition = "int default 1")
    private int currentValue = 1;

    public ProductIdEntity() {

    }

    public ProductIdEntity(final String submitterId) {
        this.submitterId = Assert.checkHasText(submitterId, "submitterId");
    }

    @Override
    public String getId() {
        return submitterId;
    }

    public void setSubmitterId(final String submitterId) {
        this.submitterId = submitterId;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public int incr() {
        return ++currentValue;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("submitterId", submitterId)
                .add("currentValue", currentValue)
                .toString();
    }

}
