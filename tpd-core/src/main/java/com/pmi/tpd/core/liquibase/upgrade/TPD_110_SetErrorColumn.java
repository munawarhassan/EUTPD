package com.pmi.tpd.core.liquibase.upgrade;

import java.util.Map;
import java.util.function.Consumer;

import org.eu.ceg.AbstractAppResponse;
import org.eu.ceg.AppResponse;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.google.common.base.Strings;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import liquibase.change.ColumnConfig;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class TPD_110_SetErrorColumn implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPD_110_SetErrorColumn.class);

    private static FormattingConversionService converter = new DefaultFormattingConversionService();

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;

    @Override
    public String getConfirmationMessage() {
        return "Set Error to Receipt";
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(final ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;

    }

    @Override
    public ValidationErrors validate(final Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(final Database database) throws CustomChangeException {
        final DefaultLiquibaseAccessor dao = new DefaultLiquibaseAccessor(database, 1000);
        final ICancelState cancelState = new SimpleCancelState();
        try {
            dao.beginChangeSet();
            dao.forEachRow(TransmitReceiptEntity.TABLE_NAME, "id", cancelState, new SetUpdateColumn(dao));
        } finally {
            dao.endChangeSet();
        }
    }

    private class SetUpdateColumn implements Consumer<Map<String, Object>> {

        private final ILiquibaseAccessor dao;

        public SetUpdateColumn(final ILiquibaseAccessor dao) {
            this.dao = dao;
        }

        @Override
        public void accept(final Map<String, Object> row) {
            Boolean error = converter.convert(row.get("response_error"), Boolean.class);
            if (error != null) {
                return;
            }

            final Long id = converter.convert(row.get("id"), Long.class);

            final UpdateDataChange change = new UpdateDataChange();
            change.setTableName(TransmitReceiptEntity.TABLE_NAME);
            change.setWhere("id = ?");
            change.addWhereParam(new ColumnConfig().setName("id").setValueNumeric(id));
            error = isError(row);
            change.addColumn(new ColumnConfig().setName("response_error").setValueBoolean(error).setType("boolean"));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("set '{}' to error column of  '{}' table", error, TransmitReceiptEntity.TABLE_NAME);
            }
            dao.update(change);
        }

        private boolean isError(final Map<String, Object> row) {
            final AppResponse response = getResponse(row);
            final TransmitStatus status = getTransmitStatus(row);
            if (status != null) {
                switch (status) {
                    case DELETED:
                    case REJECTED:
                        return true;
                    default:
                        break;
                }
            }
            if (response == null) {
                return false;
            }
            if (response instanceof ErrorResponse) {
                return true;
            }
            if (response instanceof AbstractAppResponse) {
                final AbstractAppResponse r = (AbstractAppResponse) response;
                return ResponseStatus.ERROR.equals(r.getStatus());
            }
            return false;
        }

        public AppResponse getResponse(final Map<String, Object> row) {
            final String xmlResponse = converter.convert(row.get("xml_response"), String.class);
            if (Strings.isNullOrEmpty(xmlResponse)) {
                return null;
            }
            return Eucegs.unmarshal(xmlResponse);
        }

        public TransmitStatus getTransmitStatus(final Map<String, Object> row) {
            return converter.convert(row.get("transmit_status"), TransmitStatus.class);
        }
    }

}
