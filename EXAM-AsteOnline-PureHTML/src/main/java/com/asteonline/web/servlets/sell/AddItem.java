package com.asteonline.web.servlets.sell;

import com.asteonline.web.beans.Item;
import com.asteonline.web.dao.ItemDAO;
import com.asteonline.web.utils.ConnectionHandler;
import com.asteonline.web.utils.Constants;
import com.asteonline.web.utils.FileManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/AddItem")
@MultipartConfig
public class AddItem extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public AddItem() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // This servlet is under LoginFilter, so user is logged in for sure
        int userID = (int) req.getSession().getAttribute("user_id");

        // Get and validate code, name and description
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String description = req.getParameter("description");

        if(code == null || code.isEmpty() || name == null || name.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Some fields are missing");
            return;
        }

        if(description.isEmpty()) {
            // Set description to null if empty to avoid storing empty strings in the database
            description = null;
        }

        // Get and parse price
        float price;
        try {
            price = Float.parseFloat(req.getParameter("price"));
        } catch (NullPointerException | NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while parsing float");
            return;
        }

        if (price < 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Price should be equal or greater than 0");
            return;
        }

        // Get uploaded image
        Part imagePart = req.getPart("image");

        if(imagePart == null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The request should contain an image");
            return;
        }

        // Get filename and mimeType
        String filename = code + "_" + imagePart.getSubmittedFileName();
        String mimeType = getServletContext().getMimeType(filename);

        // Validate mimetype
        if(mimeType == null || !mimeType.startsWith("image/")){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Uploaded file is not a valid image");
            return;
        }

        // Store image
        InputStream imageStream = imagePart.getInputStream();
        FileManager.storeFile(getServletContext(), Constants.ITEMS_IMAGE_FOLDER, filename, imageStream);

        // Create a new item bean
        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setDescription(description);
        item.setImagePath(Constants.ITEMS_IMAGE_FOLDER + "/" + filename);
        item.setPrice(price);
        item.setUserID(userID);

        ItemDAO itemDAO = new ItemDAO(connection);
        try {
            itemDAO.addItem(item);
        } catch (SQLException e){
            FileManager.removeFile(getServletContext(), item.getImagePath());

            resp.sendRedirect(getServletContext().getContextPath() + "/Sell?err=item");
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
