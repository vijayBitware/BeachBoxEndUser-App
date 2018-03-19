
package com.beachbox.beachbox.User.Model.signupResponse;

import com.beachbox.beachbox.volly.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignUpResponse extends BaseResponse {

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
