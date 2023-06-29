package com.asteonline.js.servlets.auth;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serial;

@WebServlet("/Logout")
public class Logout extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    public Logout() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the current session, if it already exists
        HttpSession session = request.getSession(false);
        if(session != null){
            // Invalidate the session
            session.invalidate();
        }

        response.sendRedirect(getServletContext().getContextPath() + "/");
    }
}
