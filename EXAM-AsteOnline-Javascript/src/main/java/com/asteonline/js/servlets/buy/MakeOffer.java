package com.asteonline.js.servlets.buy;

import com.asteonline.js.beans.Auction;
import com.asteonline.js.beans.Error;
import com.asteonline.js.dao.AuctionDAO;
import com.asteonline.js.dao.OfferDAO;
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

@WebServlet("/api/MakeOffer")
@MultipartConfig
public class MakeOffer extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public MakeOffer() {
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
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid auction ID or offer")));
            return;
        }

        OfferDAO offerDAO = new OfferDAO(connection);
        AuctionDAO auctionDAO = new AuctionDAO(connection);

        try {
            Auction auction = auctionDAO.getAuction(auctionID);

            if(auction.isExpired()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Auction is expired")));
                return;
            }

            if (auction.getUserID() == userID){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("You can't make an offer on your own auction")));
                return;
            }

            if(offer < auction.getMinBid()){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().println(new Gson().toJson(new Error("Offer must be greater than current max offer + minimum rise")));
                return;
            }

            offerDAO.addOffer(auctionID, userID, offer);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Can't create offer")));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");

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
