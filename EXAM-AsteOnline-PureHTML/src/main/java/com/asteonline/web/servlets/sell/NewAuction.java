package com.asteonline.web.servlets.sell;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/NewAuction")
public class NewAuction extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public NewAuction() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int userID = (int) req.getSession().getAttribute("user_id");

        // Get item codes from request
        String[] itemCodes = req.getParameterValues("items");
        if (itemCodes == null || itemCodes.length == 0){
            resp.sendRedirect(getServletContext().getContextPath() + "/Sell?err=auction");
            return;
        }

        // Get deadline date from request and parse it
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date deadline;
        try {
            deadline = simpleDateFormat.parse(req.getParameter("deadline"));
        } catch (ParseException | NullPointerException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format");
            return;
        }

        int minimumRise;
        try {
            minimumRise = Integer.parseInt(req.getParameter("minimum_rise"));
        } catch (NullPointerException | NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while parsing integer");
            return;
        }

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        try {
            // Add new auction to database. itemCodes validity check is done in DAO
            auctionDAO.newAuction(deadline, minimumRise, userID, itemCodes);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating auction");
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/Sell");
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
