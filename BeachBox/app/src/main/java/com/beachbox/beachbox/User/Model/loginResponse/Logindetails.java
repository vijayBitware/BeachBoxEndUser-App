
package com.beachbox.beachbox.User.Model.loginResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Logindetails {

    @SerializedName("displayname")
    @Expose
    private String displayname;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("session_usertoken")
    @Expose
    private String sessionUsertoken;

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionUsertoken() {
        return sessionUsertoken;
    }

    public void setSessionUsertoken(String sessionUsertoken) {
        this.sessionUsertoken = sessionUsertoken;
    }

}
