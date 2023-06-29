package com.asteonline.web.servlets.auth;

import com.asteonline.web.beans.User;
import com.asteonline.web.dao.UserDAO;
import com.asteonline.web.utils.ConnectionHandler;
import com.asteonline.web.utils.Constants;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/PerformSignup")
public class PerformSignup extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication application;

    public PerformSignup() {
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
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        boolean errors = false;

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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Some required fields are missing");
            return;
        }

        // Check if the email is valid
        if(email != null && !email.isEmpty()){
            if(!email.matches(Constants.EMAIL_VALIDATION_REGEX)){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email is malformed");
                return;
            }
        } else {
            email = null;
        }

        // Check if the username is at least 3 characters long
        if(username.length() < 3){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username is too short");
            return;
        }

        // Check if the password is at least 8 characters long
        if(password.length() < 8){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Password is too short");
            return;
        }

        // Check if passwords match, if not, return an error to the user
        if (!password.equals(passwordConfirm)) {
            ctx.setVariable("confirmPasswordError", "Le password non coincidono");
            errors = true;
        }

        UserDAO userDAO = new UserDAO(connection);

        try {
            // Check if the username is already taken, if so, return an error to the user
            if(userDAO.isUsernameTaken(username)){
                ctx.setVariable("usernameError", "Username già in uso");
                errors = true;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't check if username is taken");
            return;
        }

        if(errors){
            templateEngine.process("signup.html", ctx, response.getWriter());
        } else {
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
            request.getSession().setAttribute("login_timestamp", new Date());
            response.sendRedirect(getServletContext().getContextPath() + "/Home");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Redirect to the signup page
        response.sendRedirect(getServletContext().getContextPath() + "/signup.html");
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
