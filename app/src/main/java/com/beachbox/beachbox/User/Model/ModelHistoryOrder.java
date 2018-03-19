package com.beachbox.beachbox.User.Model;

/**
 * Created by bitware on 23/3/17.
 */

public class ModelHistoryOrder {

    public String resName;
    public String resDateTime;
    public String resPrice;
    public String resRating;
    public String resImage;
    public String orderId;

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String restaurant_id;

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String custName;
    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResDateTime() {
        return resDateTime;
    }

    public void setResDateTime(String resDateTime) {
        this.resDateTime = resDateTime;
    }

    public String getResPrice() {
        return resPrice;
    }

    public void setResPrice(String resPrice) {
        this.resPrice = resPrice;
    }

    public String getResRating() {
        return resRating;
    }

    public void setResRating(String resRating) {
        this.resRating = resRating;
    }

    public String getResImage() {
        return resImage;
    }

    public void setResImage(String resImage) {
        this.resImage = resImage;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
