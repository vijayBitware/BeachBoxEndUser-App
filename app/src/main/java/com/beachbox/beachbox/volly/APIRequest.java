package com.beachbox.beachbox.volly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.User.Model.favouriteRestaurant.ResponseFavUnfavRestaurant;
import com.beachbox.beachbox.User.Model.filterSubMenuResponse.ResponseFilterSubMenu;
import com.beachbox.beachbox.User.Model.historyOrderResponse.HistoryOrderResponse;
import com.beachbox.beachbox.User.Model.loginResponse.LogOut;
import com.beachbox.beachbox.User.Model.loginResponse.LoginResponse;
import com.beachbox.beachbox.User.Model.notificationResponse.ResponseNotificationNew;
import com.beachbox.beachbox.User.Model.removePaymentCard.ResponseRemoveCard;
import com.beachbox.beachbox.User.Model.responseCancelOrder.ResponseCancelOrder;
import com.beachbox.beachbox.User.Model.responseUpdateVersion.UpdateVersionResponse;
import com.beachbox.beachbox.User.Model.responseplaceorder.ResponsePlaceOrder;
import com.beachbox.beachbox.User.Model.restaurantlist.ResponseRestaurantList;
import com.beachbox.beachbox.User.Model.signupResponse.SignUpResponse;
import com.beachbox.beachbox.User.Model.upcomingOrders.UpcomingOrderResponse;
import com.beachbox.beachbox.User.Model.updateDeliveryLocation.UpdateDeliveryLocation;
import com.google.gson.Gson;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitwarepc on 04-Jul-17.
 */

public class APIRequest extends AppCompatActivity {

    private JSONObject mJsonObject;
    private String mUrl;
    private ResponseHandler responseHandler;
    private int API_NAME;
    private Context mContext;
    ProgressDialog progressDialog;
    BaseResponse baseResponse;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public APIRequest(Context context, JSONObject jsonObject, String url, ResponseHandler responseHandler1, int api, String methodName) {
        this.responseHandler = responseHandler1;
        this.API_NAME = api;
        this.mUrl = url;
        this.mJsonObject = jsonObject;
        this.mContext = context;
        sharedPreferences = this.mContext.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();
        System.out.println("api NO >>> "+api);

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if (methodName.equals(Config.GET)) {
            apiGetRequest();
        } else {
            apiPostRequest();
        }

    }



    private void apiPostRequest() {
        String REQUEST_TAG = String.valueOf(API_NAME);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(mUrl, mJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" >>> API RESPONSE " + response);
                        setResponseToBody(response);
                        progressDialog.hide();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
            }
        });

        jsonObjectReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppSingleton.getInstance(mContext).addToRequestQueue(jsonObjectReq, REQUEST_TAG);
    }

    private void apiGetRequest() {
        String REQUEST_TAG = String.valueOf(API_NAME);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(mUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response is " + response);
                        setResponseToBody(response);
                        progressDialog.hide();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
            }
        });

        jsonObjectReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppSingleton.getInstance(mContext).addToRequestQueue(jsonObjectReq, REQUEST_TAG);
    }

    private void setResponseToBody(JSONObject response) {
        Gson gson = new Gson();
        switch (API_NAME) {
            case Config.API_LOG_OUT:
                baseResponse = gson.fromJson(response.toString(), LogOut.class);
                break;

            case Config.API_PLACE_ORDER:
                baseResponse = gson.fromJson(response.toString(), ResponsePlaceOrder.class);
                break;

            case Config.API_RESTAURANT_LIST:
                //System.out.println(">>> API_RESTAURANT_LIST :"+response.toString());
                baseResponse = gson.fromJson(response.toString(), ResponseRestaurantList.class);
                break;

            case Config.API_SIGNIN:
                baseResponse = gson.fromJson(response.toString(), LoginResponse.class);
                break;

            case Config.API_SIGNUP:
                baseResponse = gson.fromJson(response.toString(), SignUpResponse.class);
                break;

            case Config.API_GET_UPCOMING_ORDER:
                baseResponse = gson.fromJson(response.toString(), UpcomingOrderResponse.class);
                break;

            case Config.API_GET_HISTORY_ORDER:
                baseResponse = gson.fromJson(response.toString(), HistoryOrderResponse.class);
                break;

            case Config.API_FILTER_SUB_MENU:
                baseResponse = gson.fromJson(response.toString(), ResponseFilterSubMenu.class);
                break;

            case Config.REMOVE_PAYMENT_CARD:
                baseResponse = gson.fromJson(response.toString(), ResponseRemoveCard.class);
                break;

            case Config.API_FAV_UNFAV_RESTAURANT:
                baseResponse = gson.fromJson(response.toString(), ResponseFavUnfavRestaurant.class);
                break;

            case Config.API_CANCEL_PENDING_ORDER:
                System.out.println(">>>> response.toString() Cancel :"+response.toString());
                baseResponse = gson.fromJson(response.toString(), ResponseCancelOrder.class);
                break;

            case Config.API_GET_ALLNOTIFICATION:
                System.out.println(">>>>  Notification result :"+response.toString());
                baseResponse = gson.fromJson(response.toString(), ResponseNotificationNew.class);
                break;

            case Config.API_UPDATE_DELIVERY_BOY_LOCATION:
                System.out.println(">>>> Update Delivery in APIRequest  :"+response.toString());
                baseResponse = gson.fromJson(response.toString(), UpdateDeliveryLocation.class);
                break;
        }
        baseResponse.setApiName(API_NAME);
        responseHandler.onSuccess(baseResponse);
    }


    public interface ResponseHandler {
        public void onSuccess(BaseResponse response);

        public void onFailure(BaseResponse response);

    }
}