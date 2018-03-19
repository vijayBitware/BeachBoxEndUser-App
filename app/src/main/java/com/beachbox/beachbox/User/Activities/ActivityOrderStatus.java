package com.beachbox.beachbox.User.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Fragments.CustomMapView;
import com.beachbox.beachbox.User.Model.ModelOrderDetail;
import com.beachbox.beachbox.User.Model.responseCancelOrder.ResponseCancelOrder;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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

import static android.R.attr.dial;

/**
 * Created by bitware on 23/3/17.
 */
/**
 * This class for tracking delivery boy and order status.
 */

public class ActivityOrderStatus extends Activity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ViewSwitcher.ViewFactory,APIRequest.ResponseHandler {
    double deliveryLat,deliveryLang ;
    TextView tv_back, tv_resName, tv_resCreatedAt, tv_delivererName, tv_pendingOrder,tvCancelOrder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ArrayList<Object> arrOrderDetails;
    ConnectionDetector cd;
    boolean isInternetPresent;
    String orderId = "", orderStatus = "", delivererContact = "", delivererName = "", delivererImage = "";
    GoogleMap googleMap;
    MapView mapView;
    private double latitude, longitude;
    ImageView iv_delivereImage, iv_statusAccepted, iv_statusReady, iv_statusDelivering, imgCurrentLocation;
    ArrayList<LatLng> arrCustomerLocation;
    LinearLayout ll_delivererDetail, ll_contact;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    String strDelevereLat = "",strDelevereLang = "" ;
    double mLat, mLong;
    Bundle mSavedInstance;
    Marker userMarker, deliveryMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        System.out.println("*********activity********");
        mSavedInstance = savedInstanceState;
        init();
        //showMap(mSavedInstance);

