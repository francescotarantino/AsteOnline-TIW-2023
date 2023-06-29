package com.asteonline.web.servlets.sell;

import com.asteonline.web.beans.Auction;
import com.asteonline.web.beans.Item;
import com.asteonline.web.beans.Offer;
import com.asteonline.web.dao.AuctionDAO;
import com.asteonline.web.dao.ItemDAO;
import com.asteonline.web.dao.OfferDAO;
import com.asteonline.web.utils.ConnectionHandler;
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
import java.util.List;

@WebServlet("/AuctionDetails")
public class AuctionDetails extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication application;

    public AuctionDetails() {
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

        int auctionID;
        try {
            auctionID = Integer.parseInt(request.getParameter("auctionID"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid auction ID");
            return;
        }

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        OfferDAO offerDAO = new OfferDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);

        Auction auction;
        List<Offer> offers;
        List<Item> items;
        try {
            auction = auctionDAO.getAuction(auctionID);
            offers = offerDAO.getAuctionOffers(auctionID);
            items = itemDAO.getItemsAttachedToAuction(auctionID);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't get data from DB");
            return;
        }

        // Check if the user is the owner of the auction, otherwise he can't see the details in this page
        if(userID != auction.getUserID()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not the owner of this auction");
            return;
        }

        // Get auction details and the offers
        ctx.setVariable("auction", auction);
        ctx.setVariable("items", items);
        ctx.setVariable("offers", offers);

        templateEngine.process("auction_details.html", ctx, response.getWriter());
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
