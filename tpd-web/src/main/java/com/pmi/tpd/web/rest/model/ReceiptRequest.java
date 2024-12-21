package com.pmi.tpd.web.rest.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eu.ceg.AbstractAppResponse;
import org.eu.ceg.AppResponse;
import org.eu.ceg.Error;
import org.eu.ceg.ErrorResponse;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@JsonSerialize
public class ReceiptRequest extends RepresentationModel<RepresentationModel<?>> {

    /** */
    private String name;

    /** */
    private String messageId;

    /** */
    private TransmitStatus status;

    /** */
    private PayloadType type;

    /** */
    private Long submissionId;

    /** */
    private AppResponse response;

    private boolean error;

    private List<ReceiptError> errorDetails;

    public static List<ReceiptRequest> map(final List<? extends ITransmitReceiptEntity> entities) {
        return map(entities, null);
    }

    public static List<ReceiptRequest> map(final List<? extends ITransmitReceiptEntity> entities,
        @Nullable final RepresentationModelAssembler<ITransmitReceiptEntity, ReceiptRequest> assembler) {
        final List<ReceiptRequest> receiptRequests = new ArrayList<>();
        for (final ITransmitReceiptEntity transmitReceiptEntity : entities) {
            receiptRequests.add(from(transmitReceiptEntity, assembler));
        }
        return receiptRequests;
    }

    /**
     * @param entity
     * @return
     */
    public static ReceiptRequest from(final ITransmitReceiptEntity entity,
        @Nullable final RepresentationModelAssembler<ITransmitReceiptEntity, ReceiptRequest> assembler) {
        final ReceiptRequest request;
        if (assembler == null) {
            request = new ReceiptRequest();
        } else {
            request = assembler.toModel(entity);
        }
        request.name = entity.getName();
        request.status = entity.getTransmitStatus();
        request.messageId = entity.getMessageId();
        request.type = entity.getType();
        request.submissionId = entity.getSubmission().getId();
        final AppResponse entityResponse = entity.getResponse();
        request.response = entityResponse;
        request.error = entity.isError();
        if (entityResponse != null) {
            if (entityResponse instanceof ErrorResponse) {
                request.errorDetails = new ArrayList<>();
                final ErrorResponse entityErrorResponse = (ErrorResponse) entityResponse;
                request.errorDetails
                        .add(new ReceiptError(entityErrorResponse.getCode(), entityErrorResponse.getMessage()));
            } else if (entityResponse instanceof AbstractAppResponse) {
                final AbstractAppResponse abstractResponse = (AbstractAppResponse) entityResponse;
                request.errorDetails = new ArrayList<>();
                if (abstractResponse.getError() != null) {
                    request.errorDetails.add(new ReceiptError(abstractResponse.getError().getCode(),
                            abstractResponse.getError().getMessage()));
                }
                if (abstractResponse.getErrors() != null) {
                    for (final Error error : abstractResponse.getErrors().getError()) {
                        request.errorDetails.add(new ReceiptError(error.getCode(), error.getMessage()));
                    }
                }
            }
        }
        return request;
    }

    public static class ReceiptError {

        private final String code;

        private final String message;

        public ReceiptError(final String code, final String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

}
