package com.asteonline.js.filters;

import com.asteonline.js.beans.Error;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(
        description = "Check if the user is logged in",
        urlPatterns = {"/Logout", "/api/*", "/GetImage/*"}
)
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // Cast the servlet request and response to HTTP servlet request and response
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Get the current session
        HttpSession session = request.getSession();
        // Check if the session is new or if the user attribute is null
        if(session.isNew() || session.getAttribute("user_id") == null){
            // Send an error message
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("You are not logged in.")));
            return;
        }

        // Continue the filter chain
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
