package com.pmi.tpd.core.event.advisor.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;

/**
 * A handler that returns a no-content temporarily unavailable response suitable for refusing responses when an
 * application is unable to handle normal requests. This is especially useful for cases where the normal response is of
 * an unknown, or dynamic content-type and sending actual content may confuse clients.
 * <p/>
 * Example uses include AJAX requests, generated images, pdf, excel and word docs.
 */
public class Servlet503EventFilter extends AbstractServletEventFilter {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(Servlet503EventFilter.class);

    /**
     * @param appEventContainer
     * @param servletRequest
     * @param servletResponse
     * @throws IOException
     */
    @Override
    protected void handleError(final IEventContainer appEventContainer,
        final HttpServletRequest servletRequest,
        final HttpServletResponse servletResponse) throws IOException {
        LOGGER.info("The application is unavailable, or there are errors.  Returing a temporarily unavailable status.");
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        if (hasOnlyWarnings(appEventContainer)) {
            servletResponse.setHeader("Retry-After", "30");
        }
        // flushing the writer stops the app server from putting its html message into the otherwise empty response
        servletResponse.getWriter().flush();
    }

    @Override
    protected void handleNotSetup(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse)
            throws IOException {
        LOGGER.info("The application is not setup.  Returing a temporarily unavailable status.");
        servletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        // flushing the writer stops the app server from putting its html message into the otherwise empty response
        servletResponse.getWriter().flush();
    }

    private boolean hasOnlyWarnings(final IEventContainer eventContainer) {
        if (eventContainer != null && eventContainer.hasEvents()) {
            final Collection<Event> events = eventContainer.getEvents();
            return allEventsAreWarnings(events);
        } else {
            return false;
        }
    }

    private boolean allEventsAreWarnings(final Collection<Event> events) {
        for (final Object eventObject : events) {
            if (eventObject instanceof Event) {
                if (hasNonWarningLevel((Event) eventObject)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean hasNonWarningLevel(final Event event) {
        return !EventLevel.WARNING.equals(event.getLevel().getLevel());
    }
}
