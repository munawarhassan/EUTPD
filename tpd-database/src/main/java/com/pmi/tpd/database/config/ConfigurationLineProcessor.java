package com.pmi.tpd.database.config;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.google.common.io.LineProcessor;
import com.pmi.tpd.api.util.Assert;

/**
 * LineProcessor that is injected with the writer used to write the new version of the file. Delegates actual
 * modifications to the amendment.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ConfigurationLineProcessor implements LineProcessor<Void> {

    @Nonnull
    private final Writer writer;

    /**
     * The payload of property values and comments that this line processor will write to the writer.
     */
    private final IConfigurationAmendment amendment;

    public ConfigurationLineProcessor(@Nonnull final Writer writer, @Nonnull final IConfigurationAmendment amendment) {
        this.writer = Assert.checkNotNull(writer, "writer");
        this.amendment = Assert.checkNotNull(amendment, "amendment");
    }

    @Override
    public boolean processLine(@Nonnull final String line) throws IOException {
        if (amendment.isAmendable(line)) {
            amendment.amend(writer, line);
        } else {
            writeLine(line);
        }

        return true;
    }

    /**
     * @return {@code true} if the output has been amended while processing it. {@code false} otherwise
     */
    @SuppressWarnings("null")
    @Override
    @Nonnull
    public Void getResult() {
        return null;
    }

    /**
     * Processes a property line that we don't care about.
     */
    private void writeLine(@Nonnull final String line) throws IOException {
        writer.write(line);
        writer.write('\n');
    }
}
