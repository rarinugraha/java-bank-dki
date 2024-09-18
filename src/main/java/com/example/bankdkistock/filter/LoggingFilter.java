package com.example.bankdkistock.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class LoggingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

        logRequest(httpServletRequest);

        chain.doFilter(request, responseWrapper);

        logResponse(responseWrapper);

        writeResponseBack(responseWrapper, (HttpServletResponse) response);
    }

    private void logRequest(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        StringBuilder parameters = new StringBuilder();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            parameters.append(paramName).append("=").append(paramValue).append(", ");
        }

        logger.info("Incoming Request: [Method: {}, URI: {}, Parameters: {}]",
                request.getMethod(), request.getRequestURI(), parameters);
    }

    private void logResponse(ResponseWrapper responseWrapper) throws IOException {
        String responseBody = new String(responseWrapper.getResponseBody(), StandardCharsets.UTF_8);
        logger.info("Outgoing Response: [Status: {}, Body: {}]", responseWrapper.getStatus(), responseBody);
    }

    private void writeResponseBack(ResponseWrapper responseWrapper, HttpServletResponse response) throws IOException {
        byte[] responseBody = responseWrapper.getResponseBody();
        response.setContentLength(responseBody.length);
        response.getOutputStream().write(responseBody);
    }

    @Override
    public void destroy() {
        //
    }
}
