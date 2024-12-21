package com.pmi.tpd.api.event.advisor.event;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.pmi.tpd.api.event.advisor.EventLevel;

/**
 * This class represents an application event.
 *
 * @author devacfr
 */
public class Event {

    private static String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /** */
    private EventType key;

    /** */
    private String desc;

    /** */
    private String exception;

    /** */
    private EventLevel level;

    /** */
    private String date;

    /** */
    private int progress = -1;

    /** */
    private final Map<String, Object> attributes;

    /**
     * <p>
     * Constructor for Event.
     * </p>
     *
     * @param key
     *            a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     * @param desc
     *            a {@link java.lang.String} object.
     */
    public Event(final EventType key, final String desc) {
        this.key = key;
        this.desc = desc;
        this.date = getFormattedDate();
        this.attributes = new HashMap<>();
    }

    /**
     * <p>
     * Constructor for Event.
     * </p>
     *
     * @param key
     *            a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     * @param desc
     *            a {@link java.lang.String} object.
     * @param exception
     *            a {@link java.lang.String} object.
     */
    public Event(final EventType key, final String desc, final String exception) {
        this.key = key;
        this.desc = desc;
        this.exception = exception;
        this.date = getFormattedDate();
        this.attributes = new HashMap<>();
    }

    /**
     * <p>
     * Constructor for Event.
     * </p>
     *
     * @param key
     *            a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     * @param desc
     *            a {@link java.lang.String} object.
     * @param level
     *            a {@link com.pmi.tpd.core.event.advisor.EventLevel} object.
     */
    public Event(final EventType key, final String desc, final EventLevel level) {
        this.key = key;
        this.desc = desc;
        this.level = level;
        this.date = getFormattedDate();
        this.attributes = new HashMap<>();
    }

    /**
     * <p>
     * Constructor for Event.
     * </p>
     *
     * @param key
     *            a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     * @param desc
     *            a {@link java.lang.String} object.
     * @param exception
     *            a {@link java.lang.String} object.
     * @param level
     *            a {@link com.pmi.tpd.core.event.advisor.EventLevel} object.
     */
    public Event(final EventType key, final String desc, final String exception, final EventLevel level) {
        this.key = key;
        this.desc = desc;
        this.exception = exception;
        this.level = level;
        this.date = getFormattedDate();
        this.attributes = new HashMap<>();
    }

    /**
     * <p>
     * Getter for the field <code>key</code>.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     */
    public EventType getKey() {
        return key;
    }

    /**
     * <p>
     * Setter for the field <code>key</code>.
     * </p>
     *
     * @param name
     *            a {@link com.pmi.tpd.api.event.advisor.event.EventType} object.
     */
    public void setKey(final EventType name) {
        this.key = name;
    }

    /**
     * <p>
     * Getter for the field <code>desc</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * <p>
     * Setter for the field <code>desc</code>.
     * </p>
     *
     * @param desc
     *            a {@link java.lang.String} object.
     */
    public void setDesc(final String desc) {
        this.desc = desc;
    }

    /**
     * <p>
     * Getter for the field <code>exception</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getException() {
        return exception;
    }

    /**
     * <p>
     * Setter for the field <code>exception</code>.
     * </p>
     *
     * @param exception
     *            a {@link java.lang.String} object.
     */
    public void setException(final String exception) {
        this.exception = exception;
    }

    /**
     * <p>
     * Getter for the field <code>level</code>.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.event.advisor.EventLevel} object.
     */
    public EventLevel getLevel() {
        return level;
    }

    /**
     * <p>
     * Setter for the field <code>level</code>.
     * </p>
     *
     * @param level
     *            a {@link com.pmi.tpd.core.event.advisor.EventLevel} object.
     */
    public void setLevel(final EventLevel level) {
        this.level = level;
    }

    /**
     * <p>
     * Getter for the field <code>date</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDate() {
        return date;
    }

    /**
     * <p>
     * Setter for the field <code>date</code>.
     * </p>
     *
     * @param date
     *            a {@link java.lang.String} object.
     */
    public void setDate(final String date) {
        this.date = date;
    }

    /**
     * <p>
     * Getter for the field <code>progress</code>.
     * </p>
     *
     * @return a int.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * <p>
     * Setter for the field <code>progress</code>.
     * </p>
     *
     * @param progress
     *            a int.
     */
    public void setProgress(final int progress) {
        this.progress = progress;
    }

    /**
     * <p>
     * hasProgress.
     * </p>
     *
     * @return a boolean.
     */
    public boolean hasProgress() {
        return progress != -1;
    }

    /**
     * <p>
     * addAttribute.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.Object} object.
     */
    public void addAttribute(final String key, final Object value) {
        attributes.put(key, value);
    }

    /**
     * <p>
     * getAttribute.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object getAttribute(final String key) {
        return attributes.get(key);
    }

    /**
     * <p>
     * Getter for the field <code>attributes</code>.
     * </p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }

        final Event event = (Event) o;

        if (date != null ? !date.equals(event.date) : event.date != null) {
            return false;
        }
        if (desc != null ? !desc.equals(event.desc) : event.desc != null) {
            return false;
        }
        if (exception != null ? !exception.equals(event.exception) : event.exception != null) {
            return false;
        }
        if (key != null ? !key.equals(event.key) : event.key != null) {
            return false;
        }
        if (level != null ? !level.equals(event.level) : event.level != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result;
        result = key != null ? key.hashCode() : 0;
        result = 29 * result + (desc != null ? desc.hashCode() : 0);
        result = 29 * result + (exception != null ? exception.hashCode() : 0);
        result = 29 * result + (level != null ? level.hashCode() : 0);
        result = 29 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Level = " + (getLevel() == null ? "" : getLevel() + " ") + ", Key = "
                + (getKey() == null ? "" : getKey() + " ") + ", Desc = " + (getDesc() == null ? "" : getDesc() + " ")
                + ", Exception = " + (getException() == null ? "" : getException() + " ");
    }

    /**
     * @param t
     * @return
     */
    public static String toString(Throwable t) {
        final StringWriter string = new StringWriter();
        final PrintWriter writer = new PrintWriter(string);

        writer.println(t);
        t.printStackTrace(writer);
        while ((t = t.getCause()) != null) {
            writer.println("Caused by: " + t);
            t.printStackTrace(writer);
        }
        writer.flush();

        return string.toString();
    }
}
