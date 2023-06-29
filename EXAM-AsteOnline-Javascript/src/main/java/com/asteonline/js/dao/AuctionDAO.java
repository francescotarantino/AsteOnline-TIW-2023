package com.asteonline.js.dao;

import com.asteonline.js.beans.Auction;
import com.asteonline.js.beans.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AuctionDAO {
    private final Connection con;

    /**
     * Enumerates all the possible statuses of an auction
     */
    public enum AuctionStatus {
        OPEN("Aperta"),
        CLOSED("Chiusa");

        private final String statusHumanReadable;

        AuctionStatus(String statusHumanReadable) {
            this.statusHumanReadable = statusHumanReadable;
        }

        @Override
        public String toString() {
            return this.statusHumanReadable;
        }
    }

    public AuctionDAO(Connection con) {
        this.con = con;
    }

    /**
     * This method returns the auctions that match the search criteria.
     * The auctions returned are open and not expired.
     * @param search the query
     * @param userID the user who's searching (to exclude his auctions)
     * @return a list of auctions
     */
    public List<Auction> searchOpenAuctions(String search, int userID) throws SQLException {
        List<Auction> auctions = new ArrayList<>();

        String query = "SELECT auction.id, auction.deadline, auction.starting_price, auction.minimum_rise, wo.price AS max_offer, COUNT(*) AS items_match_count, GROUP_CONCAT(item.name SEPARATOR ', ') AS items_match "
                + "FROM auction JOIN item ON auction.id = item.auction_id "
                + "LEFT JOIN winning_offer AS wo ON auction.id = wo.auction_id "
                + "WHERE (LOWER(item.name) LIKE CONCAT('%', LOWER(?), '%') OR LOWER(item.description) LIKE CONCAT('%', LOWER(?), '%')) "
                + "AND auction.status = 'OPEN' AND auction.deadline > CURRENT_TIMESTAMP AND auction.user_id != ? "
                + "GROUP BY auction.id, auction.deadline, auction.starting_price, auction.minimum_rise, wo.price "
                + "ORDER BY auction.deadline ASC ";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, search);
        stmt.setString(2, search);
        stmt.setInt(3, userID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Auction auction = new Auction();
            auction.setId(result.getInt("auction.id"));
            auction.setDeadline(result.getTimestamp("auction.deadline"));
            auction.setStartingPrice(result.getFloat("auction.starting_price"));
            auction.setMinimumRise(result.getInt("auction.minimum_rise"));
            auction.setStatus(AuctionStatus.OPEN);
            auction.setMaxOffer(result.getFloat("max_offer"));
            auction.setItemsMatchCount(result.getInt("items_match_count"));
            auction.setItemsMatch(result.getString("items_match"));

            auctions.add(auction);
        }

        result.close();
        stmt.close();

        return auctions;
    }

    /**
     * This method returns the auction with the specified ID.
     * @param auctionID the ID of the auction
     * @return all the details of the auction
     */
    public Auction getAuction(int auctionID) throws SQLException {
        Auction auction = new Auction();

        String query = "SELECT auction.user_id, status, deadline, starting_price, minimum_rise, won_by, final_price, wo.price AS max_offer, CONCAT(won_by_user.firstname, ' ', won_by_user.lastname) AS won_by_fullname, won_by_user.shipping_address "
                + "FROM auction "
                + "LEFT JOIN winning_offer AS wo ON auction.id = wo.auction_id "
                + "LEFT JOIN user AS won_by_user ON auction.won_by = won_by_user.id "
                + "WHERE auction.id = ? ";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);

        ResultSet result = stmt.executeQuery();
        result.next();

        auction.setId(auctionID);
        auction.setUserID(result.getInt("auction.user_id"));
        auction.setDeadline(result.getTimestamp("deadline"));
        auction.setExpired(result.getString("status").equals("CLOSED") || result.getTimestamp("deadline").before(new Date()));
        auction.setStartingPrice(result.getFloat("starting_price"));
        auction.setMinimumRise(result.getInt("minimum_rise"));
        auction.setStatus(AuctionStatus.valueOf(result.getString("status")));
        auction.setWonByUserID(result.getInt("won_by"));
        auction.setFinalPrice(result.getFloat("final_price"));
        auction.setMaxOffer(result.getFloat("max_offer"));
        auction.setWonByFullname(result.getString("won_by_fullname"));
        auction.setWonByShippingAddress(result.getString("won_by_user.shipping_address"));
        if(result.getFloat("max_offer") > 0){
            auction.setMinBid(result.getFloat("max_offer") + auction.getMinimumRise());
        } else {
            auction.setMinBid(auction.getStartingPrice() + auction.getMinimumRise());
        }

        result.close();
        stmt.close();
        return auction;
    }

    /**
     * This method returns all the auctions that the user has won.
     * @param userID the user ID
     * @return a list of auctions
     */
    public List<Auction> getWonAuctions(int userID) throws SQLException {
        List<Auction> auctions = new ArrayList<>();

        String query = "SELECT id, final_price "
                + "FROM auction "
                + "WHERE won_by = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, userID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Auction auction = new Auction();
            auction.setId(result.getInt("id"));
            auction.setStatus(AuctionStatus.CLOSED);
            auction.setWonByUserID(userID);
            auction.setFinalPrice(result.getFloat("final_price"));

            auctions.add(auction);
        }

        result.close();
        stmt.close();
        return auctions;
    }

    public List<Auction> getUserAuctions(int userID) throws SQLException {
        List<Auction> auctions = new ArrayList<>();

        String query = "SELECT auction.id, deadline, status, starting_price, minimum_rise, won_by, final_price, wo.price AS max_offer, CONCAT(won_by_user.firstname, ' ', won_by_user.lastname) AS won_by_fullname "
                + "FROM auction "
                + "LEFT JOIN winning_offer AS wo ON auction.id = wo.auction_id "
                + "LEFT JOIN user AS won_by_user ON auction.won_by = won_by_user.id "
                + "WHERE auction.user_id = ? ";
        query += "ORDER BY deadline ASC ";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, userID);

        ResultSet result = stmt.executeQuery();

        while(result.next()){
            Auction auction = new Auction();
            auction.setId(result.getInt("auction.id"));
            auction.setDeadline(result.getTimestamp("deadline"));
            auction.setExpired(result.getString("status").equals("CLOSED") || result.getTimestamp("deadline").before(new Date()));
            auction.setStartingPrice(result.getFloat("starting_price"));
            auction.setMinimumRise(result.getInt("minimum_rise"));
            auction.setUserID(userID);
            auction.setStatus(AuctionStatus.valueOf(result.getString("status")));
            auction.setWonByUserID(result.getInt("won_by"));
            auction.setFinalPrice(result.getFloat("final_price"));
            auction.setMaxOffer(result.getFloat("max_offer"));
            auction.setWonByFullname(result.getString("won_by_fullname"));

            auctions.add(auction);
        }

        result.close();
        stmt.close();
        return auctions;
    }

    /**
     * Closes the auction.
     * @param auctionID the auction ID
     */
    public void closeAuction(int auctionID) throws SQLException {
        String query = "UPDATE auction "
                + "SET status = 'CLOSED', won_by = (SELECT user_id FROM winning_offer WHERE auction_id = auction.id), final_price = (SELECT price FROM winning_offer WHERE auction_id = auction.id) "
                + "WHERE id = ?";

        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, auctionID);

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * This method adds a new auction to the database.
     * @param deadline the deadline of the auction
     * @param minimumRise the minimum rise of the auction
     * @param userID the ID of the user who is creating the auction
     * @param itemsCodes an array of the items to be attached to the auction
     * @return the ID of the new auction
     */
    public int newAuction(Date deadline, int minimumRise, int userID, String[] itemsCodes) throws SQLException {
        // Disable auto-commit so that the transaction can be rolled back if something goes wrong
        con.setAutoCommit(false);

        String query = "INSERT INTO auction (deadline, starting_price, minimum_rise, user_id) "
                + "VALUES (?,?,?,?)";

        ItemDAO itemDAO = new ItemDAO(con);
        PreparedStatement stmt = null;

        int auctionID;
        try {
            List<Item> availableItems = itemDAO.getItemsAvailableForAuction(userID);
            boolean allValidItems = true;
            for (String code : itemsCodes){
                if(availableItems
                        .stream()
                        .noneMatch(item -> item.getCode().equals(code))
                ){
                    allValidItems = false;
                    break;
                }
            }

            if(!allValidItems){
                throw new SQLException("Invalid items");
            }

            float startingPrice = availableItems
                    .stream()
                    .filter(item -> Arrays.asList(itemsCodes).contains(item.getCode()))
                    .map(Item::getPrice)
                    .reduce(0f, Float::sum);

            stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setTimestamp(1, new java.sql.Timestamp(deadline.getTime()));
            stmt.setFloat(2, startingPrice);
            stmt.setInt(3, minimumRise);
            stmt.setInt(4, userID);

            stmt.executeUpdate();

            ResultSet result = stmt.getGeneratedKeys();
            result.next();
            auctionID = result.getInt(1);
            result.close();

            for (String code : itemsCodes){
                itemDAO.attachItemToAuction(code, auctionID);
            }

            // Commit the transaction
            con.commit();
        } catch (SQLException e){
            // If something goes wrong, rollback
            con.rollback();
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }

            // Re-enable auto-commit
            con.setAutoCommit(true);
        }

        return auctionID;
    }
}
