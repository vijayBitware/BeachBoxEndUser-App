
package com.beachbox.beachbox.User.Model.filterSubMenuResponse;

import java.util.List;

import com.beachbox.beachbox.expandable.Menu;
import com.beachbox.beachbox.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseFilterSubMenu extends BaseResponse {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("is_success")
    @Expose
    private Boolean isSuccess;
    @SerializedName("dietary_menu")
    @Expose
    private List<Menu> dietaryMenu = null;
    @SerializedName("cuisines_menu")
    @Expose
    private List<Menu> cuisinesMenu = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public List<Menu> getDietaryMenu() {
        return dietaryMenu;
    }

    public void setDietaryMenu(List<Menu> dietaryMenu) {
        this.dietaryMenu = dietaryMenu;
    }

    public List<Menu> getCuisinesMenu() {
        return cuisinesMenu;
    }

    public void setCuisinesMenu(List<Menu> cuisinesMenu) {
        this.cuisinesMenu = cuisinesMenu;
    }

}
