package com.asteonline.js.servlets.sell;

import com.asteonline.js.beans.Error;
import com.asteonline.js.beans.Item;
import com.asteonline.js.dao.ItemDAO;
import com.asteonline.js.utils.ConnectionHandler;
import com.asteonline.js.utils.Constants;
import com.asteonline.js.utils.FileManager;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/api/AddItem")
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
        int userID = (int) req.getSession().getAttribute("user_id");

        // Get and validate code, name and description
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String description = req.getParameter("description");

        if(code == null || code.isEmpty() || name == null || name.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Some fields are missing")));
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
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Price is not valid")));
            return;
        }

        if (price < 0){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Price should be greater than 0")));
            return;
        }

        // Get uploaded image
        Part imagePart = req.getPart("image");

        if(imagePart == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("The image is missing")));
            return;
        }

        // Get filename and mimeType
        String filename = code + "_" + imagePart.getSubmittedFileName();
        String mimeType = getServletContext().getMimeType(filename);

        // Validate mimetype
        if(mimeType == null || !mimeType.startsWith("image/")){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Uploaded file is not an image")));
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

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().println(new Gson().toJson(new Error("Error while storing the item in the database")));
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
