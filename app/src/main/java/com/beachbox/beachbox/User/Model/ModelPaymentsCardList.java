package com.beachbox.beachbox.User.Model;

/**
 * Created by bitwarepc on 11-Jul-17.
 */

public class ModelPaymentsCardList {

    String isDefault;

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String zipCode;
    public String getCard_cvv() {
        return card_cvv;
    }

    public void setCard_cvv(String card_cvv) {
        this.card_cvv = card_cvv;
    }

    public String getCard_exp_month() {
        return card_exp_month;
    }

    public void setCard_exp_month(String card_exp_month) {
        this.card_exp_month = card_exp_month;
    }

    public String getCard_exp_year() {
        return card_exp_year;
    }

    public void setCard_exp_year(String card_exp_year) {
        this.card_exp_year = card_exp_year;
    }

    public String card_cvv,card_exp_month,card_exp_year;
    public String getCardholder_name() {
        return cardholder_name;
    }

    public void setCardholder_name(String cardholder_name) {
        this.cardholder_name = cardholder_name;
    }

    public String getCredit_card_mask() {
        return credit_card_mask;
    }

    public void setCredit_card_mask(String credit_card_mask) {
        this.credit_card_mask = credit_card_mask;
    }

    public String getBilling_address_id() {
        return billing_address_id;
    }

    public void setBilling_address_id(String billing_address_id) {
        this.billing_address_id = billing_address_id;
    }

    public String getUser_profile_id() {
        return user_profile_id;
    }

    public void setUser_profile_id(String user_profile_id) {
        this.user_profile_id = user_profile_id;
    }

    public String getPayment_profile_id() {
        return payment_profile_id;
    }

    public void setPayment_profile_id(String payment_profile_id) {
        this.payment_profile_id = payment_profile_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String cardholder_name,credit_card_mask,billing_address_id,user_profile_id,payment_profile_id,id,card_type,user_id;



}
