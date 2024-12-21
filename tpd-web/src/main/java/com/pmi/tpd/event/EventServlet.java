package com.pmi.tpd.event;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class EventServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private static final String PAGE_FATAL_ERROR = "/fatal.jsp";

    /** */
    public static final String PAGE_MAINTENANCE = "/maintenance"; // Can't use /system/maintenance here
                                                                  // (yet)

    /** */
    private static final String PAGE_SYSTEM_MAINTENANCE = "/maintenance/lock.html";

    /** */
    private static final Map<String, String> LEVELS_TO_PAGES = ImmutableMap.<String, String> builder()
            .put(EventAdvisorService.LEVEL_MAINTENANCE, PAGE_MAINTENANCE)
            .put(EventAdvisorService.LEVEL_SYSTEM_MAINTENANCE, PAGE_SYSTEM_MAINTENANCE)
            .build();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final IEventContainer eventContainer = ServletEventAdvisor.getInstance().getEventContainer(getServletContext());
        final String highestEventLevel = EventAdvisorService.getInstance(eventContainer).findHighestEventLevel();
        if (highestEventLevel == null) {
            if (LifecycleUtils.isStarting(getServletContext())) {
                response.sendRedirect(request.getContextPath());
            }
        } else if (LEVELS_TO_PAGES.containsKey(highestEventLevel)) {
            // If the system has an explicit mapping for the level, dispatch to the mapped page
            request.getRequestDispatcher(LEVELS_TO_PAGES.get(highestEventLevel)).forward(request, response);
        } else {
            // Otherwise, show the fatal error page
            request.getRequestDispatcher(PAGE_FATAL_ERROR).forward(request, response);
        }
    }
}
