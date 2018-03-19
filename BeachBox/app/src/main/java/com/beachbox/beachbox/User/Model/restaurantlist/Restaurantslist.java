
package com.beachbox.beachbox.User.Model.restaurantlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Restaurantslist {

    @SerializedName("restaurant_id")
    @Expose
    private Integer restaurantId;
    @SerializedName("restaurant_name")
    @Expose
    private String restaurantName;
    @SerializedName("restaurant_image")
    @Expose
    private String restaurantImage;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("open_time")
    @Expose
    private String openTime;
    @SerializedName("close_time")
    @Expose
    private String closeTime;
    @SerializedName("rating")
    @Expose
    private Double rating;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;
    @SerializedName("rs_open_closed_status")
    @Expose
    private String rsOpenClosedStatus;
    @SerializedName("calculated_distance")
    @Expose
    private Float calculatedDistance;
    @SerializedName("favorite_count")
    @Expose
    private Integer favoriteCount;
    @SerializedName("favorite_flag")
    @Expose
    private Integer favoriteFlag;
    @SerializedName("advertisement")
    @Expose
    private Integer advertisement;
    @SerializedName("state_abbr")
    @Expose
    private Object stateAbbr;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("link")
    @Expose
    private String link;

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getRsOpenClosedStatus() {
        return rsOpenClosedStatus;
    }

    public void setRsOpenClosedStatus(String rsOpenClosedStatus) {
        this.rsOpenClosedStatus = rsOpenClosedStatus;
    }

    public Float getCalculatedDistance() {
        return calculatedDistance;
    }

    public void setCalculatedDistance(Float calculatedDistance) {
        this.calculatedDistance = calculatedDistance;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getFavoriteFlag() {
        return favoriteFlag;
    }

    public void setFavoriteFlag(Integer favoriteFlag) {
        this.favoriteFlag = favoriteFlag;
    }

    public Integer getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Integer advertisement) {
        this.advertisement = advertisement;
    }

    public Object getStateAbbr() {
        return stateAbbr;
    }

    public void setStateAbbr(Object stateAbbr) {
        this.stateAbbr = stateAbbr;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