        if (sharedPreferences.getString("navigation", "").equals("fromOrderHistory")) {
            iv_statusAccepted.setImageResource(R.drawable.step_green);
            iv_statusReady.setImageResource(R.drawable.step_green);
            iv_statusDelivering.setImageResource(R.drawable.step_green);
        } else if (sharedPreferences.getString("navigation", "").equals("fromUpcomingOrder")) {
            orderStatus = sharedPreferences.getString("orderStatus", "");
            if (orderStatus.equalsIgnoreCase("pending")) {
                iv_statusAccepted.setImageResource(R.drawable.step_gray);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
            } else if (orderStatus.equalsIgnoreCase("approved")) {
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_gray);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
            } else if (orderStatus.equalsIgnoreCase("foodready")) {
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_gray);
            } else if (orderStatus.equalsIgnoreCase("on the way")) {
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
            } else if (orderStatus.equalsIgnoreCase("delivered")) {
                iv_statusAccepted.setImageResource(R.drawable.step_green);
                iv_statusReady.setImageResource(R.drawable.step_green);
                iv_statusDelivering.setImageResource(R.drawable.step_green);
            }
        }

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("tabPosition", "2");
                editor.commit();
                startActivity(new Intent(ActivityOrderStatus.this, UDashboardActivityNew.class));
            }
        });

        tvCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("accesstoken",Config.accessToken);
                    jsonObject.put("session_user_token",sharedPreferences.getString("userSessionTokan",""));
                    jsonObject.put("order_id", orderId);

                    String cancelOrderURL = Config.BASE_URL+"cancelorder";
                    cancelAPIRequest(jsonObject, cancelOrderURL);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        });

        if (isInternetPresent) {
            new OrderDetails().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"order_id\":\"" + orderId + "\"}");
        } else {
            Toast.makeText(ActivityOrderStatus.this, R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
        }
    }

    //cancel order API
    private void cancelAPIRequest(JSONObject jsonObject, String cancelOrderURL) {
        new APIRequest(ActivityOrderStatus.this, jsonObject, cancelOrderURL, this, Config.API_CANCEL_PENDING_ORDER, Config.POST);
    }

    //initialization
    private void init() {
        cd = new ConnectionDetector(ActivityOrderStatus.this);
        isInternetPresent = cd.isConnectingToInternet();
        tv_back = (TextView) findViewById(R.id.tv_back);
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_resName = (TextView) findViewById(R.id.tv_resName);
        tv_delivererName = (TextView) findViewById(R.id.tv_delivererName);
        iv_delivereImage = (ImageView) findViewById(R.id.tv_delivererImage);
        iv_statusAccepted = (ImageView) findViewById(R.id.iv_statusAccepted);
        iv_statusReady = (ImageView) findViewById(R.id.iv_statusReady);
        iv_statusDelivering = (ImageView) findViewById(R.id.iv_statusDelivering);
        arrCustomerLocation = new ArrayList<>();
        tv_pendingOrder = (TextView) findViewById(R.id.tv_pendingOrder);
        ll_delivererDetail = (LinearLayout) findViewById(R.id.ll_delivererDetail);
        ll_contact = (LinearLayout) findViewById(R.id.ll_contact);
        tvCancelOrder = (TextView) findViewById(R.id.tvCancelOrder);
        imgCurrentLocation = (ImageView) findViewById(R.id.imgCurrentLocation);
        orderId = sharedPreferences.getString("order_id", "");

        imgCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("**********click*******");
               // getCurrentLocation();


            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    //show map
    private void showMap(Bundle savedInstanceState) {
        System.out.println("**************showmap********");
        mapView = (CustomMapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.setDuplicateParentStateEnabled(false);
        mapView.onResume();
        googleMap = mapView.getMap();

        if (ActivityCompat.checkSelfPermission(ActivityOrderStatus.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityOrderStatus.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnMyLocationChangeListener(myLocationChangeListener());
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

       // getCurrentLocation();
    }

    //track db boy current location and place on map
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.e("Location >> ", "Lattitude-" + latitude + " " + "Logitude-" + longitude);
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 18));


                if(!strDelevereLat.isEmpty() || !strDelevereLat.equalsIgnoreCase("")){
                     deliveryLat = Double.parseDouble(strDelevereLat);
                     deliveryLang = Double.parseDouble(strDelevereLang);
                    System.out.println(">> Delivery lat/long after getting del lat lang: "+deliveryLat+"--"+deliveryLang);
                }else{
                     deliveryLat = latitude;
                     deliveryLang = longitude;
                    System.out.println(">> Delivery lat/long as a current latlang: "+deliveryLat+"--"+deliveryLang);

                }
                double userLat = Double.parseDouble(sharedPreferences.getString("userLat",""));
                double userLang = Double.parseDouble(sharedPreferences.getString("userLang",""));

                if (deliveryMarker != null) {
                    deliveryMarker.remove();
                }

                deliveryMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(deliveryLat, deliveryLang))
                        .title("Deliverer"));


                if (userMarker != null) {
                    userMarker.remove();
                }

                userMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(userLat, userLang))
                        .title("User"));

                LatLng source = new LatLng(deliveryLat,deliveryLang);
                LatLng destination = new LatLng(userLat,userLang);
                ArrayList<LatLng> mapList = new ArrayList<>();
                mapList.add(source);
                mapList.add(destination);
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                for (int z = 0; z < mapList.size(); z++) {
                    LatLng point = mapList.get(z);
                    options.add(point);
                }
                googleMap.addPolyline(options);
            }
        };
    }

    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    class OrderDetails extends AsyncTask<String, Void, String> {

        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityOrderStatus.this);
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

                        JSONObject delivery_user = jsonObject.getJSONObject("delivery_user");
                        delivererName = delivery_user.getString("user_name");
                        delivererContact = delivery_user.getString("user_phone");
                        delivererImage = delivery_user.getString("user_pic");
                        strDelevereLat = delivery_user.getString("latitude");
                        strDelevereLang = delivery_user.getString("longitude");
                        showMap(mSavedInstance);
                        setData();
                        JSONArray customer_location = jsonObject.getJSONArray("customer_location");
                        for (int i = 0; i < customer_location.length(); i++) {
                            JSONObject customerLocation = customer_location.getJSONObject(i);
                        }
                    } else {
                        p.dismiss();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(ActivityOrderStatus.this, "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //set data
    private void setData() {
        if (delivererName.equals("null")) {
            tv_pendingOrder.setVisibility(View.VISIBLE);
            tvCancelOrder.setVisibility(View.VISIBLE);
        } else {
            ll_delivererDetail.setVisibility(View.VISIBLE);
            ll_contact.setVisibility(View.VISIBLE);
            tv_delivererName.setText(delivererName);
            if (delivererImage == null ||delivererImage.equals("") || delivererImage.equals("null")) {
                iv_delivereImage.setImageResource(R.drawable.blank_resturant);
            } else {
                Glide.with(ActivityOrderStatus.this)
                        .load(delivererImage)
                        .into(iv_delivereImage);
            }
            ll_contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delivererContact != null || delivererContact.equalsIgnoreCase("null") || delivererContact.equalsIgnoreCase("")){
                        if (checkPermission(Manifest.permission.CALL_PHONE)) {
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+delivererContact)));
                        }
                         if (!checkPermission(Manifest.permission.CALL_PHONE)) {
                            ActivityCompat.requestPermissions(ActivityOrderStatus.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        }
                    }else{
                        Toast.makeText(ActivityOrderStatus.this, "Contact no not available of deliverer.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    @Override
    public void onSuccess(BaseResponse response) {
        ResponseCancelOrder cancelOrder = (ResponseCancelOrder)response;

        if(cancelOrder.getIsSuccess()){
            Toast.makeText(this, cancelOrder.getErrMsg(), Toast.LENGTH_SHORT).show();
            editor.putString("tabPosition", "3");
            editor.commit();
            startActivity(new Intent(ActivityOrderStatus.this, UDashboardActivityNew.class));
            finish();

        }else{
            Toast.makeText(this, cancelOrder.getErrMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }
                return;
        }
    }

    private void getCurrentLocation() {

        System.out.println("**getCurrentLocation activity********");
        GPSTracker mGPS = new GPSTracker(ActivityOrderStatus.this);

        if (mGPS.canGetLocation) {


            mGPS = new GPSTracker(ActivityOrderStatus.this);
            mLat = mGPS.getLatitude();
            mLong = mGPS.getLongitude();
            System.out.println("**********" + mLat + "**" + mLong);
            //setMyLocation = true;
           // latitude = location.getLatitude();
            //longitude = location.getLongitude();
            Log.e("Location >> ", "Lattitude-" + latitude + " " + "Logitude-" + longitude);
            googleMap.clear();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLat, mLong), 10));


            if(!strDelevereLat.isEmpty() || !strDelevereLat.equalsIgnoreCase("")){
                deliveryLat = Double.parseDouble(strDelevereLat);
                deliveryLang = Double.parseDouble(strDelevereLang);
                System.out.println(">> Delivery lat/long after getting del lat lang: "+deliveryLat+"--"+deliveryLang);
            }else{
                deliveryLat = latitude;
                deliveryLang = longitude;
                System.out.println(">> Delivery lat/long as a current latlang: "+deliveryLat+"--"+deliveryLang);

            }
            double userLat = Double.parseDouble(sharedPreferences.getString("userLat",""));
            double userLang = Double.parseDouble(sharedPreferences.getString("userLang",""));

            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(deliveryLat, deliveryLang))
                    .title("Deliverer"));
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLat, userLang))
                    .title("User"));

            LatLng source = new LatLng(deliveryLat,deliveryLang);
            LatLng destination = new LatLng(userLat,userLang);
            ArrayList<LatLng> mapList = new ArrayList<>();
            mapList.add(source);
            mapList.add(destination);
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for (int z = 0; z < mapList.size(); z++) {
                LatLng point = mapList.get(z);
                options.add(point);
            }
            googleMap.addPolyline(options);

        } else {
            Toast.makeText(ActivityOrderStatus.this, "unable to get location", Toast.LENGTH_LONG).show();
        }
    }


    //get current lattitude and longitude
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
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

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
                        if (ActivityCompat.checkSelfPermission(ActivityOrderStatus.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityOrderStatus.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        /**
         * Stop using GPS listener Calling this function will stop using GPS in your
         * app
         * */
        public void stopUsingGPS() {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         * */
        public double getLatitude() {
            if (location != null) {

                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         * */
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
         * */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("lat>>>>>++++++"+ latitude);
                System.out.println("lang>>>>>>+++++++"+ longitude);

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
