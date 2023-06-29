package com.asteonline.web.servlets.sell;

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

@WebServlet("/Sell")
public class Sell extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JavaxServletWebApplication application;

    public Sell() {
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

        String err = request.getParameter("err");
        if(err != null){
            switch (err) {
                case "auction" -> ctx.setVariable("newAuctionError", "Devi selezionare almeno un oggetto dalla lista!");
                case "item" -> ctx.setVariable("addItemError", "Errore durante l'aggiunta dell'oggetto nel database");
            }
        }

        // Get user data from session
        int userID = (int) request.getSession().getAttribute("user_id");
        Date loginTimestamp = (Date) request.getSession().getAttribute("login_timestamp");

        ItemDAO itemDAO = new ItemDAO(connection);
        AuctionDAO auctionDAO = new AuctionDAO(connection);

        try {
            // Get user items available for auction
            ctx.setVariable("availableItems", itemDAO.getItemsAvailableForAuction(userID));

            // Get open user auctions
            List<Auction> openAuctions = auctionDAO.openUserAuctions(userID);

            for (Auction auction : openAuctions) {
                auction.setItems(itemDAO.getItemsAttachedToAuction(auction.getId()));
                // Set human-readable deadline for each auction
                auction.setHumanReadableDeadline(Dates.timeBetween(loginTimestamp, auction.getDeadline()));
            }

            ctx.setVariable("openAuctions", openAuctions);

            // Get closed user auctions
            List<Auction> closedAuctions = auctionDAO.closedUserAuctions(userID);

            for (Auction auction : closedAuctions) {
                auction.setItems(itemDAO.getItemsAttachedToAuction(auction.getId()));
            }

            ctx.setVariable("closedAuctions", closedAuctions);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't get data from DB");
            return;
        }

        templateEngine.process("sell.html", ctx, response.getWriter());
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
