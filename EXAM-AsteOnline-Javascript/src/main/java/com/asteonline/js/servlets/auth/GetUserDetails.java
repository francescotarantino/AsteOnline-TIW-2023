package com.asteonline.js.servlets.auth;

import com.asteonline.js.beans.Error;
import com.asteonline.js.beans.User;
import com.asteonline.js.dao.UserDAO;
import com.asteonline.js.utils.ConnectionHandler;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/api/GetUserDetails")
public class GetUserDetails extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetUserDetails() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userID = (int) request.getSession().getAttribute("user_id");

        UserDAO userDAO = new UserDAO(connection);
        User user;
        try {
            user = userDAO.getUserByID(userID);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Not possible to verify user")));
            return;
        }

        if(user == null) {
            // User is not found, it should never happen

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
            // User is found

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(user));
        }
    }

    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
