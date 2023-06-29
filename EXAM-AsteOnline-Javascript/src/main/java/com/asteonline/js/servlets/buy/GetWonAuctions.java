package com.asteonline.js.servlets.buy;

import com.asteonline.js.beans.Auction;
import com.asteonline.js.beans.Error;
import com.asteonline.js.dao.AuctionDAO;
import com.asteonline.js.dao.ItemDAO;
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

@WebServlet("/api/GetWonAuctions")
public class GetWonAuctions extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetWonAuctions() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userID = (int) request.getSession().getAttribute("user_id");

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            List<Auction> auctions = auctionDAO.getWonAuctions(userID);

            for (Auction auction : auctions) {
                auction.setItems(itemDAO.getItemsAttachedToAuction(auction.getId()));
            }

            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(auctions));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new Error("Cannot retrieve won auctions")));
            return;
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
