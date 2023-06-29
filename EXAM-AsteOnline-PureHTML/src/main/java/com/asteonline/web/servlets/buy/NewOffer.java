package com.asteonline.web.servlets.buy;

import com.asteonline.web.beans.Auction;
import com.asteonline.web.dao.AuctionDAO;
import com.asteonline.web.dao.OfferDAO;
import com.asteonline.web.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/NewOffer")
public class NewOffer extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public NewOffer() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userID = (int) req.getSession().getAttribute("user_id");

        // Get and parse parameters
        int auctionID;
        float offer;
        try {
            auctionID = Integer.parseInt(req.getParameter("auction_id"));
            offer = Float.parseFloat(req.getParameter("offer"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid auction ID or offer");
            return;
        }

        OfferDAO offerDAO = new OfferDAO(connection);
        AuctionDAO auctionDAO = new AuctionDAO(connection);

        try {
            Auction auction = auctionDAO.getAuction(auctionID);

            if(auction.isExpired()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Auction is expired");
                return;
            }

            if (auction.getUserID() == userID){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't offer on your own auction");
                return;
            }

            if(offer < auction.getMinBid()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Offer must be greater than current max offer + minimum rise");
                return;
            }

            offerDAO.addOffer(auctionID, userID, offer);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't create offer");
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/Offer?auctionID=" + auctionID);
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
