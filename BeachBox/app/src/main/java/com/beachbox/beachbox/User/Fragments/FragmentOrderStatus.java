package com.beachbox.beachbox.User.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityOrderStatus;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Model.responseCancelOrder.ResponseCancelOrder;
import com.beachbox.beachbox.User.Model.updateDeliveryLocation.UpdateDeliveryLocation;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitwarepc on 01-Aug-17.
 */

public class FragmentOrderStatus extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ViewSwitcher.ViewFactory, APIRequest.ResponseHandler {
    TextView tv_back, tv_resName, tv_resCreatedAt, tv_delivererName, tv_pendingOrder, tvCancelOrder, tvLastStatus;
    SharedPreferences sharedPreferences;
    final Handler handler = new Handler();
    SharedPreferences.Editor editor;
    private ArrayList<Object> arrOrderDetails;
    ConnectionDetector cd;
    boolean isInternetPresent;
    String orderId = "", delivererContact = "", delivererName = "", delivererImage = "";
    GoogleMap googleMap;
    MapView mapView;
    private double currentUserLat, currentUserLong;
    ImageView iv_delivereImage, iv_statusAccepted, iv_statusReady, iv_statusDelivering, imgCurrentLocation;
    ArrayList<LatLng> arrCustomerLocation;
    LinearLayout ll_delivererDetail, ll_contact;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    //String strDelevereLat = "", strDelevereLang = "";
    View view;
    double mLat, mLong;
    boolean isZoomedMap = true, isGoogleMapCalledAlready = true, isResumeCalled = false, isGPSEnabled = false;
    // double restoLatt,restoLangg ;
    Bundle mSavedInstance;
    Marker delivererMarker, currentMarker;
    LatLng currentLatLang;
    TextView txtReady, txtDelivering;
    String orderType = null;
    String mNavigation;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.activity_order_status, container, false);
            inIt();
            mSavedInstance = savedInstanceState;
            isGPSEnabled = checkGPSEnabled();
            if (isGPSEnabled) {
                OnClick();
            } else {
                showSettingsAlert();
            }


        }
        return view;
    }

    private void OnClick() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        mNavigation = sharedPreferences.getString("navigation", "");
        orderType = sharedPreferences.getString("orderType", "");
        if (mNavigation.equals("fromOrderHistory")) {

            //  ll_contact.setClickable(false);
            System.out.println("********hstry****");
            ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
            iv_statusAccepted.setImageResource(R.drawable.step_green);
            iv_statusReady.setImageResource(R.drawable.step_green);
            iv_statusDelivering.setImageResource(R.drawable.step_green);
            txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
            txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));

        } else if (mNavigation.equals("fromUpcomingOrder")) {
            String orderStatus = sharedPreferences.getString("orderStatus", "");

            System.out.println("## orderStatus is :" + orderStatus);

            if (orderStatus.equalsIgnoreCase("pending")) {
                iv_statusAccepted.setImageResource(R.drawable.step_gray);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
            } else if (orderStatus.equalsIgnoreCase("approved")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
            } else if (orderStatus.equalsIgnoreCase("foodready")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
            } else if (orderStatus.equalsIgnoreCase("on the way")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
            } else if (orderStatus.equalsIgnoreCase("delivered")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
            }
        } else if (mNavigation.equals("Notification")) {

            String orderStatus = sharedPreferences.getString("orderStatus", "");

            System.out.println("# noti status " + orderStatus);

            if (orderStatus.equalsIgnoreCase("pending")) {
                iv_statusAccepted.setImageResource(R.drawable.step_gray);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));

            } else if (orderStatus.equalsIgnoreCase("approved")) {
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
            } else if (orderStatus.equalsIgnoreCase("foodready")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));

            } else if (orderStatus.equalsIgnoreCase("on the way")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_grey));
            } else if (orderStatus.equalsIgnoreCase("delivered")) {
                ll_contact.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_filter_new_grey));
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
                txtReady.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
                txtDelivering.setBackgroundDrawable(getResources().getDrawable(R.drawable.rectangle_bar_green));
            }
        }
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferences.getString("navigation", "").equalsIgnoreCase("Notification")) {
                    editor.putString("tabPosition", "5");
                    editor.commit();
                    startActivity(new Intent(getActivity(), UDashboardActivityNew.class));
                } else {
                    editor.putString("tabPosition", "3");
                    editor.commit();
                    startActivity(new Intent(getActivity(), UDashboardActivityNew.class));
                }
            }
        });

        tvCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle("Cancel Order?");
                builder.setMessage("Are you sure you want to cancel the order?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cancelOrder();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                android.support.v7.app.AlertDialog alert = builder.create();
                alert.show();


            }


        });
        if (isInternetPresent) {
            // getDeliveryLocationByOrderIdAPI();
            callOrderDetailsAPI();
        } else {
            Toast.makeText(getActivity(), R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeliveryLocationByOrderIdAPI() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sharedPreferences.getString("userSessionTokan", ""));
            jsonObject.put("order_id", orderId);
            String updateDeliveryLocation = Config.BASE_URL + "user/deliveryUserLocation";

            System.out.println(" >>> Update Delivery Location params : " + jsonObject);
            new APIRequest(getActivity(), jsonObject, updateDeliveryLocation, this, Config.API_UPDATE_DELIVERY_BOY_LOCATION, Config.POST);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancelOrder() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sharedPreferences.getString("userSessionTokan", ""));
            jsonObject.put("order_id", orderId);

            String cancelOrderURL = Config.BASE_URL + "cancelorder";
            cancelAPIRequest(jsonObject, cancelOrderURL);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void inIt() {
        System.out.println("*********fragment********");
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        txtReady = (TextView) view.findViewById(R.id.txtReady);
        txtDelivering = (TextView) view.findViewById(R.id.txtDelivering);
        tv_back = (TextView) view.findViewById(R.id.tv_back);
        sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_resName = (TextView) view.findViewById(R.id.tv_resName);
        tv_delivererName = (TextView) view.findViewById(R.id.tv_delivererName);
        iv_delivereImage = (ImageView) view.findViewById(R.id.tv_delivererImage);
        iv_statusAccepted = (ImageView) view.findViewById(R.id.iv_statusAccepted);
        iv_statusReady = (ImageView) view.findViewById(R.id.iv_statusReady);
        iv_statusDelivering = (ImageView) view.findViewById(R.id.iv_statusDelivering);

        tvLastStatus = (TextView) view.findViewById(R.id.tvStatuslat);

        arrCustomerLocation = new ArrayList<>();
        tv_pendingOrder = (TextView) view.findViewById(R.id.tv_pendingOrder);
        ll_delivererDetail = (LinearLayout) view.findViewById(R.id.ll_delivererDetail);
        ll_contact = (LinearLayout) view.findViewById(R.id.ll_contact);
        tvCancelOrder = (TextView) view.findViewById(R.id.tvCancelOrder);
        orderId = sharedPreferences.getString("order_id", "");
        System.out.println(">>> Received Order Id : " + orderId);

        imgCurrentLocation = (ImageView) view.findViewById(R.id.imgCurrentLocation);
        // orderId = sharedPreferences.getString("order_id", "");

        imgCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("**********click*******");
                //getCurrentLocation();


            }
        });
    }

    private void cancelAPIRequest(JSONObject jsonObject, String cancelOrderURL) {
        new APIRequest(getActivity(), jsonObject, cancelOrderURL, this, Config.API_CANCEL_PENDING_ORDER, Config.POST);
    }

    @Override
    public void onSuccess(BaseResponse response) {
        if (response.getApiName() == Config.API_CANCEL_PENDING_ORDER) {
            ResponseCancelOrder cancelresponse = (ResponseCancelOrder) response;
            if (cancelresponse.getIsSuccess()) {
                editor.putString("tabPosition", "3");
                editor.commit();
                Intent i = new Intent(getActivity(), UDashboardActivityNew.class);
                startActivity(i);
            }
        }
        if (response.getApiName() == Config.API_UPDATE_DELIVERY_BOY_LOCATION) {
            UpdateDeliveryLocation updateDeliveryLocation = (UpdateDeliveryLocation) response;
            if (updateDeliveryLocation.getIsSuccess()) {

                System.out.println(">>> Delivery Lat in service : " + String.valueOf(updateDeliveryLocation.getDelivery().getLatitude()));
                System.out.println(">>> Delivery Long in service : " + String.valueOf(updateDeliveryLocation.getDelivery().getLongitude()));

                editor.putString("deliveryLat", String.valueOf(updateDeliveryLocation.getDelivery().getLatitude()));
                editor.putString("deliveryLong", String.valueOf(updateDeliveryLocation.getDelivery().getLongitude()));
                editor.commit();

                callOrderDetailsAPI();  //Call Order Details API any how even  update location API response is ok or not

            } else {
                System.out.println(">>> In Fail update location");
                callOrderDetailsAPI();
            }
        }
    }

    private void callOrderDetailsAPI() {
        if (isInternetPresent) {
            new OrderDetails().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"order_id\":\"" + orderId + "\"}");
        } else {
            Toast.makeText(getActivity(), R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
        }
    }

    class OrderDetails extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "orderdetails")
                    .post(body)
                    .build();

            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>Orders details result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if (is_success == true) {

                        JSONObject orderDetails = jsonObject.getJSONObject("orderdetails");

                        editor.putString("restoLat", orderDetails.getString("restaurant_lat"));
                        editor.putString("restoLong", orderDetails.getString("restaurant_lng"));
                        editor.commit();

                        JSONObject delivery_user = jsonObject.getJSONObject("delivery_user");
                        delivererName = delivery_user.getString("user_name");
                        delivererContact = delivery_user.getString("user_phone");
                        delivererImage = delivery_user.getString("user_pic");
                        editor.putString("deliveryLat", delivery_user.getString("latitude"));
                        editor.putString("deliveryLong", delivery_user.getString("longitude"));
                        editor.commit();
                        setData();
                        JSONArray customer_location = jsonObject.getJSONArray("customer_location");
                        JSONObject custObject = customer_location.getJSONObject(0);
                        tv_pendingOrder.setText("Your order is " + custObject.getString("status"));

                        if (custObject.getString("status").equalsIgnoreCase("pending")) {
                            tvCancelOrder.setVisibility(View.VISIBLE);
                        }
                        isGoogleMapCalledAlready = true;

                        showMap(mSavedInstance);

                    } else {
                        p.dismiss();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(getActivity(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setData() {
        if (delivererName.equals("null")) {
            tv_pendingOrder.setVisibility(View.VISIBLE);
        } else {
            ll_delivererDetail.setVisibility(View.VISIBLE);
            ll_contact.setVisibility(View.VISIBLE);
            tv_delivererName.setText(delivererName);
            if (delivererImage == null || delivererImage.equals("") || delivererImage.equals("null")) {
                iv_delivereImage.setImageResource(R.drawable.blank_resturant);
            } else {
                Glide.with(getActivity())
                        .load(delivererImage)
                        .into(iv_delivereImage);
            }
            ll_contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNavigation.equals("fromOrderHistory")) {
                    } else {
                        String orderStatus = sharedPreferences.getString("orderStatus", "");
                        if(orderStatus.equalsIgnoreCase("on the way")) {
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                            builder.setTitle("Call");
                            builder.setMessage("Are you sure you want to call?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            checkCallingPermission();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            android.support.v7.app.AlertDialog alert = builder.create();
                            alert.show();
                        }

                    }


                }
            });
        }
    }

    private void checkCallingPermission() {
        if (delivererContact != null || delivererContact.equalsIgnoreCase("null") || delivererContact.equalsIgnoreCase("")) {
            if (checkPermission(Manifest.permission.CALL_PHONE)) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + delivererContact)));
            }
            if (!checkPermission(Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
            }
        } else {
            Toast.makeText(getActivity(), "Contact no not available of deliverer.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMap(Bundle savedInstanceState) {

        System.out.println("**************showmap111********");
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        googleMap = mapView.getMap();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        //googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationChangeListener(myLocationChangeListener());
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        //getCurrentLocation();

    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (isGoogleMapCalledAlready) {

                    Double finalDoubleLat = null, finalDoubleLong = null;
                    currentUserLat = location.getLatitude();
                    currentUserLong = location.getLongitude();

                    System.out.println("********OnMyLocationChangeListener********");
                    //Toast.makeText(getActivity(), ""+currentUserLat+"--"+currentUserLong, Toast.LENGTH_SHORT).show();

                    currentLatLang = new LatLng(currentUserLat, currentUserLong);
                    // Showing the current location in Google Map
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLang));
// Zoom in the Google Map
                    //googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                    //addCurrentMarker(currentLatLang);

                    if (orderType.equalsIgnoreCase("pickup")) {
                        tvLastStatus.setText("Picked up");
                        String receivedRestoLat = sharedPreferences.getString("restoLat", "");
                        String receivedRestoLong = sharedPreferences.getString("restoLong", "");
                        System.out.println(">> pickup :" + receivedRestoLat + "--" + receivedRestoLong);
                        if (receivedRestoLat != null && !String.valueOf(receivedRestoLat).equals("") && !String.valueOf(receivedRestoLat).equals("null")) {
                            finalDoubleLat = Double.parseDouble(receivedRestoLat);
                            finalDoubleLong = Double.parseDouble(receivedRestoLong);
                            LatLng restoMarker = new LatLng(finalDoubleLat, finalDoubleLong);
                            addRestaurantMarker(restoMarker);
                        }

                    } else if (orderType.equalsIgnoreCase("delivery")) {
                        String receivedLat = sharedPreferences.getString("deliveryLat", "");
                        String receivedLang = sharedPreferences.getString("deliveryLong", "");
                        System.out.println(">> Delivery :" + receivedLat + "--" + receivedLang);

                        tvLastStatus.setText("Delivering");

                        if (receivedLat != null && !String.valueOf(receivedLat).equals("") && !String.valueOf(receivedLat).equals("null")) {
                            finalDoubleLat = Double.parseDouble(receivedLat);
                            finalDoubleLong = Double.parseDouble(receivedLang);
                            LatLng deliveryLatLong = new LatLng(finalDoubleLat, finalDoubleLong);
                            if (mNavigation.equals("fromOrderHistory")) {

                            } else {
                                ///////on the way
                                String orderStatus = sharedPreferences.getString("orderStatus", "");

                                System.out.println("## orderStatus is :" + orderStatus);
                                if (orderStatus.equalsIgnoreCase("on the way"))// || (orderStatus.equalsIgnoreCase("delivered"))) {
                                {
                                    System.out.println("**************on the way");
                                    addDeliveryMarker(deliveryLatLong);
                                }

                            }
                        }
                    } else {
                        tvLastStatus.setText("Completed");
                        zoomOnCurrentLocation();
                    }
                    // Drwawing the route/path here
                    if (finalDoubleLat != null && !String.valueOf(finalDoubleLat).equals("") && !String.valueOf(finalDoubleLat).equals("null")) {
                        System.out.println(">>> In if  ");
                        LatLng sLatLong = new LatLng(currentUserLat, currentUserLong);
                        LatLng dLatLong = new LatLng(finalDoubleLat, finalDoubleLong);
                        //drawSourceDestinationPath(sLatLong, dLatLong);
                        zoomOnCurrentLocation();

                        // If Ordertype is delivery then only fetch the delivery boy location
                        System.out.println("********orderType***********" + orderType);
                        if (orderType.equalsIgnoreCase("delivery")) {
                            System.out.println("********orderType***********if");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isInternetPresent) {
                                        if (mNavigation.equals("fromOrderHistory")) {

                                        } else {
                                            new FetchDeliveryBoyLocation().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"order_id\":\"" + orderId + "\",\"session_user_token\":\"" + sharedPreferences.getString("userSessionTokan", "") + "\"}");

                                        }
                                        //new FetchDeliveryBoyLocation().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"order_id\":\"" + orderId + "\",\"session_user_token\":\"" + sharedPreferences.getString("userSessionTokan", "") + "\"}");
                                    }
                                    handler.postDelayed(this, 20000);
                                }
                            }, 20000);
                        }

                    } else {
                        System.out.println(">>> In else");
                        LatLng sLatLong = new LatLng(currentUserLat, currentUserLong);
                        //  drawSourceDestinationPath(sLatLong, sLatLong);
                        zoomOnCurrentLocation();
                    }
                    isGoogleMapCalledAlready = false;
                }
            }
        };
    }

    private void zoomOnCurrentLocation() {
        if (isZoomedMap) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentUserLat, currentUserLong), 18));
            isZoomedMap = false;
        }
    }

    private void addCurrentMarker(LatLng currentLatLang) {
        if (currentMarker != null) {
            currentMarker.remove();
        }

        currentMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLatLang)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greenmap))
                .snippet("User Current Location")
                .title("Current"));
    }

    private void addDeliveryMarker(LatLng deliveryLatLong) {
        if (delivererMarker != null) {
            delivererMarker.remove();
        }

        delivererMarker = googleMap.addMarker(new MarkerOptions()
                .position(deliveryLatLong)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greymap))
                .snippet("Deliverer Location")
                .title("Deliverer"));
    }

    private void addRestaurantMarker(LatLng restoMarker) {
        delivererMarker = googleMap.addMarker(new MarkerOptions()
                .position(restoMarker)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greymap))
                .title("Restaurant"));
    }

    private void drawSourceDestinationPath(LatLng source, LatLng destination) {
        String directionURL = getMapsApiDirectionsUrl(source, destination);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(directionURL);
    }


    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);

            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    public class MapHttpConnection {
        @SuppressLint("LongLogTag")
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();

            } catch (Exception e) {
                Log.d("Exception while reading url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }

    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;

        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }


                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.BLACK);
            }
            if (polyLineOptions != null) {
                googleMap.addPolyline(polyLineOptions);

            }

        }
    }

    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onFailure(BaseResponse response) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }
                return;
        }
    }

    private String getFormatedval(String currentLat) {
        String doubleVal = "";
        String last2Chars;
        if (String.valueOf(currentLat).contains(".")) {
            String[] arr = String.valueOf(currentLat).split("\\.");
            long[] intArr = new long[2];
            intArr[0] = Long.parseLong(arr[0]); // 1
            intArr[1] = Long.parseLong(arr[1]); //
            String mainDigits = String.valueOf(intArr[0]);
            String strLenght = String.valueOf(intArr[1]);
            if (strLenght.length() > 4) {
                last2Chars = strLenght.substring(0, 4);
            } else {
                last2Chars = strLenght;
            }
            String strFinal = mainDigits + "." + last2Chars;
            doubleVal = strFinal;
        } else {
            doubleVal = currentLat;
        }
        return doubleVal;
    }

    class FetchDeliveryBoyLocation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e(" >> Background params", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "user/deliveryUserLocation")
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>>Update Location Result background :" + s);
            if (s != null) {
                try {
                    //isZoomedMap  = true;
                    String strDLat = null, strDLang = null;
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");
                    if (is_success) {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("delivery");
                        strDLat = jsonObject1.getString("latitude");
                        strDLang = jsonObject1.getString("longitude");
                        if (strDLat != null && !strDLat.equals("null") && !strDLat.isEmpty()) {
                            editor.putString("deliveryLat", strDLat);
                            editor.putString("deliveryLong", strDLang);
                            editor.commit();
                        }

                        if (!strDLat.isEmpty() && strDLat != null && !strDLat.equalsIgnoreCase("null")) {
                            double dLat = Double.parseDouble(strDLat);
                            double dLang = Double.parseDouble(strDLang);
                            System.out.println(">>> I am at the sucess ");

                            LatLng deliveryLatLong = new LatLng(dLat, dLang);
                            if (delivererMarker != null) {
                                delivererMarker.remove();
                            }
                            if (currentMarker != null) {
                                currentMarker.remove();
                            }


                            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLang));
// Zoom in the Google Map
                            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                            //addCurrentMarker(currentLatLang);

                            //////////////////////on the way
                            String orderStatus = sharedPreferences.getString("orderStatus", "");

                            System.out.println("## orderStatus is :" + orderStatus);
                            if (orderStatus.equalsIgnoreCase("on the way"))// || (orderStatus.equalsIgnoreCase("delivered")))
                            {
                                System.out.println("**************on the way");
                                addDeliveryMarker(deliveryLatLong);
                            }

                            System.out.println(">>>> Before gooing to add path");
                            //drawSourceDestinationPath(currentLatLang, deliveryLatLong);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps_enabled && network_enabled) {
                resVal = true;
            } else {
                resVal = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resVal;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isResumeCalled = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }


  /*  private void getCurrentLocation() {

        System.out.println("**getCurrentLocation fragment********");
        GPSTracker mGPS = new GPSTracker(getActivity());

        if (mGPS.canGetLocation) {


            mGPS = new GPSTracker(getActivity());
            mLat = mGPS.getLatitude();
            mLong = mGPS.getLongitude();
            System.out.println("**********" + mLat + "**" + mLong);

            LatLng cur_Latlng = new LatLng(mLat, mLong); // giving your marker to zoom to your location area.
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(cur_Latlng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            //setMyLocation = true;
            // latitude = location.getLatitude();
            //longitude = location.getLongitude();
            Double finalDoubleLat = null, finalDoubleLong = null;
            currentUserLat = mLat;
            currentUserLong = mLong;

            //Toast.makeText(getActivity(), ""+currentUserLat+"--"+currentUserLong, Toast.LENGTH_SHORT).show();

            //currentLatLang = new LatLng(currentUserLat, currentUserLong);
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLang));
// Zoom in the Google Map
            // googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            //addCurrentMarker(currentLatLang);

            if (orderType.equalsIgnoreCase("pickup")) {
                tvLastStatus.setText("Picked up");
                String receivedRestoLat = sharedPreferences.getString("restoLat", "");
                String receivedRestoLong = sharedPreferences.getString("restoLong", "");
                System.out.println(">> pickup :" + receivedRestoLat + "--" + receivedRestoLong);
                if (receivedRestoLat != null && !String.valueOf(receivedRestoLat).equals("") && !String.valueOf(receivedRestoLat).equals("null")) {
                    finalDoubleLat = Double.parseDouble(receivedRestoLat);
                    finalDoubleLong = Double.parseDouble(receivedRestoLong);
                    LatLng restoMarker = new LatLng(finalDoubleLat, finalDoubleLong);
                    addRestaurantMarker(restoMarker);
                }

            } else if (orderType.equalsIgnoreCase("delivery")) {
                String receivedLat = sharedPreferences.getString("deliveryLat", "");
                String receivedLang = sharedPreferences.getString("deliveryLong", "");
                System.out.println(">> Delivery :" + receivedLat + "--" + receivedLang);

                tvLastStatus.setText("Delivering");

                if (receivedLat != null && !String.valueOf(receivedLat).equals("") && !String.valueOf(receivedLat).equals("null")) {
                    finalDoubleLat = Double.parseDouble(receivedLat);
                    finalDoubleLong = Double.parseDouble(receivedLang);
                    LatLng deliveryLatLong = new LatLng(finalDoubleLat, finalDoubleLong);
                    addDeliveryMarker(deliveryLatLong);
                }
            } else {
                tvLastStatus.setText("Completed");
                zoomOnCurrentLocation();
            }
            // Drwawing the route/path here
            if (finalDoubleLat != null && !String.valueOf(finalDoubleLat).equals("") && !String.valueOf(finalDoubleLat).equals("null")) {
                System.out.println(">>> In if  ");
                LatLng sLatLong = new LatLng(currentUserLat, currentUserLong);
                LatLng dLatLong = new LatLng(finalDoubleLat, finalDoubleLong);
                //drawSourceDestinationPath(sLatLong, dLatLong);
                zoomOnCurrentLocation();

                // If Ordertype is delivery then only fetch the delivery boy location
                System.out.println("********orderType***********" + orderType);
                if (orderType.equalsIgnoreCase("delivery")) {
                    System.out.println("********orderType***********if");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isInternetPresent) {
                                new FetchDeliveryBoyLocation().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"order_id\":\"" + orderId + "\",\"session_user_token\":\"" + sharedPreferences.getString("userSessionTokan", "") + "\"}");
                            }
                            handler.postDelayed(this, 20000);
                        }
                    }, 20000);
                }

            } else {
                System.out.println(">>> In else");
                LatLng sLatLong = new LatLng(currentUserLat, currentUserLong);
                //  drawSourceDestinationPath(sLatLong, sLatLong);
                zoomOnCurrentLocation();
            }
            isGoogleMapCalledAlready = false;
        }

    }

*/
    public final class GPSTracker implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        public boolean isGPSEnabled = false;

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

        /**
         * Function to get the user's current location
         *
         * @return
         */
        public Location getLocation() {
            try {
                System.out.println("In getlocation.............");
                locationManager = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                Log.v("isGPSEnabled", "=" + isGPSEnabled);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

                if (isGPSEnabled == false && isNetworkEnabled == false) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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

        /**
         * Stop using GPS listener Calling this function will stop using GPS in your
         * app
         */
        public void stopUsingGPS() {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         */
        public double getLatitude() {
            if (location != null) {

                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            if (location != null) {

                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/wifi enabled
         *
         * @return boolean
         */
        public boolean canGetLocation() {
            return this.canGetLocation;
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

    }


}
