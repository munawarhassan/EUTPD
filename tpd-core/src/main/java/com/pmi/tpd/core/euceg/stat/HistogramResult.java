package com.pmi.tpd.core.euceg.stat;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
@JsonSerialize
@JsonDeserialize(builder = HistogramResult.HistogramResultBuilder.class)
public class HistogramResult {

    /** */
    @Singular("series")
    private List<String> series;

    /** */
    @Singular("data")
    private Map<String, List<Long>> data;

    @JsonPOJOBuilder(withPrefix = "")
    public static class HistogramResultBuilder {

        public HistogramResultBuilder addData(final String name, final Long data) {
            if (this.data$key == null) {
                this.data$key = Lists.newArrayList();
                this.data$value = Lists.newArrayList();
            }
            final int index = this.data$key.indexOf(name);
            if (index > -1) {
                this.data$value.get(index).add(data);
            } else {
                this.data$key.add(name);
                this.data$value.add(Lists.newArrayList(data));
            }
            return this;
        }
    }

}
