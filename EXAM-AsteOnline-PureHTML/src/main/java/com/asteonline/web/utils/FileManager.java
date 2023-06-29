package com.asteonline.web.utils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public abstract class FileManager {
    /**
     * @param context the servlet context
     * @param path the path of the file
     * @return the file object corresponding to the specified path
     */
    public static File getFile(ServletContext context, String path){
        return new File(context.getInitParameter("STORAGE_PATH"), path);
    }

    /**
     * @param context the servlet context
     * @param folder the folder where to store the file
     * @param filename the name of the file
     * @param inputStream the input stream of the file
     */
    public static void storeFile(ServletContext context, String folder, String filename, InputStream inputStream) throws IOException {
        File storage = new File(context.getInitParameter("STORAGE_PATH") + "/" + folder);

        // Create the directory (only if it doesn't exist)
        Files.createDirectories(storage.toPath());

        File file = new File(storage, filename);

        // Actually copy the content from the input stream to the file
        Files.copy(inputStream, file.toPath());
    }

    /**
     * @param context the servlet context
     * @param path the path of the file to remove
     */
    public static void removeFile(ServletContext context, String path){
        new File(context.getInitParameter("STORAGE_PATH") + "/" + path).delete();
    }
}
