package com.xqdd.highlightword.filter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@ServletComponentScan
@WebFilter
public class CrossDomainFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,HEAD,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "range,Origin,Accept, X-Requested-With, Content-Type,Authorization");
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.getOutputStream().write("OK".getBytes(StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }

}
