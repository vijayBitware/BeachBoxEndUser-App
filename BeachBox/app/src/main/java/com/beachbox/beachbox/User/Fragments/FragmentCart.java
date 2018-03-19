package com.beachbox.beachbox.User.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.GPSTracker;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Adapter.AdapterCartList;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;
import com.beachbox.beachbox.User.Model.responseplaceorder.ResponsePlaceOrder;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.beachbox.beachbox.widgets.TextViewBold;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 22/3/17.
 */

public class FragmentCart extends Fragment implements AdapterCartList.myInterface, APIRequest.ResponseHandler {
    View view, header, footer;
    ListView lv_cart;
    AdapterCartList adapterCartList;
    ArrayList<ModelRestaurantDetails> arrCartList;
    ArrayList<ModelRestaurantDetails> arrCartUpdated = new ArrayList<>();
    TextView tvCartRestoName, tvOrderSubTotal, tvSalesTax, tvCardDetails, tvCartIsEmpty;
    LinearLayout llfiteenperdis, lltwentyper, llNoTip, llCustome;
    TextView tvFifteenPercentage, tvTwentyPercentage, tvPercenategeType, tvTipAmount, tvFinalPlaceOrderAmt;
    EditText etCustomeVal, etCustomerComment;
    TextViewBold tv_clear;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent, isSuccess;
    // LinearLayout llCreditCard;
    String strIsCardParmananent = "";
    String mPaymentOption = "";
    //TextView tvEditCard;
    boolean mFlag = false;

    boolean isTitleShow = true;
    boolean hasItems = false;
    public static String isEditCard = "";
    String RESTAURANT_SALES_TAX = "0.0";
    boolean isTipNoneClicked = false, isTip15Clicked = false, isTip20Clicked = false, isTipCustomlicked = false;
    GPSTracker gpsTracker;
    double lattitude, longitude;

    ////////////////////////////////////////////////////////
    protected static final String TAG = "location-updates-sample";
    /**
     * 10秒間隔で位置情報を更新。実際には多少頻度が多くなるかもしれない。
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * 最速の更新間隔。この値より頻繁に更新されることはない。
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;

    //private ActivityMainBinding mBinding;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    TextView latitudeLabel, longitudeLabel;
    Button btnStart, btnStop;
    /////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_cart, container, false);
            header = inflater.inflate(R.layout.header_cart, null);
            footer = inflater.inflate(R.layout.footer_cart, null);
            inIt();
            // checkHasItemInDB();
            setAdapter();
        }

        return view;
    }

    private void setAdapter() {
        if (getActivity() != null) {
            System.out.println("***********set adapter*********");
            //boolean hasItems = RealmController.getInstance().hasItemInDB();
            boolean hasItems = RealmController.with(getActivity()).hasItemInDB();
            if (hasItems) {
                System.out.println("***********set adapter*****hasItems****" + hasItems);
                adapterCartList = new AdapterCartList(getActivity(), R.layout.row_cart, arrCartUpdated, this, FragmentCart.this);
                lv_cart.addHeaderView(header);
                lv_cart.addFooterView(footer);
                lv_cart.setAdapter(adapterCartList);
            } else {
                adapterCartList = new AdapterCartList(getActivity(), R.layout.row_cart, arrCartUpdated, this, FragmentCart.this);
                lv_cart.addHeaderView(header);
                lv_cart.addFooterView(footer);
                lv_cart.setAdapter(adapterCartList);
            }

        }
    }

    private void inIt() {
        inItWidgets();
        tvSalesTax.setText("$ 0");
        if (!sharedPreferences.getString("cartComment", "").isEmpty()) {
            etCustomerComment.setText(sharedPreferences.getString("cartComment", ""));
        }

       /* gpsTracker = new GPSTracker(getContext());
        if (gpsTracker.canGetLocation()){
            lattitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            System.out.println("Current Location >> Lat > " +lattitude + "Longitude > " +longitude );
        }else {
            Toast.makeText(getContext(),"Enable To Get Location",Toast.LENGTH_SHORT).show();
        }*/

        getCartList();
        normalValInit();

        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove("CartCnt");
                editor.remove("cartCount");
                editor.remove("cartComment");
                editor.apply();

                RealmController.getInstance().clearAll();
                tvCartIsEmpty.setVisibility(View.VISIBLE);
                setCartCount();
                Config.isShowRestoTitle = "No";
                editor.putString("tabPosition", "2");
                editor.commit();

