package com.pmi.tpd.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleState;
import com.pmi.tpd.core.event.advisor.spring.lifecycle.LifecycleUtils;
import com.pmi.tpd.startup.IStartupManager;
import com.pmi.tpd.startup.StartupUtils;
import com.pmi.tpd.web.rest.model.Progress;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class StartupProgressServlet extends HttpServlet {

    public static final String PATH = "/system/progress";

    /**
     *
     */
    private static final long serialVersionUID = 870234915600606406L;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final IStartupManager startupManager = getStartupManager();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        JSON.writeValue(response.getOutputStream(), new Progress(startupManager.getProgress(), getCurrentState()));
    }

    @VisibleForTesting
    protected LifecycleState getCurrentState() {
        return LifecycleUtils.getCurrentState(getServletContext());
    }

    @VisibleForTesting
    protected IStartupManager getStartupManager() {
        return StartupUtils.getStartupManager(getServletContext());
    }

}
