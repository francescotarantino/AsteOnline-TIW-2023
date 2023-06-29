package com.asteonline.web.beans;

import com.asteonline.web.dao.AuctionDAO.AuctionStatus;

import java.beans.JavaBean;
import java.util.Date;
import java.util.List;

@JavaBean
public class Auction {
    private int id;
    private AuctionStatus status;
    private Date deadline;
    private boolean isExpired;
    private float startingPrice;
    private int minimumRise;
    private int userID;
    private int wonByUserID;
    private String wonByFullname;
    private String wonByShippingAddress;
    private float finalPrice;
    private List<Item> items;
    private String humanReadableDeadline;
    private float maxOffer;
    private String itemsMatch;
    private int itemsMatchCount;
    private float minBid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public float getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(float startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getMinimumRise() {
        return minimumRise;
    }

    public void setMinimumRise(int minimumRise) {
        this.minimumRise = minimumRise;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getWonByUserID() {
        return wonByUserID;
    }

    public void setWonByUserID(int wonByUserID) {
        this.wonByUserID = wonByUserID;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(float finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getHumanReadableDeadline() {
        return humanReadableDeadline;
    }

    public void setHumanReadableDeadline(String humanReadableDeadline) {
        this.humanReadableDeadline = humanReadableDeadline;
    }

    public float getMaxOffer() {
        return maxOffer;
    }

    public void setMaxOffer(float maxOffer) {
        this.maxOffer = maxOffer;
    }

    public String getWonByFullname() {
        return wonByFullname;
    }

    public void setWonByFullname(String wonByFullname) {
        this.wonByFullname = wonByFullname;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public String getWonByShippingAddress() {
        return wonByShippingAddress;
    }

    public void setWonByShippingAddress(String wonByShippingAddress) {
        this.wonByShippingAddress = wonByShippingAddress;
    }

    public int getItemsMatchCount() {
        return itemsMatchCount;
    }

    public void setItemsMatchCount(int itemsMatchCount) {
        this.itemsMatchCount = itemsMatchCount;
    }

    public String getItemsMatch() {
        return itemsMatch;
    }

    public void setItemsMatch(String itemsMatch) {
        this.itemsMatch = itemsMatch;
    }

    public float getMinBid() {
        return minBid;
    }

    public void setMinBid(float minBid) {
        this.minBid = minBid;
    }
}
