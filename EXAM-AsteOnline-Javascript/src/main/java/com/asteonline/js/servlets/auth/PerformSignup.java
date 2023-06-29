package com.asteonline.js.servlets.auth;

import com.asteonline.js.beans.Error;
import com.asteonline.js.beans.User;
import com.asteonline.js.dao.UserDAO;
import com.asteonline.js.utils.ConnectionHandler;
import com.asteonline.js.utils.Constants;
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

@WebServlet("/PerformSignup")
@MultipartConfig
public class PerformSignup extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public PerformSignup() {
        super();
    }

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get parameters from request
		String username, email, firstname, lastname, password, passwordConfirm, shippingAddress;
		username = request.getParameter("username");
		email = request.getParameter("email");
		firstname = request.getParameter("firstname");
		lastname = request.getParameter("lastname");
		password = request.getParameter("password");
		passwordConfirm = request.getParameter("password_confirm");
		shippingAddress = request.getParameter("shipping_address");

		// Check if all required fields are present
		if (username == null || username.isEmpty() || firstname == null || firstname.isEmpty() || lastname == null || lastname.isEmpty() || password == null || password.isEmpty() || passwordConfirm == null || passwordConfirm.isEmpty() || shippingAddress == null || shippingAddress.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Some required fields are missing")));
			return;
		}

		// Check if the email is valid
		if(email != null && !email.isEmpty()){
			if(!email.matches(Constants.EMAIL_VALIDATION_REGEX)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("application/json");
				response.getWriter().write(new Gson().toJson(new Error("Email is malformed")));
				return;
			}
		} else {
			email = null;
		}

		// Check if the username is at least 3 characters long
		if(username.length() < 3){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Username is too short")));
			return;
		}

		// Check if the password is at least 8 characters long
		if(password.length() < 8){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Password is too short")));
			return;
		}

		// Check if passwords match, if not, return an error to the user
		if (!password.equals(passwordConfirm)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Passwords don't match")));
			return;
		}

		UserDAO userDAO = new UserDAO(connection);

		try {
			// Check if the username is already taken, if so, return an error to the user
			if(userDAO.isUsernameTaken(username)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("application/json");
				response.getWriter().write(new Gson().toJson(new Error("Username is already taken")));
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("application/json");
			response.getWriter().write(new Gson().toJson(new Error("Can't check if username is taken")));
			return;
		}

		User user = new User();

		user.setUsername(username);
		user.setEmail(email);
		user.setFirstname(firstname);
		user.setLastname(lastname);
		user.setShippingAddress(shippingAddress);

		try {
			int id = userDAO.createUser(user, password);
			user.setId(id);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		}

		// Set session attributes
		request.getSession().setAttribute("user_id", user.getId());

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().write(new Gson().toJson(user));
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
