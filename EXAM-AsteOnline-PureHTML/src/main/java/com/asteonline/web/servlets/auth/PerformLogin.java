package com.asteonline.web.servlets.auth;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import com.asteonline.web.utils.ConnectionHandler;
import com.asteonline.web.dao.UserDAO;

@WebServlet("/PerformLogin")
public class PerformLogin extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private JavaxServletWebApplication application;

    public PerformLogin() {
        super();
    }

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());

    	this.application = JavaxServletWebApplication.buildApplication(getServletContext());
    	WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
    	templateResolver.setTemplateMode(TemplateMode.HTML);

    	this.templateEngine = new TemplateEngine();
    	this.templateEngine.setTemplateResolver(templateResolver);
    }
    
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String usernameOrEmail, password;

		usernameOrEmail = request.getParameter("usernameOrEmail");
		password = request.getParameter("password");
		
		if(usernameOrEmail == null || usernameOrEmail.isEmpty() || password == null || password.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Some fields are missing");
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		int userID;
		try {
			userID = userDAO.authenticateUser(usernameOrEmail, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to verify user");
			return;
		}

		if(userID == -1) {
			// User is not found
			final IWebExchange webExchange = this.application.buildExchange(request, response);
			final WebContext ctx = new WebContext(webExchange, request.getLocale());

			ctx.setVariable("errorMessage", "Utente non trovato");

			templateEngine.process("index.html", ctx, response.getWriter());
		} else {
			// User is found
			request.getSession().setAttribute("user_id", userID);
			request.getSession().setAttribute("login_timestamp", new Date());
			response.sendRedirect(getServletContext().getContextPath() + "/Home");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Redirect to the login page
		response.sendRedirect(getServletContext().getContextPath() + "/");
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
