package com.asteonline.js.dao;

import com.asteonline.js.beans.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    private final Connection con;

    public ItemDAO(Connection con) {
        this.con = con;
    }

    /**
     * This method adds a new item to the database.
     * @param item the item to add
     */
    public void addItem(Item item) throws SQLException {
        String query = "INSERT INTO item (code, name, description, price, image_path, user_id) "
                + "VALUES (?,?,?,?,?,?)";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, item.getCode());
        stmt.setString(2, item.getName());
        stmt.setString(3, item.getDescription());
        stmt.setFloat(4, item.getPrice());
        stmt.setString(5, item.getImagePath());
        stmt.setInt(6, item.getUserID());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * This method returns a list of items available for auction of a given user.
     * @param userID the ID of the user
     * @return a list of item
     */
    public List<Item> getItemsAvailableForAuction(int userID) throws SQLException {
        List<Item> items = new ArrayList<>();

        String query = "SELECT code, name, description, image_path, price "
                + "FROM item "
                + "WHERE user_id = ? AND auction_id IS NULL";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, userID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Item item = new Item();
            item.setCode(result.getString("code"));
            item.setName(result.getString("name"));
            item.setDescription(result.getString("description"));
            item.setImagePath(result.getString("image_path"));
            item.setPrice(result.getFloat("price"));
            item.setUserID(userID);

            items.add(item);
        }

        result.close();
        stmt.close();
        return items;
    }

    /**
     * This method attaches an item to an auction.
     * @param itemCode the code of the item
     * @param auctionID the ID of the auction
     */
    public void attachItemToAuction(String itemCode, int auctionID) throws SQLException {
        String query = "UPDATE item "
                + "SET item.auction_id = ? "
                + "WHERE item.code = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);
        stmt.setString(2, itemCode);

        stmt.executeUpdate();

        stmt.close();
    }

    /**
     * This method returns a list of items attached to a given auction.
     * @param auctionID the ID of the auction
     * @return a list of item
     */
    public List<Item> getItemsAttachedToAuction(int auctionID) throws SQLException {
        List<Item> items = new ArrayList<>();

        String query = "SELECT code, name, description, image_path, price, user_id "
                + "FROM item "
                + "WHERE auction_id = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Item item = new Item();
            item.setCode(result.getString("code"));
            item.setName(result.getString("name"));
            item.setDescription(result.getString("description"));
            item.setImagePath(result.getString("image_path"));
            item.setPrice(result.getFloat("price"));
            item.setUserID(result.getInt("user_id"));
            item.setAuctionID(auctionID);

            items.add(item);
        }

        result.close();
        stmt.close();
        return items;
    }
}
