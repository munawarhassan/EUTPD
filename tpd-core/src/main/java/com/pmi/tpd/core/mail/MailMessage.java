package com.pmi.tpd.core.mail;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Class representing a mail message.
 */
public class MailMessage {

    private final String from;

    private final Map<String, String> headers;

    private final Set<String> to;

    private final Set<String> cc;

    private final Set<String> bcc;

    private final String text;

    private final String subject;

    private final Set<MailAttachment> attachments;

    public MailMessage(@Nonnull final Set<String> to, @Nullable final String from, @Nullable final Set<String> cc,
            @Nullable final Set<String> bcc, @Nullable final Set<MailAttachment> attachments,
            @Nullable final String text, @Nullable final String subject, @Nullable final Map<String, String> headers) {
        checkArgument(!to.isEmpty(), "One or more \"to\" addresses are required");
        this.from = StringUtils.trimToNull(from);
        this.headers = headers == null ? Collections.<String, String> emptyMap() : ImmutableMap.copyOf(headers);
        this.to = ImmutableSet.copyOf(to);
        this.cc = cc != null ? ImmutableSet.copyOf(cc) : Collections.<String> emptySet();
        this.bcc = bcc != null ? ImmutableSet.copyOf(bcc) : Collections.<String> emptySet();
        this.attachments = attachments != null ? ImmutableSet.copyOf(attachments)
                : Collections.<MailAttachment> emptySet();
        this.text = StringUtils.defaultString(text);
        this.subject = StringUtils.defaultString(subject);
    }

    @Nullable
    public String getFrom() {
        return from;
    }

    @Nonnull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Nonnull
    public Set<String> getTo() {
        return to;
    }

    @Nonnull
    public Set<String> getCc() {
        return cc;
    }

    @Nonnull
    public Set<String> getBcc() {
        return bcc;
    }

    @Nonnull
    public Set<MailAttachment> getAttachments() {
        return attachments;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    @Nonnull
    public String getSubject() {
        return subject;
    }

    public boolean hasBcc() {
        return !bcc.isEmpty();
    }

    public boolean hasCc() {
        return !cc.isEmpty();
    }

    public boolean hasFrom() {
        return from != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MailMessage that = (MailMessage) o;
        return to.equals(that.to) && cc.equals(that.cc) && bcc.equals(that.bcc) && text.equals(that.text)
                && subject.equals(that.subject) && attachments.equals(that.attachments) && headers.equals(that.headers)
                && (from == null ? that.from == null : from.equals(that.from));
    }

    @Override
    public int hashCode() {
        int result = to.hashCode();
        result = 31 * result + cc.hashCode();
        result = 31 * result + bcc.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + attachments.hashCode();
        result = 31 * result + headers.hashCode();
        if (from != null) {
            result = 31 * result + from.hashCode();
        }
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Set<MailAttachment> attachments = new LinkedHashSet<>();

        private final Set<String> bcc = new LinkedHashSet<>();

        private final Set<String> cc = new LinkedHashSet<>();

        private final Map<String, String> headers = new LinkedHashMap<>();

        private final Set<String> to = new LinkedHashSet<>();

        private String from;

        private String subject;

        private String text;

        private void addRecipients(@Nonnull final Set<String> target, final String[] recipients) {
            if (recipients != null) {
                for (final String recipient : recipients) {
                    if (recipient != null) {
                        target.add(recipient);
                    }
                }
            }
        }

        private void addRecipients(@Nonnull final Set<String> target, final Iterable<String> recipients) {
            if (recipients != null) {
                for (final String recipient : recipients) {
                    if (recipient != null) {
                        target.add(recipient);
                    }
                }
            }
        }

        @Nonnull
        public Builder from(final String value) {
            from = value;
            return this;
        }

        @Nonnull
        public Builder to(final String... recipients) {
            addRecipients(to, recipients);
            return this;
        }

        @Nonnull
        public Builder to(final Iterable<String> recipients) {
            addRecipients(to, recipients);
            return this;
        }

        @Nonnull
        public Builder cc(final String... recipients) {
            addRecipients(cc, recipients);
            return this;
        }

        @Nonnull
        public Builder cc(final Iterable<String> recipients) {
            addRecipients(cc, recipients);
            return this;
        }

        @Nonnull
        public Builder bcc(final String... recipients) {
            addRecipients(bcc, recipients);
            return this;
        }

        @Nonnull
        public Builder bcc(final Iterable<String> recipients) {
            addRecipients(bcc, recipients);
            return this;
        }

        @Nonnull
        public Builder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        @Nonnull
        public Builder text(final String text) {
            this.text = text;
            return this;
        }

        @Nonnull
        public Builder attachment(final String fileName, final DataSource source) {
            this.attachments.add(new MailAttachment(fileName, source));
            return this;
        }

        @Nonnull
        public Builder attachment(final String fileName, final File file) {
            return attachment(fileName, new FileDataSource(file));
        }

        @Nonnull
        public Builder attachment(final String fileName, final String pathToFile) {
            return attachment(fileName, new File(pathToFile));
        }

        @Nonnull
        public Builder header(final String key, final String value) {
            headers.put(key, value);
            return this;
        }

        @Nonnull
        public MailMessage build() {
            return new MailMessage(to, from, cc, bcc, attachments, text, subject, headers);
        }
    }
}
