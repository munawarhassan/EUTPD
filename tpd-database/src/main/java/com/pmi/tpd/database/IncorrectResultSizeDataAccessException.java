package com.pmi.tpd.database;

/**
 * Data access exception thrown when a result was not of the expected size, for example when expecting a single row but
 * getting 0 or more than 1 rows.
 */
public class IncorrectResultSizeDataAccessException extends RuntimeException {

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /** expected result size. */
    private final int expectedSize;

    /** actual result size. */
    private final int actualSize;

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param expectedSize
     *            the expected result size
     */
    public IncorrectResultSizeDataAccessException(final int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param expectedSize
     *            the expected result size
     * @param actualSize
     *            the actual result size (or -1 if unknown)
     */
    public IncorrectResultSizeDataAccessException(final int expectedSize, final int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg
     *            the detail message
     * @param expectedSize
     *            the expected result size
     */
    public IncorrectResultSizeDataAccessException(final String msg, final int expectedSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg
     *            the detail message
     * @param expectedSize
     *            the expected result size
     * @param ex
     *            the wrapped exception
     */
    public IncorrectResultSizeDataAccessException(final String msg, final int expectedSize, final Throwable ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg
     *            the detail message
     * @param expectedSize
     *            the expected result size
     * @param actualSize
     *            the actual result size (or -1 if unknown)
     */
    public IncorrectResultSizeDataAccessException(final String msg, final int expectedSize, final int actualSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg
     *            the detail message
     * @param expectedSize
     *            the expected result size
     * @param actualSize
     *            the actual result size (or -1 if unknown)
     * @param ex
     *            the wrapped exception
     */
    public IncorrectResultSizeDataAccessException(final String msg, final int expectedSize, final int actualSize,
            final Throwable ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * @return Returns the expected result size.
     */
    public int getExpectedSize() {
        return this.expectedSize;
    }

    /**
     * @return Returns the actual result size (or -1 if unknown).
     */
    public int getActualSize() {
        return this.actualSize;
    }

}
