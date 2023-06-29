package com.asteonline.js.servlets.sell;

import com.asteonline.js.beans.Auction;
import com.asteonline.js.beans.Error;
import com.asteonline.js.dao.AuctionDAO;
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
import java.util.Date;

@WebServlet("/api/CloseAuction")
@MultipartConfig
public class CloseAuction extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public CloseAuction() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userID = (int) req.getSession().getAttribute("user_id");

        AuctionDAO auctionDAO = new AuctionDAO(connection);

        int auctionId;
        try {
            auctionId = Integer.parseInt(req.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Auction ID is not valid")));
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getAuction(auctionId);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Cannot retrieve auction details")));
            return;
        }

        if(
                auction == null ||
                        auction.getUserID() != userID ||
                        auction.getStatus() != AuctionDAO.AuctionStatus.OPEN ||
                        auction.getDeadline().after(new Date())
        ) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("You can't close this auction!")));
            return;
        }

        try {
            // Actually close the auction
            auctionDAO.closeAuction(auctionId);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(new Gson().toJson(new Error("Can't close this auction.")));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
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
