package com.pmi.tpd.core.event.advisor.servlet;

import static org.mockito.ArgumentMatchers.same;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.SimpleSetupConfig;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ServletEventFilterTest extends MockitoTestCase {

    private static final String ALREADY_FILTERED = ServletEventFilter.class.getName() + "_already_filtered";

    private static IEventContainer container;

    @Mock
    private FilterChain filterChain;

    @Mock(lenient = true)
    private FilterConfig filterConfig;

    private ServletEventFilter filter;

    @Mock(lenient = true)
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletContext servletContext;

    @AfterAll
    public static void clearConfig() {
        EventAdvisorService.getInstance().terminate();
    }

    @BeforeAll
    public static void setConfig() {
        EventAdvisorService.initialize(getResourceAsStream(ServletEventFilterTest.class, "test-event-config.xml"));

        container = EventAdvisorService.getInstance().getEventContainer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        SimpleSetupConfig.IS_SETUP = false;

        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(request.getContextPath()).thenReturn("");

        // Ensure each test starts with an empty container
        for (final Event event : container.getEvents()) {
            container.discardEvent(event);
        }

        filter = new ServletEventFilter();
        filter.init(filterConfig);
    }

    @Test
    public void testErrorNotIgnorableURI() throws Exception {
        container.publishEvent(
            new Event(ServletEventAdvisor.getInstance().getEventType("database").orElseThrow(), "foo"));

        when(request.getRequestURI()).thenReturn("somepage.jsp");
        when(request.getServletPath()).thenReturn("somepage.jsp");
        when(request.getQueryString()).thenReturn("");

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(eq("/the/error/path.jsp?next=somepage.jsp"));
    }

    @Test
    public void testErrorsIgnorableUri() throws Exception {
        container.publishEvent(
            new Event(ServletEventAdvisor.getInstance().getEventType("database").orElseThrow(), "foo"));

        when(request.getServletPath()).thenReturn("/the/error/path.jsp");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testNoErrorsAndNotSetup() throws Exception {
        when(request.getServletPath()).thenReturn("somepage.jsp");

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(eq("/the/setup/path.jsp"));
    }

    @Test
    public void testNoErrorsSetup() throws Exception {
        SimpleSetupConfig.IS_SETUP = true;

        when(request.getServletPath()).thenReturn("somepage.jsp");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testNoErrorsSetupButSetupURI() throws Exception {
        SimpleSetupConfig.IS_SETUP = true;

        when(request.getServletPath()).thenReturn("/setuppage.jsp");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testNoErrorsNotSetupButSetupURI() throws Exception {
        when(request.getServletPath()).thenReturn("/setup.jsp");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testNoErrorsNotSetupIgnorableURI() throws Exception {
        when(request.getServletPath()).thenReturn("/ignore/path/1.jsp");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testNoErrorsNotSetupNotIgnorableURI() throws Exception {
        when(request.getServletPath()).thenReturn("/somepage.jsp");

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(eq("/the/setup/path.jsp"));
    }

    @Test
    public void testNotAppliedTwice() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/somepage.jsp");

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(eq("/the/setup/path.jsp"));

        reset(filterChain, request);
        when(request.getAttribute(eq(ALREADY_FILTERED))).thenReturn(Boolean.TRUE);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
    }

    @Test
    public void testEmptyServletPaths() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/requestpage.jsp");
        when(request.getPathInfo()).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(eq("/the/setup/path.jsp"));
    }

    @Test
    public void testGetStringForEvents() {
        final EventType type = new EventType("mytype", "mydesc");
        final Event[] eventsArr = new Event[] { new Event(type, "Error 1"), new Event(type, "Error 2") };
        final String stringForEvents = filter.getStringForEvents(Arrays.asList(eventsArr));
        assertEquals("Error 1\nError 2", stringForEvents);
    }

    @Test
    public void testDestinationPathWithQueryParams() throws Exception {
        container.publishEvent(
            new Event(ServletEventAdvisor.getInstance().getEventType("database").orElseThrow(), "foo"));

        when(request.getRequestURI()).thenReturn("/the/page/we/want");
        when(request.getQueryString()).thenReturn("query=somequery&query2=somequery2");

        filter.doFilter(request, response, filterChain);

        verify(response).sendRedirect(
            eq("/the/error/path.jsp?next=%2Fthe%2Fpage%2Fwe%2Fwant%3Fquery%3Dsomequery%26query2%3Dsomequery2"));
    }
}
