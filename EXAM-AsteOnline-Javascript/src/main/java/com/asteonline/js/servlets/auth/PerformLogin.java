package com.asteonline.js.servlets.auth;

import com.asteonline.js.beans.Error;
import com.asteonline.js.beans.User;
import com.asteonline.js.dao.UserDAO;
import com.asteonline.js.utils.ConnectionHandler;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/PerformLogin")
@MultipartConfig
public class PerformLogin extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public PerformLogin() {
        super();
    }

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String usernameOrEmail, password;

		usernameOrEmail = request.getParameter("usernameOrEmail");
		password = request.getParameter("password");
		
		if(usernameOrEmail == null || usernameOrEmail.isEmpty() || password == null || password.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Missing credentials")));
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		User user;
		try {
			user = userDAO.authenticateUser(usernameOrEmail, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Not possible to verify user")));
			return;
		}

		if(user == null) {
			// User is not found

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Incorrect username or password")));
		} else {
			// User is found
			request.getSession().setAttribute("user_id", user.getId());

			response.setStatus(HttpServletResponse.SC_OK);
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
