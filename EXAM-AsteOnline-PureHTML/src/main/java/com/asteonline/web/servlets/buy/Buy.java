package com.asteonline.web.servlets.buy;

import com.asteonline.web.beans.Auction;
import com.asteonline.web.dao.AuctionDAO;
import com.asteonline.web.dao.ItemDAO;
import com.asteonline.web.utils.ConnectionHandler;
import com.asteonline.web.utils.Dates;
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
import java.util.List;

@WebServlet("/Buy")
public class Buy extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication application;

    public Buy() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        int userID = (int) request.getSession().getAttribute("user_id");

        // Get the search query from request
        String search = request.getParameter("q");

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            // If the search query is not empty, search in open auctions
            if(search != null && !search.isEmpty()){
                List<Auction> searchResults = auctionDAO.searchOpenAuctions(search, userID);

                // Calculate human-readable deadline
                searchResults
                        .forEach(x -> x.setHumanReadableDeadline(
                                Dates.timeBetween(new Date(), x.getDeadline())
                        ));

                ctx.setVariable("searchResults", searchResults);
            }

            // Get won auctions by current user
            List<Auction> auctions = auctionDAO.getWonAuctions(userID);

            for (Auction auction : auctions) {
                auction.setItems(itemDAO.getItemsAttachedToAuction(auction.getId()));
            }

            ctx.setVariable("wonAuctions", auctions);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't get data from DB");
            return;
        }

        templateEngine.process("buy.html", ctx, response.getWriter());
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
