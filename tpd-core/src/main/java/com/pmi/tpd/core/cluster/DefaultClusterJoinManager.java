package com.pmi.tpd.core.cluster;

import static com.pmi.tpd.core.event.advisor.EventAdvisorService.EVENT_TYPE_NODE_PASSIVATED;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.html.HtmlEscapers;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.cluster.BaseClusterJoinManager;
import com.pmi.tpd.cluster.IClusterJoinCheck;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultClusterJoinManager extends BaseClusterJoinManager {

    /** */
    private static final Function<String, String> HTML_ESCAPE = value -> HtmlEscapers.htmlEscaper().escape(value);

    /** */
    private final IEventAdvisorService<?> eventAdvisorService;

    @Inject
    public DefaultClusterJoinManager(final IClock clock, final IEventAdvisorService<?> eventAdvisorService,
            final IClusterJoinCheck... joinChecks) {
        super(clock, joinChecks);
        this.eventAdvisorService = eventAdvisorService;
    }

    @Override
    protected boolean isSystemUnavailable() {
        final String highestLevel = eventAdvisorService.findHighestEventLevel();
        // only ERROR and FATAL are reasons to not accept connections
        return highestLevel != null && eventAdvisorService.isLevelAtLeast(highestLevel, EventLevel.ERROR);
    }

    @Override
    protected void passivateNode(final List<String> issues) {
        // Unfortunately, we can't use AddEvent and the EventPublisher here. Join checks are called too early
        // in spring lifecycle and the AddEvent listener won't be registered.
        final String error = "The current node is unable to safely connect to the cluster and will not service requests"
                + "<ul><li>" + StringUtils.join(Collections2.transform(issues, HTML_ESCAPE), "</li><li>")
                + "</li></ul>";

        eventAdvisorService
                .publishEvent(new Event(eventAdvisorService.getEventType(EVENT_TYPE_NODE_PASSIVATED).orElse(null),
                        error, eventAdvisorService.getEventLevel(EventLevel.ERROR).orElse(null)));
    }

}
