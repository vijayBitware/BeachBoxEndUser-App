
package com.beachbox.beachbox.User.Model.signupResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Logindetails {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("role_id")
    @Expose
    private Integer roleId;
    @SerializedName("username")
    @Expose
    private Object username;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("reset_hash")
    @Expose
    private Object resetHash;
    @SerializedName("last_login")
    @Expose
    private Object lastLogin;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("deleted")
    @Expose
    private Integer deleted;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @SerializedName("phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("avatar")
    @Expose
    private Object avatar;
    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("activate_hash")
    @Expose
    private String activateHash;
    @SerializedName("profile_id")
    @Expose
    private Integer profileId;
    @SerializedName("customerPaymentProfileId")
    @Expose
    private Object customerPaymentProfileId;
    @SerializedName("facebook_token")
    @Expose
    private String facebookToken;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Object getUsername() {
        return username;
    }

    public void setUsername(Object username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getResetHash() {
        return resetHash;
    }

    public void setResetHash(Object resetHash) {
        this.resetHash = resetHash;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Object lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Object getAvatar() {
        return avatar;
    }

    public void setAvatar(Object avatar) {
        this.avatar = avatar;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getActivateHash() {
        return activateHash;
    }

    public void setActivateHash(String activateHash) {
        this.activateHash = activateHash;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Object getCustomerPaymentProfileId() {
        return customerPaymentProfileId;
    }

    public void setCustomerPaymentProfileId(Object customerPaymentProfileId) {
        this.customerPaymentProfileId = customerPaymentProfileId;
    }

    public String getFacebookToken() {
        return facebookToken;
    }

    public void setFacebookToken(String facebookToken) {
        this.facebookToken = facebookToken;
    }

}
