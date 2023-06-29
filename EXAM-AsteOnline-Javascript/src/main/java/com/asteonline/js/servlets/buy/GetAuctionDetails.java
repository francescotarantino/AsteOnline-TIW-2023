package com.asteonline.js.servlets.buy;

import com.asteonline.js.beans.Auction;
import com.asteonline.js.beans.Error;
import com.asteonline.js.beans.Item;
import com.asteonline.js.dao.AuctionDAO;
import com.asteonline.js.dao.ItemDAO;
import com.asteonline.js.dao.OfferDAO;
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
import java.util.List;

@WebServlet("/api/GetAuctionDetails")
public class GetAuctionDetails extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetAuctionDetails() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userID = (int) request.getSession().getAttribute("user_id");

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean shortVersion = false;
        if(request.getParameter("short") != null){
            shortVersion = Boolean.parseBoolean(request.getParameter("short"));
        }

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        Auction auction;
        List<Item> items;
        try {
            auction = auctionDAO.getAuction(id);

            if(auction.getStatus() == AuctionDAO.AuctionStatus.CLOSED) {
                if(auction.getWonByUserID() != userID && auction.getUserID() != userID) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(new Gson().toJson(new Error("You are not allowed to see this auction")));
                    return;
                }
            }

            items = itemDAO.getItemsAttachedToAuction(auction.getId());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Cannot retrieve auction")));
            return;
        }

        if(!shortVersion) {
            OfferDAO offerDAO = new OfferDAO(connection);

            try {
                auction.setOffers(offerDAO.getAuctionOffers(auction.getId()));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(new Error("Cannot retrieve auction details")));
                return;
            }

            auction.setItems(items);
        } else {
            auction.setItemsNames(
                    items.stream()
                            .map(Item::getName)
                            .toList()
            );
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(auction));
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
