package com.beachbox.beachbox.User.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.GPSTracker;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.ActivityFilterRestaurant;
import com.beachbox.beachbox.User.Activities.ActivityMap;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurant;
import com.beachbox.beachbox.User.Model.restaurantlist.ResponseRestaurantList;
import com.beachbox.beachbox.User.Model.restaurantlist.Restaurantslist;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class used for displaying restaurents list.
 * Created by bitware on 22/3/17.
 */

public class FragmentRestaurants extends BaseFragment implements APIRequest.ResponseHandler{
    View view;
    RecyclerView recyclerView;
    TextView tv_home;
    GPSTracker gpsTracker;
    LinearLayout ll_map;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    ArrayList<com.beachbox.beachbox.User.Model.ModelRestList> arrRestList;
    AdapterRestaurant adapterRestaurant;
    ImageView ivfavouriteicon;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LinearLayoutManager mLayoutManager;
    String currentTime;
    BaseResponse baseResponse;
    Context mContext;
    boolean isGPSEnabled = false;
    boolean isResumeCall = false;
    LinearLayout llFilterResto;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String prefVal = "";
    TextView tvFilterLabel;
    AdapterRestaurant adapterRecyclerView;
    int pageCount = 1;
    ArrayList<Restaurantslist> arrGlobal = new ArrayList<>();
    public static  ArrayList<Restaurantslist> arrNewMap = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_one, container, false);
        System.out.println("************fragment*********");
        init();

        isGPSEnabled = checkGPSEnabled();
        if (isGPSEnabled) {
            callAPI(pageCount);
        } else {
            showSettingsAlert();
        }
        tvFilterLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filterText = tvFilterLabel.getText().toString().trim();
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){
                        if (filterText.equalsIgnoreCase("Clear Filter")) {
                            editor.remove("isFilter");
                            editor.apply();

                            ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentRestaurants());

                        } else {

                            ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentFilter());
                        }

                    }else{
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }


            }
        });
        return view;
    }
    public void callAPI(int pageCount) {
        gpsTracker = new GPSTracker(getActivity());
        gpsTracker.getLocation();
        double  currentLat = gpsTracker.getLatitude();
         double  currentLang = gpsTracker.getLongitude();

        String  strLat = "",strLang = "";

        if(!String.valueOf(currentLat).isEmpty() &&  String.valueOf(currentLat).length() > 4 ){
              strLat = String.valueOf(currentLat);
              strLang = String.valueOf(currentLang);

              /* strLat = getFormatedLatLong(currentLat);
              strLang = getFormatedLatLong(currentLang);*/
        }
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            jsonRequest(String.valueOf(pageCount),strLat, strLang);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormatedLatLong(double currentLat) {
        String strLat = "";
        String[] arr=String.valueOf(currentLat).split("\\.");
        long[] intArr=new long[2];
        intArr[0]=Long.parseLong(arr[0]); // 1
        intArr[1]=Long.parseLong(arr[1]); //
        String mainDigits = String.valueOf(intArr[0]);
        String strLenght = String.valueOf(intArr[1]);
        String first4char = strLenght.substring(0,4);
        String strFinal = mainDigits+"."+first4char;
        System.out.println(">>>> return val --- :"+strFinal);
        return strFinal;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isResumeCall) {
            pageCount = 1;
            callAPI(pageCount);
        }
    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
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

    @Override
    public void onSuccess(BaseResponse response) {
        ResponseRestaurantList restaurantList = (ResponseRestaurantList) response;
        editor.remove("isFilter");
        editor.apply();

        if (restaurantList.getIsSuccess()) {
            System.out.println("************"+restaurantList.getRestaurantslist().toString());
            ArrayList<Restaurantslist> arrRestaurantList = (ArrayList<Restaurantslist>) restaurantList.getRestaurantslist();
            /*adapterRestaurant = new AdapterRestaurant(((UDashboardActivityNew)getActivity()), arrRestaurantList);

            mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(adapterRestaurant);*/

            if (arrRestaurantList.size() > 0) {
                arrGlobal.addAll(arrRestaurantList);
                if(getActivity() != null){
                    adapterRestaurant = new AdapterRestaurant(((UDashboardActivityNew)getActivity()), arrGlobal);
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setAdapter(adapterRestaurant);
                }
                arrNewMap = arrGlobal;
            } else {
                Toast.makeText(getActivity(), "No more restaurant found", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Something went wrong,please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {
    }

    private void jsonRequest(String pageCount, String mLat, String mLang) {
        JSONObject jsonObject = new JSONObject();
        try {
            String filterVal= "{}";

            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sharedPreferences.getString("userSessionTokan",""));
            jsonObject.put("latitude", mLat);
            jsonObject.put("longitude", mLang);
            jsonObject.put("page", pageCount);
            jsonObject.put("device_time", currentTime);
            if(!prefVal.isEmpty()){
                jsonObject.put("filter", FragmentFilter.jObj);
            }else{
                jsonObject.put("filter", filterVal);
            }
            if (ActivityHome.strFlowType.equalsIgnoreCase("pickup")) {
                jsonObject.put("type", Config.PICKUP);
            }else {
                jsonObject.put("type", Config.DELIVERY);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String restaurantAPI = Config.BASE_URL + "user/restaurants";
        System.out.println("## Restaurants API :" + restaurantAPI);
        System.out.println("## Restaurants Params :" + jsonObject);


        if(sharedPreferences.getString("isUserLoggedIn","").equalsIgnoreCase("Yes")){
            //If user logged in then check his current location with previous updated locations if its same then call Restaurant service with current latlong
            // if not then update his latlong on server & call the API with this updated latlong

            System.out.println(">>>> :firstTimeLat conditions : "+sharedPreferences.getString("firstTimeLat",""));
            System.out.println(">>>> :mLat conditions : "+String.valueOf(mLat));
            cd = new ConnectionDetector(getActivity());
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {

                isGPSEnabled = checkGPSEnabled();
                if(isGPSEnabled){
                    if(sharedPreferences.getString("firstTimeLat","").equalsIgnoreCase(String.valueOf(mLat))){
                        new APIRequest(getActivity(), jsonObject, restaurantAPI, this, Config.API_RESTAURANT_LIST, Config.POST);
                    }else{
                        inserUserLocationAPI();
                    }
                }else{
                    showSettingsAlert();
                }
            }else{
                Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

            }


        }else{
            new APIRequest(getActivity(), jsonObject, restaurantAPI, this, Config.API_RESTAURANT_LIST, Config.POST);
        }

    }

    private void init() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mContext = getActivity();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        currentTime = sdf.format(new Date());

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        tv_home = (TextView) view.findViewById(R.id.tv_home);
        ll_map = (LinearLayout) view.findViewById(R.id.ll_map);
        llFilterResto = (LinearLayout) view.findViewById(R.id.llFilterResto);
        tvFilterLabel = (TextView) view.findViewById(R.id.tvFilterLabel);

        prefVal = sharedPreferences.getString("isFilter", "");
        if (!prefVal.isEmpty()) {
            tvFilterLabel.setText("Clear Filter");
        } else {
            tvFilterLabel.setText("Filter");
        }
        tv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled ){
                        Intent i = new Intent(getActivity(), ActivityHome.class);
                        startActivity(i);
                    }else{
                        showSettingsAlert();
                    }

                }else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }

                //getActivity().finish();
            }
        });

        ll_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled ){
                        if(FragmentRestaurants.arrNewMap.size() > 0){
                            ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentMap());
                        }else{
                            Toast.makeText(getActivity(), "Please check restaurants list before going to map", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        showSettingsAlert();
                    }

                }else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }



            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (pageCount < 4) {
                                pageCount++;
                                callAPI(pageCount);
                            } else {
                                Toast.makeText(getActivity(), "No more restaurants found", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            }
        });
    }


    private void inserUserLocationAPI() {
        System.out.println("************************first************");
        GPSTracker gpsTracker = new GPSTracker(getActivity());

        double  currentLat = gpsTracker.getLatitude();
        double  currentLang = gpsTracker.getLongitude();

        if(!String.valueOf(currentLat).isEmpty() &&  String.valueOf(currentLat).length() > 4 ) {
          /*  String  strLat = getFormatedLatLong(currentLat);
            String  strLang = getFormatedLatLong(currentLang);
*/
            double firstTimeLat= currentLat;
            double firstTimeLang= currentLang;

            JSONObject loationObj = new JSONObject();
            try {
                editor.putString("firstTimeLat",String.valueOf(firstTimeLat));
                editor.putString("firstTimeLang",String.valueOf(firstTimeLang));
                editor.commit();

                loationObj.put("latitude",firstTimeLat);
                loationObj.put("longitude",firstTimeLang);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray locationArray = new JSONArray();
            locationArray.put(loationObj);
            cd = new ConnectionDetector(getActivity());
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent){
                new updateLocationAPI().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + sharedPreferences.getString("userSessionTokan","") + "\",\"location_details\":" + locationArray + " }");
            }else {
                Toast.makeText(getActivity(),R.string.noNetworkMsg,Toast.LENGTH_SHORT).show();
            }
        }
    }

    class updateLocationAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setMessage("Loading..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Location Update params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"updateuserlocation")
                    .post(body)
                    .build();
            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>> LocationUpdate Result:" + s);
            p.dismiss();

            JSONObject jsonObject = new JSONObject();
            String filterVal = "{}";
            try {
                jsonObject.put("accesstoken", Config.accessToken);
                jsonObject.put("session_user_token", sharedPreferences.getString("userSessionTokan",""));
                jsonObject.put("latitude", sharedPreferences.getString("firstTimeLat",""));
                jsonObject.put("longitude", sharedPreferences.getString("firstTimeLang",""));
                jsonObject.put("page", pageCount);
                jsonObject.put("device_time", currentTime);

                if(!prefVal.isEmpty()){
                    jsonObject.put("filter", FragmentFilter.jObj);
                }else{
                    jsonObject.put("filter", filterVal);
                }
                if (ActivityHome.strFlowType.equalsIgnoreCase("pickup")) {
                    jsonObject.put("type", Config.PICKUP);
                }else {
                    jsonObject.put("type", Config.DELIVERY);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String restaurantAPI = Config.BASE_URL + "user/restaurants";
            System.out.println("## Restaurants Params :" + jsonObject);

            callLocationUpdatedRestaurantListAPI(jsonObject, restaurantAPI);
        }


    }
    private void callLocationUpdatedRestaurantListAPI(JSONObject jsonObject, String restaurantAPI) {
        new APIRequest(getActivity(), jsonObject, restaurantAPI, this, Config.API_RESTAURANT_LIST, Config.POST);
    }
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isResumeCall = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
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

}
