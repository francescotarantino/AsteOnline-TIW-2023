package com.asteonline.web.servlets;

import com.asteonline.web.utils.FileManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@WebServlet(
        urlPatterns = "/GetImage/*",
        description = "This servlet is used to get images from the filesystem. * is the path of the image."
)
public class GetImage extends HttpServlet {
    /**
     * Took inspiration from <a href="https://stackoverflow.com/a/1812356">Stackoverflow</a>.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getPathInfo() == null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "You should specify a path");
            return;
        }

        // Get the path of the file from the request
        String filename = URLDecoder.decode(req.getPathInfo().substring(1), StandardCharsets.UTF_8);

        // Get the file from the filesystem
        File file = FileManager.getFile(getServletContext(), filename);

        // Check if the file exists
        if(!file.exists() || !file.isFile()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image not found");
            return;
        }

        // Set the response headers
        resp.setHeader("Content-Type", getServletContext().getMimeType(filename));
        resp.setHeader("Content-Length", String.valueOf(file.length()));
        resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        // Copy the file to the response
        Files.copy(file.toPath(), resp.getOutputStream());
    }
}
