package com.beachbox.beachbox.Config;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.realm.Realm;

public class Config {
   // public static String BASE_URL = "http://beachbox.dotcomweavers.net/api/";
    public static String BASE_URL = "https://app.beachboxenterprise.com/api/";
    public static String PHOTO_UPLOAD_URL = "http://app.beachboxenterprise.com/api/";

    public static String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static String PHONE_REGEX = "^\\([0-9]{3}\\)[0-9]{3}-[0-9]{4}$";
    public static String accessToken = "dy1Wq-gJuCuAPfBa7dZsk-EK";

    public static String PICKUP = "pickup";
    public static String APP_VERSION = "1.0";
    public static String DELIVERY = "delivery";
    public static String GET = "GET";
    public static String POST = "POST";
    //public static String SALES_TAX = "2.30";
    public static String PAYMNTCASH = "cash";
    public static String DEVICE_TYPE = "android";
    public static String PUSH_NOTI_TOKAN = " ";
    public static String notiFlag = "No";
    public static String isShowRestoTitle = "Yes";

    //------------------------------------------------------


    public static final int  API_RESTAURANT_LIST = 100;
    public static final int  API_PLACE_ORDER = 101;
    public static final int  API_SIGNIN = 102;
    public static final int  API_SIGNUP = 103;
    public static final int  API_GET_UPCOMING_ORDER= 104;
    public static final int  API_GET_HISTORY_ORDER= 105;
    public static final int  API_FILTER_SUB_MENU= 106;
    public static final int  REMOVE_PAYMENT_CARD= 107;
    public static final int  API_FAV_UNFAV_RESTAURANT= 108;
    public static final int  API_CANCEL_PENDING_ORDER= 109;
    public static final int  API_GET_ALLNOTIFICATION= 110;
    public static final int  API_VERSION_UPDATE= 111;
    public static final int  API_UPDATE_DELIVERY_BOY_LOCATION= 112;
 public static final int  API_LOG_OUT= 113;
    //------------------------------------------------------

              /*  editor.putString("username",loginResponse.getLogindetails().getDisplayname());
                editor.putString("useremail",loginResponse.getLogindetails().getEmail());
                editor.putString("userSessionTokan",loginResponse.getLogindetails().getSessionUsertoken());
                editor.putString("isUserLoggedIn","Yes");*/
               //if(sharedPreferences.getString("isUserLoggedIn","").equalsIgnoreCase("Yes")){}


    /**
     * add or remove from realm
     *
     * @param modelRestaurantDetails
     */
    public static void  addRemoveFromCart(ModelRestaurantDetails modelRestaurantDetails){
        Realm realm = RealmController.getInstance().getRealm();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(modelRestaurantDetails);
        realm.commitTransaction();
    }
    public static boolean checkHasItemInCart() {
        boolean hasItem = RealmController.getInstance().hasItemInDB();
        return hasItem ;
    }
}
