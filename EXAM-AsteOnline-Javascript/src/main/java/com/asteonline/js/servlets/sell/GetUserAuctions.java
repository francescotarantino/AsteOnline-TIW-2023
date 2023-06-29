package com.asteonline.js.servlets.sell;

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

@WebServlet("/api/GetUserAuctions")
public class GetUserAuctions extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userID = (int) req.getSession().getAttribute("user_id");

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            List<Auction> auctions = auctionDAO.getUserAuctions(userID);

            for (Auction auction : auctions) {
                auction.setItems(itemDAO.getItemsAttachedToAuction(auction.getId()));
            }

            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(auctions));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Cannot retrieve won auctions")));
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
