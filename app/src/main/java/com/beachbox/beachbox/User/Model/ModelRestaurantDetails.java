package com.beachbox.beachbox.User.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by bitwarepc on 04-Jul-17.
 */

public class ModelRestaurantDetails extends RealmObject {


    @PrimaryKey
    private String menu_id;
    private String qty;
    private String menu_name;
    private String menu_description;
    private String menu_price;
    private String menu_type;

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    private String mainCategory;

    public String getRestoTiming() {
        return RestoTiming;
    }

    public void setRestoTiming(String restoTiming) {
        RestoTiming = restoTiming;
    }

    private String RestoTiming;

    public String getResraurantTax() {
        return resraurantTax;
    }

    public void setResraurantTax(String resraurantTax) {
        this.resraurantTax = resraurantTax;
    }

    private String resraurantTax;

    public String getRestoName() {
        return restoName;
    }

    public void setRestoName(String restoName) {
        this.restoName = restoName;
    }

    private String restoName;

    public String getMenu_type() {
        return menu_type;
    }

    public void setMenu_type(String menu_type) {
        this.menu_type = menu_type;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    private String restaurantId;

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getMenu_name() {
        return menu_name;
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }

    public String getMenu_description() {
        return menu_description;
    }

    public void setMenu_description(String menu_description) {
        this.menu_description = menu_description;
    }

    public String getMenu_price() {
        return menu_price;
    }

    public void setMenu_price(String menu_price) {
        this.menu_price = menu_price;
    }
}
