
package com.beachbox.beachbox.User.Model.loginResponse;

import com.beachbox.beachbox.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse extends BaseResponse {

    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("is_success")
    @Expose
    private Boolean isSuccess;
    @SerializedName("err_msg")
    @Expose
    private String errMsg;
    @SerializedName("logindetails")
    @Expose
    private Logindetails logindetails;

    @SerializedName("default_creditcard")
    @Expose
    private String default_creditcard;

    @SerializedName("default_creditcard_id")
    @Expose
    private String default_creditcard_id;

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }


    public String getDefault_creditcard() {
        return default_creditcard;
    }

    public void setDefault_creditcard(String default_creditcard) {
        this.default_creditcard = default_creditcard;
    }

    public String getDefault_creditcard_id() {
        return default_creditcard_id;
    }

    public void setDefault_creditcard_id(String default_creditcard_id) {
        this.default_creditcard_id = default_creditcard_id;
    }

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

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Logindetails getLogindetails() {
        return logindetails;
    }

    public void setLogindetails(Logindetails logindetails) {
        this.logindetails = logindetails;
    }

}
