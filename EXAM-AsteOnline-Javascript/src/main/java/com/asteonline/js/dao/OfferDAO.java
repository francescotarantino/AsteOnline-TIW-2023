package com.asteonline.js.dao;

import com.asteonline.js.beans.Offer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO {
    private final Connection con;

    public OfferDAO(Connection con) {
        this.con = con;
    }

    /**
     * This method returns an ordered list of offers for a given auction.
     * @param auctionID the ID of the auction
     * @return a list of offers
     */
    public List<Offer> getAuctionOffers(int auctionID) throws SQLException {
        List<Offer> offers = new ArrayList<>();

        String query = "SELECT user_id, price, created_at, user.username, CONCAT(user.firstname, ' ', user.lastname) AS fullname "
                     + "FROM offer JOIN user on user.id = offer.user_id "
                     + "WHERE auction_id = ? "
                     + "ORDER BY created_at DESC";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Offer offer = new Offer();
            offer.setUserID(result.getInt("user_id"));
            offer.setPrice(result.getFloat("price"));
            offer.setCreatedAt(result.getTimestamp("created_at"));
            offer.setUsername(result.getString("user.username"));
            offer.setFullname(result.getString("fullname"));

            offers.add(offer);
        }

        result.close();
        stmt.close();
        return offers;
    }

    /**
     * This method adds a new offer to the database.
     * @param auctionID the ID of the auction
     * @param userID the id of the user who is making the offer
     * @param price the offer
     */
    public void addOffer(int auctionID, int userID, float price) throws SQLException {
        String query = "INSERT INTO offer (auction_id, user_id, price) VALUES (?, ?, ?)";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);
        stmt.setInt(2, userID);
        stmt.setFloat(3, price);

        stmt.executeUpdate();
        stmt.close();
    }
}
