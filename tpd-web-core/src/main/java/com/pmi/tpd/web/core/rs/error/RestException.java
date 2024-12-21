package com.pmi.tpd.web.core.rs.error;

/**
 * Class to map application related exceptions.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class RestException extends Exception {

    private static final long serialVersionUID = -8999932578270387947L;

    /**
     * contains redundantly the HTTP status of the response sent back to the client in case of error, so that the
     * developer does not have to look into the response headers. If null a default
     */
    protected Integer status;

    /** application specific error code. */
    protected int code;

    /** link documenting the exception. */
    protected String link;

    /** detailed error description for developers. */
    protected String developerMessage;

    /**
     * @param status
     * @param code
     * @param message
     * @param developerMessage
     * @param link
     */
    public RestException(final int status, final int code, final String message, final String developerMessage,
            final String link) {
        super(message);
        this.status = status;
        this.code = code;
        this.developerMessage = developerMessage;
        this.link = link;
    }

    public RestException() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(final String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

}
