package com.pmi.tpd.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventContainerServlet extends HttpServlet {

    public static final String PATH = "/system/events";

    /**
     *
     */
    private static final long serialVersionUID = 870234915600606406L;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final IEventContainer appEventContainer = ServletEventAdvisor.getInstance()
                .getEventContainer(getServletContext());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        JSON.writeValue(response.getOutputStream(), appEventContainer.getEvents());
    }

}
