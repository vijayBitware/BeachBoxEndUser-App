
package com.beachbox.beachbox.User.Model.historyOrderResponse;

import java.util.List;

import com.beachbox.beachbox.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HistoryOrderResponse extends BaseResponse {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("is_success")
    @Expose
    private Boolean isSuccess;
    @SerializedName("orderdetails")
    @Expose
    private List<Orderdetail> orderdetails = null;

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

    public List<Orderdetail> getOrderdetails() {
        return orderdetails;
    }

    public void setOrderdetails(List<Orderdetail> orderdetails) {
        this.orderdetails = orderdetails;
    }

}
