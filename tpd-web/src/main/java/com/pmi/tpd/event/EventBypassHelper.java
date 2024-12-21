package com.pmi.tpd.event;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Conventions;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.config.IEventConfig;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.maintenance.event.SystemMaintenanceEvent;
import com.pmi.tpd.event.filter.BypassableEventFilter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class EventBypassHelper {

    /**
     * The attribute that will be added to the {@code ServletRequest} to indicate bypass logic has executed. Its value
     * will be a {@code Boolean} indicating whether Event-Application can or cannot be bypassed.
     */
    public static final String ATTR_BYPASSED = Conventions.getQualifiedAttributeName(BypassableEventFilter.class,
        "bypassed");

    /**
     * The header used to provide the maintenance token in order to bypass processing for the request.
     */
    public static final String HEADER_TOKEN = "X-Maintenance-Token";

    private EventBypassHelper() {
        throw new UnsupportedOperationException(getClass().getName() + " is a utility class");
    }

    /**
     * Determines whether processing should be bypassed for the provided {@code ServletRequest}.
     * <p>
     * This functionality is used to provide, in essence, single-user mode. A {@link SystemMaintenanceEvent} may be
     * added to the {@link IEventContainer}, defining a special token which may be used to access the system where would
     * otherwise deny access.
     * <p>
     * If <i>multiple</i> events are in the container, even if the correct token is supplied it will not be possible to
     * bypass. This is done intentionally, as other forms of maintenance such as taking backups may be taken during
     * system maintenance, and such operations may prevent database access.
     * <p>
     * Note: Bypassing does <i>not</i> bypass authentication. Valid user credentials must still be supplied with every
     * request, even if a valid bypass token is supplied.
     *
     * @param request
     *            the servlet request
     * @return {@code true} if event processing should be bypassed for the request; otherwise, {@code false}
     */
    public static boolean isBypassed(final ServletRequest request) {
        final Object checked = request.getAttribute(ATTR_BYPASSED);
        if (checked instanceof Boolean) {
            return (Boolean) checked;
        }

        final HttpServletRequest http = (HttpServletRequest) request;
        final ServletContext servletContext = http.getServletContext();

        final boolean bypassed = isIgnored(http, servletContext) || isBypassed(http, servletContext);
        http.setAttribute(ATTR_BYPASSED, bypassed);

        return bypassed;
    }

    public static String getFullURL(final HttpServletRequest request) {
        final StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        final String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    /**
     * Examines the provided {@code request} to determine if it should bypass event processing. Bypassing event is
     * enabled only during {@link SystemMaintenanceEvent system maintenance}, and only if the correct token is supplied
     * in a {@link #HEADER_TOKEN custom header}.
     * <p>
     * If multiple events are in the container, even if one of them is the {@link SystemMaintenanceEvent} and the
     * correct token is supplied, bypassing is still <i>not</i> supported. Because other forms of maintenance may mean
     * the database is inaccessible, bypassing would likely result in other failures, or in requests hanging because the
     * database is unavailable.
     *
     * @param request
     *            the incoming request
     * @param servletContext
     *            the servlet context for the request
     * @return {@code true} if {@link SystemMaintenanceEvent system maintenance} is in progress and the correct token
     *         was supplied; otherwise, {@code false} if the token is missing, wrong or multiple events exist
     */
    private static boolean isBypassed(final HttpServletRequest request, final ServletContext servletContext) {
        final IEventContainer eventContainer = ServletEventAdvisor.getInstance().getEventContainer(servletContext);

        if (eventContainer.hasEvents()) {
            // This request _would_ be blocked by Event-Application. Now, should we bypass that?
            final Collection<Event> events = eventContainer.getEvents();
            final Iterator<Event> iterator = events.iterator();

            // Question 1: Is a system maintenance event the _only_ event in the container?
            //
            // Note: If a backup is taken or a migration is performed, a different type of MaintenanceEvent will be
            // added. If either is done _during_ system maintenance, there will be _two_ events; one for the
            // system maintenance and one for backup/migration processing. That will prevent bypassing, even
            // if the right token is supplied. That is _intentional_; if the database is unavailable, users
            // cannot be authenticated and event is used to "protect" the system while it is in that state.
            if (iterator.hasNext()) {
                final Event event = iterator.next();
                if (event instanceof SystemMaintenanceEvent && !iterator.hasNext()) {
                    final SystemMaintenanceEvent maintenanceEvent = (SystemMaintenanceEvent) event;

                    // Question 2: Is the token the right one?
                    return maintenanceEvent.isToken(request.getHeader(HEADER_TOKEN));
                }
            }

            return false;
        } else {
            // As an optimisation over the rest of the event processing, given that we've already looked at the
            // container, if there are no events directly allow the request through and bypass the normal logic.
            return true;
        }
    }

    /**
     * Examines the provided {@code request} to determine if event has been configured to ignore it.
     * <p>
     * Event has logic for this internally already, in {@code AbstractServletEventFilter}, but that logic only looks at
     * the servlet path. When SpringMVC is used, however, the servlet path is always the path to the dispatcher servlet.
     * The full request path (taken here as the concatenation of the servlet path and the trailing path info) should be
     * checked instead.
     * <p>
     * Note: This logic is here as a workaround. Ideally, Event should be handling this itself, internally.
     * Additionally, it is possible that Event will still decide the path should be ignored during its own processing,
     * later, in {@code AbstractServletEventFilter}. The check it applies is not duplicated fully here.
     *
     * @param request
     *            the incoming request
     * @param servletContext
     *            the servlet context for the request
     * @return {@code true} if event has been configured to ignore the request path; otherwise, {@code false}
     */
    public static boolean isIgnored(final HttpServletRequest request, final ServletContext servletContext) {
        final IEventConfig config = EventAdvisorService.getConfig(servletContext);

        return config.isIgnoredPath(
            StringUtils.defaultString(request.getServletPath()) + StringUtils.defaultString(request.getPathInfo()));
    }
}
