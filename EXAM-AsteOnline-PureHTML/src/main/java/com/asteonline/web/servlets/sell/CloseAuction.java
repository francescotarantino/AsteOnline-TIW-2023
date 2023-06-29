package com.asteonline.web.servlets.sell;

import com.asteonline.web.beans.Auction;
import com.asteonline.web.dao.AuctionDAO;
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
import java.util.Date;

@WebServlet("/CloseAuction")
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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Auction ID is not valid");
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getAuction(auctionId);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't access DB");
            return;
        }

        if(
                auction == null ||
                        auction.getUserID() != userID ||
                        auction.getStatus() != AuctionDAO.AuctionStatus.OPEN ||
                        auction.getDeadline().after(new Date())
        ) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You can't close this auction!");
            return;
        }

        try {
            // Actually close the auction
            auctionDAO.closeAuction(auctionId);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can't access DB");
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/AuctionDetails?auctionID=" + auctionId);
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