                Intent i = new Intent(getActivity(), UDashboardActivityNew.class);
                startActivity(i);
            }
        });
        paymentInIt();

        strIsCardParmananent = sharedPreferences.getString("IsCardParmanant", "");  //For setting DB payemnt card no
        if (strIsCardParmananent.equalsIgnoreCase("Yes")) {
            String strCardDetails = sharedPreferences.getString("getSelectedCard", "");
            saveSelectedCardDetails();
            if (!strCardDetails.isEmpty()) {

                if (strCardDetails.length() > 4) {
                    String strMaskData = strCardDetails.substring(strCardDetails.length() - 4);
                    tvCardDetails.setText("************" + strMaskData);
                } else {
                    tvCardDetails.setText("************" + strCardDetails);
                }

            } else {
                tvCardDetails.setText("Select Card");
            }
        } else if (strIsCardParmananent.equalsIgnoreCase("No")) {
            String tempCardNo = sharedPreferences.getString("tempCardNo", "");
            saveTemperoryCardDetails();
            if (!tempCardNo.isEmpty()) {
                String strTempMaskData = tempCardNo.substring(tempCardNo.length() - 4);
                tvCardDetails.setText("************" + strTempMaskData);
            } else {
                tvCardDetails.setText("Select Card");
            }
        } else {
            tvCardDetails.setText("Select Card");   // used for card is edited successfully.
        }

        tvFinalPlaceOrderAmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double lat = ((UDashboardActivityNew) getActivity()).getLatitude();
                lattitude = lat;

                double lon = ((UDashboardActivityNew) getActivity()).getLongitude();
                longitude = lon;

                System.out.println("##************location**********" + lattitude + "***" + longitude);

                if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                    if (arrCartUpdated.size() > 0) {
                        String strType = arrCartUpdated.get(0).getMainCategory();
                        System.out.println(">>> My DB FloeType is :" + strType);
                        String strActVal = tvFinalPlaceOrderAmt.getText().toString().trim().replace("Place your order: $", "").trim();
                        if (Double.parseDouble(strActVal) > 0) {
                            if (strType.equalsIgnoreCase(ActivityHome.strFlowType)) {
                                if (ActivityHome.strFlowType.equalsIgnoreCase("pickup")) {
                                    //showPaymentOptionDialog();
                                    if (!tvCardDetails.getText().toString().trim().equalsIgnoreCase("Select Card")) {
                                        mPaymentOption = "CreditCard";
                                        System.out.println("## CALL API FOR PICKUP");
                                        callPlaceOrderAPI();
                                    } else {
                                        Toast.makeText(getActivity(), "Please select payment card for placing order", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (!tvCardDetails.getText().toString().trim().equalsIgnoreCase("Select Card")) {
                                        System.out.println("## CALL API FOR DELIVERY");
                                        callPlaceOrderAPI();
                                    } else {
                                        Toast.makeText(getActivity(), "Please select card for placing order", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), getContext().getResources().getString(R.string.strOtherCategory) + " " + strType, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Amount is not valid for placing order", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getActivity(), "Please add the product in cart for placing order.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please login, for placing order.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        etCustomeVal.setFilters(new InputFilter[] { filter });
    }

    String salesTax = "";

    private void callPlaceOrderAPI() {
        if (tvSalesTax.getText().toString().trim().contains("$")) {
            salesTax = tvSalesTax.getText().toString().trim().replace("$", "").trim();
        } else {
            salesTax = tvSalesTax.getText().toString().trim();
        }
        if (!salesTax.isEmpty()) {
            RESTAURANT_SALES_TAX = salesTax;    // getting actual tax value
        } else {
            RESTAURANT_SALES_TAX = "0";
        }
        ArrayList<String> arrStringCart = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arrCartUpdated.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("menu_id", arrCartUpdated.get(i).getMenu_id());
                jsonObject.put("menu_price", arrCartUpdated.get(i).getMenu_price());
                jsonObject.put("menu_title", arrCartUpdated.get(i).getMenu_name());
                jsonObject.put("display_price", "$" + arrCartUpdated.get(i).getMenu_price());
                jsonObject.put("menu_type", "");
                jsonObject.put("quantity", arrCartUpdated.get(i).getQty());
                //  double strMyVal = getFormatedval(Double.parseDouble(arrCartUpdated.get(i).getMenu_price()) * Integer.valueOf(arrCartUpdated.get(i).getQty()));
                jsonObject.put("total", String.valueOf(getFormatedval(Double.parseDouble(arrCartUpdated.get(i).getMenu_price()) * Integer.valueOf(arrCartUpdated.get(i).getQty()))));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println(">>>arrStringCart :" + arrStringCart);
        String restoId = arrCartUpdated.get(0).getRestaurantId();   //Getting the resto ID form List which is saved already at resto Details List Menu
        String sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
        String tipAmt = tvTipAmount.getText().toString().replace("$", "").trim();
        String order_total = tvFinalPlaceOrderAmt.getText().toString().replace("Place your order: $ ", "").trim();
        String order_sub_total = tvOrderSubTotal.getText().toString().replace("$", "").trim();
        String customerComment = etCustomerComment.getText().toString().trim();
        if (!customerComment.isEmpty()) {
            editor.putString("cartComment", customerComment);
            editor.commit();
        }
        String username = sharedPreferences.getString("username", "");
        String card_cc_transaction_id = sharedPreferences.getString("card_cc_transaction_id", "");
        String card_cvv_num = sharedPreferences.getString("card_cvv_num", "");
        String card_user_id = sharedPreferences.getString("card_user_id", "");
        String card_exp_year = sharedPreferences.getString("card_exp_year", "");
        String card_card_id = sharedPreferences.getString("card_card_id", "");
        String card_exp_month = sharedPreferences.getString("card_exp_month", "");

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.format(new Date(System.currentTimeMillis()));
        String strOrderDate = formatter.format(new Date(System.currentTimeMillis()));
        strIsCardParmananent = sharedPreferences.getString("IsCardParmanant", "");

        if (strIsCardParmananent.equalsIgnoreCase("Yes")) {    // If payment is done by permanent CreditCard
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("menu_items", jsonArray);
                jsonObject.put("accesstoken", Config.accessToken);
                jsonObject.put("session_user_token", sessionUserTokan);
                jsonObject.put("name", username);
                jsonObject.put("restaurant_id", restoId);
                jsonObject.put("sales_tax", RESTAURANT_SALES_TAX);
                jsonObject.put("customer_comment", customerComment);
                jsonObject.put("tip_amount", tipAmt);
                jsonObject.put("order_total", order_total);
                jsonObject.put("sub_total", order_sub_total);
                jsonObject.put("order_date", strOrderDate);
                jsonObject.put("latitude", lattitude);
                jsonObject.put("longitude", longitude);
                //jsonObject.put("payment_mode", "");
                if (ActivityHome.strFlowType.equalsIgnoreCase("pickup")) {

                    jsonObject.put("order_type", Config.PICKUP);
                    jsonObject.put("payment_mode", "");
                    jsonObject.put("cc_transaction_id", card_cc_transaction_id);
                    jsonObject.put("card_num", card_cc_transaction_id);
                    jsonObject.put("cc_num", card_cvv_num);
                    jsonObject.put("user_id", card_user_id);
                    jsonObject.put("exp_month", card_exp_month);
                    jsonObject.put("exp_year", card_exp_year);
                    jsonObject.put("card_id", card_card_id);
                    jsonObject.put("latitude", lattitude);
                    jsonObject.put("longitude", longitude);

                    // after only keep the Credit card option for pickup  (11 Aug 2017)
                   /* if (mPaymentOption.equalsIgnoreCase("Cash")) {     //if payment mode is cash
                        jsonObject.put("payment_mode", Config.PAYMNTCASH);
                        jsonObject.put("cc_transaction_id", "");
                        jsonObject.put("card_num", "");
                        jsonObject.put("cc_num", "");
                        jsonObject.put("user_id", "");
                        jsonObject.put("exp_month", "");
                        jsonObject.put("exp_year", "");
                        jsonObject.put("card_id", "");
                    }else if(mPaymentOption.equalsIgnoreCase("Creditcard")){   //if payment mode is Creditcard
                        jsonObject.put("payment_mode", "");
                        jsonObject.put("cc_transaction_id", card_cc_transaction_id);
                        jsonObject.put("card_num", card_cc_transaction_id);
                        jsonObject.put("cc_num", card_cvv_num);
                        jsonObject.put("user_id", card_user_id);
                        jsonObject.put("exp_month", card_exp_month);
                        jsonObject.put("exp_year", card_exp_year);
                        jsonObject.put("card_id", card_card_id);
                    }*/

                } else {
                    jsonObject.put("order_type", Config.DELIVERY);
                    jsonObject.put("payment_mode", "");
                    jsonObject.put("cc_transaction_id", card_cc_transaction_id);
                    jsonObject.put("card_num", card_cc_transaction_id);
                    jsonObject.put("cc_num", card_cvv_num);
                    jsonObject.put("user_id", card_user_id);
                    jsonObject.put("exp_month", card_exp_month);
                    jsonObject.put("exp_year", card_exp_year);
                    jsonObject.put("card_id", card_card_id);
                    jsonObject.put("latitude", lattitude);
                    jsonObject.put("longitude", longitude);
                }
                String paymentURL = Config.BASE_URL + "user/payment";
                System.out.println(">>Place Order Params :" + jsonObject);

                new APIRequest(getActivity(), jsonObject, paymentURL, this, Config.API_PLACE_ORDER, Config.POST);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {  // If payment is done by temprory CreditCard

            //Note : userid & card id is empty in this case (if user use temp card without saving)
            String tempCard_user_id = "";
            String tempCard_id = "";
            String tempCardNo = sharedPreferences.getString("tempCardNo", "");
            String tempCvvNo = sharedPreferences.getString("tempCvvNo", "");
            String tempCardMonth = sharedPreferences.getString("tempCardMonth", "");
            String tempCardYear = sharedPreferences.getString("tempCardYear", "");
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("menu_items", jsonArray);
                jsonObject.put("accesstoken", Config.accessToken);
                jsonObject.put("session_user_token", sessionUserTokan);
                jsonObject.put("name", username);
                jsonObject.put("restaurant_id", restoId);
                jsonObject.put("sales_tax", RESTAURANT_SALES_TAX);
                jsonObject.put("customer_comment", customerComment);
                jsonObject.put("tip_amount", tipAmt);
                jsonObject.put("order_total", order_total);
                jsonObject.put("sub_total", order_sub_total);
                jsonObject.put("order_date", strOrderDate);
                jsonObject.put("latitude", lattitude);
                jsonObject.put("longitude", longitude);

                if (ActivityHome.strFlowType.equalsIgnoreCase("pickup")) {
                    jsonObject.put("order_type", Config.PICKUP);
                    if (mPaymentOption.equalsIgnoreCase("Cash")) {
                        //if payment mode is cash
                        jsonObject.put("payment_mode", Config.PAYMNTCASH);
                        jsonObject.put("cc_transaction_id", "");
                        jsonObject.put("card_num", "");
                        jsonObject.put("cc_num", "");
                        jsonObject.put("user_id", "");
                        jsonObject.put("exp_month", "");
                        jsonObject.put("exp_year", "");
                        jsonObject.put("card_id", "");
                        jsonObject.put("latitude", lattitude);
                        jsonObject.put("longitude", longitude);
                    } else if (mPaymentOption.equalsIgnoreCase("Creditcard")) {   //if payment mode is Creditcard

                        jsonObject.put("payment_mode", "");
                        jsonObject.put("cc_transaction_id", tempCardNo);
                        jsonObject.put("card_num", tempCardNo);
                        jsonObject.put("cc_num", tempCvvNo);
                        jsonObject.put("user_id", tempCard_user_id);
                        jsonObject.put("exp_month", tempCardMonth);
                        jsonObject.put("exp_year", tempCardYear);
                        jsonObject.put("card_id", tempCard_id);
                        jsonObject.put("latitude", lattitude);
                        jsonObject.put("longitude", longitude);
                    }
                } else {
                    System.out.println("## WITHOUT SAVE " + "DELIVERY ");
                    jsonObject.put("order_type", Config.DELIVERY);
                    jsonObject.put("payment_mode", "");
                    jsonObject.put("cc_transaction_id", tempCardNo);
                    jsonObject.put("card_num", tempCardNo);
                    jsonObject.put("cc_num", tempCvvNo);
                    jsonObject.put("user_id", tempCard_user_id);
                    jsonObject.put("exp_month", tempCardMonth);
                    jsonObject.put("exp_year", tempCardYear);
                    jsonObject.put("card_id", tempCard_id);
                    jsonObject.put("latitude", lattitude);
                    jsonObject.put("longitude", longitude);
                }

                String paymentURL = Config.BASE_URL + "user/payment";
                System.out.println(">> Place order Params :" + jsonObject);

                new APIRequest(getActivity(), jsonObject, paymentURL, this, Config.API_PLACE_ORDER, Config.POST);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSuccess(BaseResponse response) {
        System.out.println("###>>> : " + response);

        ResponsePlaceOrder responsePlaceOrder = (ResponsePlaceOrder) response;
        if (responsePlaceOrder.getIsSuccess()) {
            Toast.makeText(getActivity(), responsePlaceOrder.getErrMsg().toString(), Toast.LENGTH_SHORT).show();
            RealmController.getInstance().clearAll();
            ///////////////////////////////////////////////////////
            editor.putString("CartCnt", String.valueOf(0));
            editor.commit();
            ////////////////////////////////////////////////////////
            setCartCount();
            removeOtherPref();

            Intent intent = new Intent(getActivity(), UDashboardActivityNew.class);
            getActivity().startActivity(intent);
            editor.putString("tabPosition", "2");
            editor.commit();

        } else {
            Toast.makeText(getActivity(), responsePlaceOrder.getErrMsg().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {
        Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getCartTotalPrice(String mPrice) {
        double customeVal = 0;
        String FifteenPercent = getFormatedval(Double.parseDouble(mPrice) * (15.0f / 100.0f));
        String TwentyPercentage = getFormatedval(Double.parseDouble(mPrice) * (20.0f / 100.0f));

        String strCustomeVal = etCustomeVal.getText().toString().trim();
        if (!strCustomeVal.isEmpty()) {
            if (strCustomeVal.contains("$")) {
                if (!strCustomeVal.replace("$", "").trim().equalsIgnoreCase("")) {
                    customeVal = Double.parseDouble(strCustomeVal.replace("$", "").trim());
                }
            } else {
                if (!strCustomeVal.isEmpty()) {
                    customeVal = Double.parseDouble(strCustomeVal);
                }
            }
        }

        String mRestoTax = "";
        if (RESTAURANT_SALES_TAX.contains("$")) {
            mRestoTax = RESTAURANT_SALES_TAX.replace("$", "").trim();
        } else {
            mRestoTax = RESTAURANT_SALES_TAX;
        }

        String tax = getFormatedval((Double.parseDouble(mPrice) * Double.parseDouble(mRestoTax)) / 100);
        String strFinalTax = getFinalFormattedVal(String.valueOf(tax));
        tvSalesTax.setText(String.valueOf("$"+strFinalTax));
        Double salesTax = Double.parseDouble(strFinalTax);

        DecimalFormat dc = new DecimalFormat(".00");
        double val = 12.5;
        System.out.println("******price---**********"+mPrice);
        if(mPrice.equalsIgnoreCase("0.0"))
        {
            tvOrderSubTotal.setText("$0.00");
        }else
        {
            System.out.println("******price---**********"+dc.format(Double.parseDouble(mPrice)));
            String pr = dc.format(Double.parseDouble(mPrice));
            tvOrderSubTotal.setText("$"+pr);
            System.out.println("******price---**********"+pr);
        }

        tvFifteenPercentage.setText("$" + FifteenPercent + "");
        tvTwentyPercentage.setText("$" + TwentyPercentage + "");
        if (isTip15Clicked) {
            tvTipAmount.setText("$" + tvFifteenPercentage.getText().toString().trim().replace("$", "").trim());
        }
        if (isTip20Clicked) {
            tvTipAmount.setText("$" + tvTwentyPercentage.getText().toString().trim().replace("$", "").trim());
        }
        double finalAmtCheck = 0.0;
        if (isTipNoneClicked) {
            finalAmtCheck = salesTax + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
        } else if (isTip15Clicked) {
            finalAmtCheck = salesTax + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim()) + Double.parseDouble(FifteenPercent);
        } else if (isTip20Clicked) {
            finalAmtCheck = salesTax + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim()) + Double.parseDouble(TwentyPercentage);
        } else if (isTipCustomlicked) {
            if (customeVal != 0) {
                finalAmtCheck = salesTax + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim()) + customeVal;
            } else {
                finalAmtCheck = salesTax + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
            }
        }
        System.out.println("#>>>> -Double Amt - " + finalAmtCheck);
        String strFinalAmt = String.valueOf(getFormatedval(finalAmtCheck));
        String strFinalAmt1 = getFinalFormattedVal(strFinalAmt);
        tvFinalPlaceOrderAmt.setText("Place your order: $ " + strFinalAmt1);

    }

    private String getFinalFormattedVal(String strFinalAmt) {
        String mVal = "";
        String last2Chars = "";
        if (String.valueOf(strFinalAmt).contains(".")) {
            String[] arr = String.valueOf(strFinalAmt).split("\\.");
            String mainDigits = String.valueOf(arr[0]);
            String strLenght = String.valueOf(arr[1]);
            if (strLenght.length() > 2) {
                last2Chars = strLenght.substring(0, 2);
            } else {
                if (strLenght.length() == 2) {
                    last2Chars = strLenght;
                } else if (strLenght.length() < 2) {
                    last2Chars = strLenght + "0";
                }
            }
            mVal = mainDigits + "." + last2Chars;
        } else {
            mVal = strFinalAmt;
        }
        return mVal;
    }

    @Override
    public void updateCartCount(int cartCount) {
    }

    private void inItWidgets() {
        ///////////////////////////////////////////


        /////////////////////////////////////////
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tvCartRestoName = (TextView) header.findViewById(R.id.tvCartRestoName);
        lv_cart = (ListView) view.findViewById(R.id.lv_cartList);
        tv_clear = (TextViewBold) view.findViewById(R.id.tv_clear);
        //tvCartRestoName.setText(sharedPreferences.getString("clickedRestoName",""));
        llfiteenperdis = (LinearLayout) footer.findViewById(R.id.llfiteenperdis);
        lltwentyper = (LinearLayout) footer.findViewById(R.id.lltwentyper);
        llNoTip = (LinearLayout) footer.findViewById(R.id.llNoTip);
        llCustome = (LinearLayout) footer.findViewById(R.id.llCustome);

        tvFifteenPercentage = (TextView) footer.findViewById(R.id.tvFifteenPercentage);
        tvTwentyPercentage = (TextView) footer.findViewById(R.id.tvTwentyPercentage);
        etCustomeVal = (EditText) footer.findViewById(R.id.etCustomeVal);
        tvPercenategeType = (TextView) footer.findViewById(R.id.tvPercenategeType);
        tvTipAmount = (TextView) footer.findViewById(R.id.tvTipAmount);
        tvOrderSubTotal = (TextView) footer.findViewById(R.id.tvOrderSubTotal);
        tvFinalPlaceOrderAmt = (TextView) footer.findViewById(R.id.tvFinalPlaceOrderAmt);
        tvSalesTax = (TextView) footer.findViewById(R.id.tvSalesTax);
        tvCardDetails = (TextView) footer.findViewById(R.id.tvCardDetails);
        tvCartIsEmpty = (TextView) view.findViewById(R.id.cardemptytext);
        etCustomerComment = (EditText) footer.findViewById(R.id.etCustomerComment);

        if (Config.isShowRestoTitle.equalsIgnoreCase("Yes")) {
            tvCartRestoName.setVisibility(View.VISIBLE);
        } else {
            tvCartRestoName.setVisibility(View.GONE);
        }
        //tvEditCard = (TextView) footer.findViewById(R.id.tvEditCard);
    }

    private void getCartList() {
        double mPrice = 0;
        double actPrice = 0;
        if (RealmController.getInstance() != null) {
            hasItems = RealmController.getInstance().hasItemInDB();
            if (hasItems) {
                List<ModelRestaurantDetails> list = RealmController.getInstance().getRestaurantList();
                if (list.size() > 0) {
                    if (Integer.parseInt(list.get(0).getQty()) > 0) {
                        Config.isShowRestoTitle = "Yes";
                        tvCartRestoName.setVisibility(View.VISIBLE);
                        tvCartRestoName.setText(list.get(0).getRestoName());      // For getting Resto Name;
                    }
                    RESTAURANT_SALES_TAX = list.get(0).getResraurantTax();     // For getting Resto Sales tax;
                }
                for (int i = 0; i < list.size(); i++) {
                    String strQty = list.get(i).getQty();
                    if (!TextUtils.isEmpty(strQty)) {
                        int qty = Integer.parseInt(strQty);
                        if (qty > 0) {
                            arrCartUpdated.add(list.get(i));
                            System.out.println(">>> Size of arr Item changes :" + arrCartUpdated.size());
                            actPrice += Double.parseDouble(list.get(i).getMenu_price()) * Integer.parseInt((list.get(i).getQty()));
                            mPrice = getFormatedActval(actPrice);
                        }
                    }
                }
                if (arrCartUpdated.size() > 0) {
                    tvCartIsEmpty.setVisibility(View.GONE);
                    editor.putString("cartCount", arrCartUpdated.size() + "");
                    editor.commit();
                }

            }

        }

        DecimalFormat dc = new DecimalFormat(".00");

        System.out.println("******price&&---**********"+dc.format(mPrice));
        System.out.println("******price&---**********"+mPrice);
        if(mPrice == 0.0)
        {
            tvOrderSubTotal.setText("$0.00");
        }else
        {
            tvOrderSubTotal.setText("$" + dc.format(mPrice));
        }



        double FifteenPercent = mPrice * (15.0f / 100.0f);
        double TwentyPercentage = mPrice * (20.0f / 100.0f);
        System.out.println(" # >>> fifteen " + FifteenPercent + "---" + TwentyPercentage);

        String strFinal15New = getNewCartVal(String.valueOf(FifteenPercent));
        String strFinal20NEW = getNewCartVal(String.valueOf(TwentyPercentage));

        System.out.println(" # >>> fifteen after strFinal15New " + strFinal15New + "---" + strFinal20NEW);

        tvFifteenPercentage.setText("$" + strFinal15New + "");
        tvTwentyPercentage.setText("$" + strFinal20NEW + "");
    }

    private String getNewCartVal(String myVal) {

        String mVal = "";
        String last2Chars = "";

        if (myVal.contains(".")) {
            String[] arr = String.valueOf(myVal).split("\\.");
            String[] strArr = new String[2];

            strArr[0] = arr[0]; // 1
            strArr[1] = arr[1]; //

            String mainDigits = strArr[0];
            String strLenght = strArr[1];
            if (strLenght.length() > 2) {
                last2Chars = strLenght.substring(0, 2);
            } else {
                if (strLenght.length() == 2) {
                    last2Chars = strLenght;
                } else if (strLenght.length() < 2) {
                    last2Chars = strLenght + "0";
                }
            }
            mVal = mainDigits + "." + last2Chars;
        } else {
            mVal = myVal;
        }


        return mVal;

    }

    private void paymentInIt() {
        tvCardDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                    if (!etCustomerComment.getText().toString().trim().isEmpty()) {
                        editor.putString("cartComment", etCustomerComment.getText().toString().trim());
                        editor.commit();
                    }
                    ((UDashboardActivityNew) getActivity()).replaceFragment(new FragmentPaymentCardList());
                } else {
                    Toast.makeText(getActivity(), "Please login first to select card", Toast.LENGTH_SHORT).show();
                }
            }
        });
  /*      tvEditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strCrediTcardDetails = tvCardDetails.getText().toString().trim();
                if (!strCrediTcardDetails.equalsIgnoreCase("Select Card")) {
                    isEditCard = "Yes";
                    ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentAddPaymentCard());
                } else {
                    Toast.makeText(getActivity(), "Please select the card first, for edit", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        llNoTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hideSoftKeyboard(((UDashboardActivityNew) getActivity()), v); // MainActivity is the name of the class and v is the View parameter used in the button listener method onClick.

                OnNoTipClicked();
            }
        });

        llfiteenperdis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTipNoneClicked = false;
                isTip15Clicked = true;
                isTip20Clicked = false;
                isTipCustomlicked = false;
                etCustomeVal.setText("");
                etCustomeVal.clearFocus();

                llfiteenperdis.setBackground(getResources().getDrawable(R.drawable.edt_green_border));
                llNoTip.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                lltwentyper.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                llCustome.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));

                tvPercenategeType.setText("(15%)");

                tvTipAmount.setText("$" + tvFifteenPercentage.getText().toString().trim().replace("$", "").trim());
                String finalTax = getFinalTaxValue(tvOrderSubTotal.getText().toString().replace("$", "").trim(), RESTAURANT_SALES_TAX);
                String strFinalTax = getFinalFormattedVal(finalTax);
                tvSalesTax.setText(String.valueOf(strFinalTax));
                String mSalesTax = "";
                ;
                if (tvSalesTax.getText().toString().trim().contains("$")) {
                    mSalesTax = tvSalesTax.getText().toString().replace("$", "").trim();
                    ;
                } else {
                    mSalesTax = tvSalesTax.getText().toString().trim();
                }
                double salesTax = Double.parseDouble(mSalesTax);
                double orderSubTotal = Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
                double tip = Double.parseDouble(tvTipAmount.getText().toString().replace("$", "").trim());

                double finalPlaceOrderAmt = orderSubTotal + salesTax + tip;
                // tvFinalPlaceOrderAmt.setText("Place your order: $ " + getFormatedval(finalPlaceOrderAmt));

                String strFinalAmt = String.valueOf(getFormatedval(finalPlaceOrderAmt));
                String strFinalAmt1 = getFinalFormattedVal(strFinalAmt);
                tvFinalPlaceOrderAmt.setText("Place your order: $ " + strFinalAmt1);

                System.out.println(">> orderSubTotal " + orderSubTotal);
                System.out.println(">> salesTax " + salesTax);
                System.out.println(">> tip " + tip);
                System.out.println(">> finalPlaceOrderAmt " + finalPlaceOrderAmt);

            }
        });
        lltwentyper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hideSoftKeyboard(((UDashboardActivityNew) getActivity()), v);
                isTipNoneClicked = false;
                isTip15Clicked = false;
                isTip20Clicked = true;
                isTipCustomlicked = false;

                //String strRestoTax =  getFinalFormattedVal(String.valueOf(RESTAURANT_SALES_TAX));
                // tvSalesTax.setText(strRestoTax);
                etCustomeVal.setText("");
                etCustomeVal.clearFocus();

                lltwentyper.setBackground(getResources().getDrawable(R.drawable.edt_green_border));
                llNoTip.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                llfiteenperdis.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                llCustome.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));

                tvPercenategeType.setText("(20%)");
                tvTipAmount.setText("$" + tvTwentyPercentage.getText().toString().trim().replace("$", "").trim());

                String finalTax = getFinalTaxValue(tvOrderSubTotal.getText().toString().replace("$", "").trim(), RESTAURANT_SALES_TAX);

                String strFinalTax = getFinalFormattedVal(finalTax);
                tvSalesTax.setText(String.valueOf(strFinalTax));

                String mSalesTax = "";
                if (tvSalesTax.getText().toString().trim().contains("$")) {
                    mSalesTax = tvSalesTax.getText().toString().replace("$", "").trim();
                } else {
                    mSalesTax = tvSalesTax.getText().toString().trim();
                }
                double salesTax = Double.parseDouble(mSalesTax);

                double orderSubTotal = Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
                double tip = Double.parseDouble(tvTipAmount.getText().toString().replace("$", "").trim());

                double finalPlaceOrderAmt = orderSubTotal + salesTax + tip;
                tvFinalPlaceOrderAmt.setText("Place your order: $ " + getFormatedval(finalPlaceOrderAmt));

                System.out.println(">> orderSubTotal " + orderSubTotal);
                System.out.println(">> salesTax " + salesTax);
                System.out.println(">> tip " + tip);
                System.out.println(">> finalPlaceOrderAmt " + finalPlaceOrderAmt);
            }
        });

        etCustomeVal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                isTipNoneClicked = false;
                isTip15Clicked = false;
                isTip20Clicked = false;
                isTipCustomlicked = true;

                llCustome.setBackground(getResources().getDrawable(R.drawable.edt_green_border));
                lltwentyper.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                llNoTip.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                llfiteenperdis.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
                tvPercenategeType.setText("custom");

                mFlag = true;
            }
        });


        etCustomeVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mFlag = false;
                if (mFlag) {
                    Toast.makeText(getActivity(), "BEFORE", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String resStr = null;
                resStr = String.valueOf(s);
                System.out.println("********etCustomeVal*******"+etCustomeVal.length()+"**"+etCustomeVal.getText().toString());

                if(etCustomeVal.length() == 1 && etCustomeVal.getText().toString().equalsIgnoreCase("."))
                {
                    System.out.println("********etCustomeVal*******"+etCustomeVal.length()+"**"+etCustomeVal.getText().toString());
                    etCustomeVal.setText("");

                }else {
                    if (!resStr.isEmpty()) {
                        if (!resStr.equals(".") && !resStr.equals("")) {
                            int lengthWithDots = resStr.length();
                            int lengthWithoutDots = resStr.replace(".", "").length();

                            if (lengthWithDots - lengthWithoutDots >= 2) {
                                Toast.makeText(getActivity(), "Please enter valid custome tip", Toast.LENGTH_SHORT).show();
                                // etCustomeVal.setText("0");
                            } else {
                                tvTipAmount.setText("$" + s);
                                double finalAmtCheck = Double.parseDouble(tvSalesTax.getText().toString().replace("$", "")) + Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim()) + Double.parseDouble(resStr);
                                System.out.println(">>> finalAmtCheck " + finalAmtCheck);
                                //  tvFinalPlaceOrderAmt.setText("Place your order: $ " + getFormatedval(finalAmtCheck));
                                String strFinalAmt = String.valueOf(getFormatedval(finalAmtCheck));
                                String strFinalAmt1 = getFinalFormattedVal(strFinalAmt);
                                tvFinalPlaceOrderAmt.setText("Place your order: $ " + strFinalAmt1);

                            }
                        }

                    } else {

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void normalValInit() {
        isTipNoneClicked = true;
        isTip15Clicked = false;
        isTip20Clicked = false;
        isTipCustomlicked = false;
        etCustomeVal.setText("");
        llNoTip.setBackground(getResources().getDrawable(R.drawable.edt_green_border));
        lltwentyper.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        llfiteenperdis.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        llCustome.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        tvPercenategeType.setText(" ");
        tvTipAmount.setText("No Tip");

        String finalTax = getFinalTaxValue(tvOrderSubTotal.getText().toString().replace("$", "").trim(), RESTAURANT_SALES_TAX);

        System.out.println(">>> ### OrderSubTotal  " + tvOrderSubTotal.getText().toString().replace("$", "").trim());
        System.out.println(">>> ### RESTAURANT_SALES_TAX  " + RESTAURANT_SALES_TAX);
        System.out.println(">>> ### finalTax  " + finalTax);

        if (!finalTax.isEmpty()) {
            String strFinalTax = getFinalFormattedVal(finalTax);
            tvSalesTax.setText("$" + String.valueOf(strFinalTax));
        }
        if (!tvSalesTax.getText().toString().replace("$", "").isEmpty()) {
            double salesTax = Double.parseDouble(tvSalesTax.getText().toString().replace("$", ""));
            double orderSubTotal = Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
            double finalPlaceOrderAmt = orderSubTotal + salesTax; // No need to add tip amt here because it is zero
            String strFinalAmt = String.valueOf(getFormatedval(finalPlaceOrderAmt));
            String strFinalAmt1 = getFinalFormattedVal(strFinalAmt);
            tvFinalPlaceOrderAmt.setText("Place your order: $ " + strFinalAmt1);
        }
        // tvFinalPlaceOrderAmt.setText("Place your order: $ " +  getFormatedval(finalPlaceOrderAmt));

    }

    private void OnNoTipClicked() {
        isTipNoneClicked = true;
        isTip15Clicked = false;
        isTip20Clicked = false;
        isTipCustomlicked = false;

        llNoTip.setBackground(getResources().getDrawable(R.drawable.edt_green_border));
        lltwentyper.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        llfiteenperdis.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        llCustome.setBackground(getResources().getDrawable(R.drawable.edt_white_cellborder));
        etCustomeVal.setText("");
        etCustomeVal.clearFocus();

        tvPercenategeType.setText(" ");
        tvTipAmount.setText("No Tip");

        String finalTax = getFinalTaxValue(tvOrderSubTotal.getText().toString().replace("$", "").trim(), RESTAURANT_SALES_TAX);
        String strFinalTax = getFinalFormattedVal(finalTax);
        tvSalesTax.setText("$" + String.valueOf(strFinalTax));

        double orderSubTotal = Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", "").trim());
        double salesTax = Double.parseDouble(tvSalesTax.getText().toString().replace("$", ""));
        double finalPlaceOrderAmt = orderSubTotal + salesTax; // No need to add tip amt here because it is zero
        //  tvFinalPlaceOrderAmt.setText("Place your order: $ " +  getFormatedval(finalPlaceOrderAmt));

        String strFinalAmt = String.valueOf(getFormatedval(finalPlaceOrderAmt));
        String strFinalAmt1 = getFinalFormattedVal(strFinalAmt);
        tvFinalPlaceOrderAmt.setText("Place your order: $ " + strFinalAmt1);

        System.out.println(">> orderSubTotal " + orderSubTotal);
        System.out.println(">> salesTax " + salesTax);
        System.out.println(">> finalPlaceOrderAmt " + finalPlaceOrderAmt);
    }

    private String getFinalTaxValue(String subTotal, String RESTAURANT_SALES_TAX) {
        double subtotal = Double.parseDouble(tvOrderSubTotal.getText().toString().replace("$", ""));
        double restoTax = Double.parseDouble(this.RESTAURANT_SALES_TAX.replace("$", ""));
        double resultVal = (subtotal * restoTax) / 100;
        System.out.println(">> My result value before sending to the final is :" + resultVal);
        String finalTax = getFormatedval(resultVal);
        System.out.println(">> My Final Tax is method is :" + finalTax);
        return finalTax;
    }

    private void checkHasItemInDB() {
        boolean hasItem = RealmController.getInstance().hasItemInDB();
        if (hasItem) {
            String restoId = RealmController.getInstance().getRestaurantList().get(0).getRestaurantId();
            Toast.makeText(getActivity(), "YES", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "NO", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPaymentOptionDialog() {
        final String[] items = {"CREDIT CARD", "PAY AT RESTAURANT", "Cancel"};
        AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
        builder3.setTitle("Select Payment Options").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                if (items[pos].equalsIgnoreCase("PAY AT RESTAURANT")) {
                    System.out.println("## DIALOG CASH ");
                    mPaymentOption = "Cash";
                    // tvCardDetails.setText("Pay at Restaurant");
                    callPlaceOrderAPI();
                } else if (items[pos].equalsIgnoreCase("CREDIT CARD")) {
                    mPaymentOption = "CreditCard";

                    if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                        if (!tvCardDetails.getText().toString().trim().equalsIgnoreCase("Select Card")) {
                            callPlaceOrderAPI();
                        } else {
                            ((UDashboardActivityNew) getActivity()).replaceFragment(new FragmentPaymentCardList());
                        }

                    } else {
                        Toast.makeText(getActivity(), "Please login, for getting card details", Toast.LENGTH_SHORT).show();
                    }
                } else if (items[pos].equalsIgnoreCase("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder3.show();
    }


    private void saveSelectedCardDetails() {
        editor.putString("strSelectCardDetails", sharedPreferences.getString("getSelectedCard", ""));
        editor.putString("strSelectedCVV", sharedPreferences.getString("card_cvv_num", ""));
        editor.putString("strSelectedExpYear", sharedPreferences.getString("card_exp_year", ""));
        editor.putString("strSelectedExpMonth", sharedPreferences.getString("card_exp_month", ""));
        editor.putString("strSelectedZip", sharedPreferences.getString("card_zip_code", ""));
        editor.putString("strSelectedCardId", sharedPreferences.getString("card_card_id", ""));
        editor.putString("chckedStatus", "Yes");
        editor.commit();
    }

    private void saveTemperoryCardDetails() {
        editor.putString("strSelectCardDetails", sharedPreferences.getString("tempCardNo", ""));
        editor.putString("strSelectedCVV", sharedPreferences.getString("tempCvvNo", ""));
        editor.putString("strSelectedExpYear", sharedPreferences.getString("tempCardYear", ""));
        editor.putString("strSelectedExpMonth", sharedPreferences.getString("tempCardMonth", ""));
        editor.putString("strSelectedZip", sharedPreferences.getString("tempZipCode", ""));
        editor.putString("chckedStatus", "No");
        editor.commit();
    }

    private void removeOtherPref() {
        //Remove temp card data
        editor.remove("tempCardNo");
        editor.remove("tempCvvNo");
        editor.remove("tempCardMonth");
        editor.remove("tempCardYear");


        //Remove other part data
        editor.remove("CartCnt");
        editor.remove("cartCount");
        editor.remove("IsCardParmanant"); // For clear the key of card Saved in DB or not
        editor.remove("clickedRestoId");  // For clear the clicked restorant Id
        editor.remove("pickupCC");  //   For clear the creditcard pickup options
        editor.remove("cartComment");  //   For removing the cart Comment
        editor.remove("SelectedDefaultCard");//Default card issue

        editor.apply();
    }

    private void setCartCount() {
        List<ModelRestaurantDetails> cartCountSize = new ArrayList<ModelRestaurantDetails>();
        List<ModelRestaurantDetails> countSize = RealmController.getInstance().getRestaurantList();

        for (int i = 0; i < countSize.size(); i++) {
            if (Integer.parseInt(countSize.get(i).getQty()) > 0) {
                cartCountSize.add(countSize.get(i));
            }
        }
        if (cartCountSize.size() <= 0) {
            editor.remove("cartComment");
            hideRestoName();
        }
        System.out.println(">>> mCrtVl " + cartCountSize.size());
        ((UDashboardActivityNew) getContext()).updateCartCount(cartCountSize.size());
    }

    public void hideRestoName() {
        tvCartRestoName.setText("");
        tvCartRestoName.setVisibility(View.GONE);
        normalValInit();
    }

    private String getFormatedval(double currentLat) {
        String doubleVal = "";
        String last2Chars = "";
        if (String.valueOf(currentLat).contains(".")) {
            String[] arr = String.valueOf(currentLat).split("\\.");
            String[] arrStr = new String[2];
            arrStr[0] = arr[0]; // 1
            arrStr[1] = arr[1]; //
            String mainDigits = String.valueOf(arrStr[0]);
            String strLenght = String.valueOf(arrStr[1]);
            /*if(strLenght.length() > 2){
                last2Chars = strLenght.substring(0,2);
            }else{
                if(strLenght.length() == 2){
                    last2Chars =  strLenght;
                }else if(strLenght.length() < 2){
                    last2Chars =  strLenght+"0";
                }
            }
            String strFinal = mainDigits+"."+last2Chars; */

            System.out.println(">>>> Final output is  strFinal --- :" + String.format("%.2f", currentLat));
            doubleVal = String.format("%.2f", currentLat);
            System.out.println(">>>> Double return val is --- :" + doubleVal);
        } else {
            doubleVal = String.valueOf(currentLat);
        }

        return doubleVal;
    }

    private double getFormatedActval(double currentLat) {
        double doubleVal = 0.00;
        String last2Chars = "";
        if (String.valueOf(currentLat).contains(".")) {
            String[] arr = String.valueOf(currentLat).split("\\.");
            String[] arrStr = new String[2];
            arrStr[0] = arr[0]; // 1
            arrStr[1] = arr[1]; //
            String mainDigits = String.valueOf(arrStr[0]);
            String strLenght = String.valueOf(arrStr[1]);
            if (strLenght.length() > 2) {
                last2Chars = strLenght.substring(0, 2);
                System.out.println("**if****"+last2Chars);
            } else {
                if (strLenght.length() == 2) {
                    last2Chars = strLenght;
                    System.out.println("**else****"+last2Chars);
                } else if (strLenght.length() < 2) {
                    last2Chars = strLenght + "0";
                    System.out.println("**else if****"+last2Chars);
                }
            }
            String strFinal = mainDigits + "." + last2Chars;
            System.out.println(">>>> Final output is  strFinal --- :" + strFinal);
            doubleVal = Double.parseDouble(strFinal);
            System.out.println(">>>> Double return val is --- :" + doubleVal);
        } else {
            doubleVal = currentLat;
        }

        return doubleVal;
    }

    public static void hideSoftKeyboard(UDashboardActivityNew activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////




   /* public class GPSTracker extends Service implements LocationListener {

        private final Context mContext;
        // flag for GPS status
        boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        Location location; // location
        double latitude; // latitude
        double longitude; // longitude

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }

        *//**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * *//*
        public void stopUsingGPS() {
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(this);
            }
        }

        *//**
     * Function to get latitude
     * *//*
        public double getLatitude(){
            if(location != null){
                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        *//**
     * Function to get longitude
     * *//*
        public double getLongitude(){
            if(location != null){
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        *//**
     * Function to check GPS/wifi enabled
     * @return boolean
     * *//*
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        *//**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * *//*
        public void showSettingsAlert(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			System.out.println("lat>>>>>++++++" + latitude);
			System.out.println("lang>>>>>>+++++++" + longitude);

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

    }
*/

    InputFilter filter = new InputFilter() {
                final int maxDigitsBeforeDecimalPoint=3;
                final int maxDigitsAfterDecimalPoint=2;

                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                   // System.out.println("********source*******"+source.toString()+"**"+source.length());
                   // System.out.println("********etCustomeVal*******"+etCustomeVal.length()+"**"+etCustomeVal.getText().toString());

                    if(etCustomeVal.length() == 0 && etCustomeVal.getText().equals("."))
                    {
                        System.out.println("********etCustomeVal*******"+etCustomeVal.length()+"**"+etCustomeVal.getText().toString());
                       // etCustomeVal.setText("");
                        return "";
                    }else
                    {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source
                                .subSequence(start, end).toString());
                        if (!builder.toString().matches(
                                "(([0-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"

                        )) {
                            if(source.length()==0)
                                return dest.subSequence(dstart, dend);
                            return "";
                        }
                    }




                    return null;

                }
            };


}
