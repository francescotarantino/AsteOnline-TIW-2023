package com.asteonline.js.servlets.sell;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/api/NewAuction")
@MultipartConfig
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
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("No items selected")));
            return;
        }

        // Get deadline date from request and parse it
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date deadline;
        try {
            deadline = simpleDateFormat.parse(req.getParameter("deadline"));
        } catch (ParseException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid deadline date")));
            return;
        }

        int minimumRise;
        try {
            minimumRise = Integer.parseInt(req.getParameter("minimum_rise"));
        } catch (NullPointerException | NumberFormatException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Invalid minimum rise")));
            return;
        }

        AuctionDAO auctionDAO = new AuctionDAO(connection);
        int auctionID;
        try {
            // Add new auction to the database. itemCodes validity check is done in DAO
            auctionID = auctionDAO.newAuction(deadline, minimumRise, userID, itemCodes);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Error while creating the new auction")));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().println("{\"id\": " + auctionID + "}");
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
