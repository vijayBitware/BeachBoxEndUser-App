package com.beachbox.beachbox.User.Fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.ActivityRestaurantMenuList;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurantMenuList;
import com.beachbox.beachbox.User.Model.MOdelMenuLIst;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by bitwarepc on 21-Jul-17.
 */

public class FragmentRestaurantMenuDetails extends android.support.v4.app.Fragment {

    View view;
    ListView lv_menuList;
    AdapterRestaurantMenuList adapterRestaurantMenuList;
    ArrayList<MOdelMenuLIst> arrMenuList;
    TextView tv_back;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    TextView tv_restaurantName,tvActualRating,tvStatus,tv_restaurantDes;
    Realm realm;
    ImageView ivRestoFullImg;
    String strType = "";
    String strAddress = "";
    String address = null,city = null,state = null,country = null,postalCode,knownName;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      if(view == null){
          view = inflater.inflate(R.layout.activity_restaurant_menu_list,container,false);
          init();

      }
        return view;
    }

    private void init() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        realm = RealmController.with(getActivity()).getRealm();

        tv_back= (TextView)view. findViewById(R.id.tv_back);
        lv_menuList= (ListView)view. findViewById(R.id.lv_menuList);

        tv_restaurantName = (TextView)view. findViewById(R.id.tv_restaurantName);
        tvActualRating = (TextView)view. findViewById(R.id.tvActualRating);
        tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        tv_restaurantDes = (TextView)view. findViewById(R.id.tv_restaurantDes);
        ivRestoFullImg = (ImageView) view.findViewById(R.id.ivRestoFullImg);

        String restoId =    sharedPreferences.getString("clickedRestoId","");
        String restoRatings =  sharedPreferences.getString("clickedRestoRating","");
        String restoName =  sharedPreferences.getString("clickedRestoName","");

        String imgAdvPath = sharedPreferences.getString("clickedRestoImg","");
        if(!imgAdvPath.isEmpty()){

            Glide.with(getActivity()).load(imgAdvPath)
                    .thumbnail(0.5f)
                    .into(ivRestoFullImg);
            // aQuery.id().image(restoList.get(position).getImage());
        }
                if(restoRatings != null && !restoRatings.equalsIgnoreCase("null") ){
                    tvActualRating.setText(restoRatings);
                }else{
                    tvActualRating.setText("0");

                }
        tv_restaurantName.setText(restoName);
        if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
            strType = "pickup";
        }else{
            strType = "delivery";
        }

        if (isInternetPresent) {
            new getRaustarantDetails().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"restaurant_id\":\"" + restoId +  "\",\"type\":\"" + strType + "\"}");
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }

          tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","1");
                editor.commit();
                Intent i = new Intent(getActivity(),UDashboardActivityNew.class);
                startActivity(i);
                //getFragmentManager().popBackStackImmediate();
               // getCartCount();
            }
        });

    }

    class getRaustarantDetails extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            //  client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            //client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> RestaurantDetails  params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"restaurantdetails")
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
            System.out.println(">>> Restaurant Details result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    String is_success = jsonObject.getString("is_success");

                    if(is_success.equalsIgnoreCase("true")){
                        ArrayList<ModelRestaurantDetails> arrModelDetails = new ArrayList<>();
                        String restaurant_name = jsonObject.getString("restaurant_name");
                        String rs_description = jsonObject.getString("rs_description");
                        tvStatus.setText(jsonObject.getString("status"));
                        String restoTax = jsonObject.getString("rs_sales_tax");
                        String restoTiming = jsonObject.getString("restaurant_timing");

                        JSONObject locationObject = jsonObject.getJSONObject("location_details");
                        String lat = locationObject.getString("latitude");
                        String lang = locationObject.getString("longitude");

                        JSONArray jsonArray = jsonObject.getJSONArray("menu_details");
                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            ModelRestaurantDetails modelDetails = new ModelRestaurantDetails();
                            modelDetails.setMenu_id(object.getString("menu_id"));
                            modelDetails.setMenu_name(object.getString("menu_name"));
                            modelDetails.setMenu_price(object.getString("menu_price"));
                            modelDetails.setQty("0");
                            modelDetails.setMenu_description(object.getString("menu_description"));
                            modelDetails.setRestaurantId(sharedPreferences.getString("clickedRestoId",""));
                            modelDetails.setRestoName(sharedPreferences.getString("clickedRestoName",""));
                            modelDetails.setResraurantTax(restoTax);
                            modelDetails.setRestoTiming(restoTiming);
                            if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
                                modelDetails.setMainCategory(Config.PICKUP);
                            }else{
                                modelDetails.setMainCategory(Config.DELIVERY);
                            }
                            arrModelDetails.add(modelDetails);
                        }

                        if(Config.checkHasItemInCart()){
                            String storedRestoId = null;
                            List<ModelRestaurantDetails> existingData = RealmController.getInstance().getRestaurantList();
                            for (int i = 0; i < existingData.size() ; i++) {
                                 storedRestoId = existingData.get(0).getRestaurantId();
                            }
                            System.out.println(">>>## Stored restoId" +storedRestoId);
                            String currentRestoId = sharedPreferences.getString("clickedRestoId","");
                            System.out.println(">>>## Selected restoId:"+currentRestoId);
                            if(storedRestoId.equalsIgnoreCase(currentRestoId)){
                                System.out.println(">>>## YESSSSSS MATTCCCHHHH");
                                for (int i = 0; i < existingData.size() ; i++) {
                                    System.out.println(">>>Data :"+existingData.get(i).getMenu_id()+"--"+existingData.get(i).getQty()+"--"+existingData.get(i).getMenu_name());

                                }
                                for (int i = 0; i < existingData.size() ; i++) {
                                    for (int j = 0; j < arrModelDetails.size(); j++) {
                                        if(existingData.get(i).getMenu_id().equalsIgnoreCase(arrModelDetails.get(j).getMenu_id())){
                                            arrModelDetails.get(j).setQty(existingData.get(i).getQty());
                                        }
                                    }
                                }
                            }
                        }
                         if(getActivity() != null){
                             adapterRestaurantMenuList = new AdapterRestaurantMenuList(getActivity(),R.layout.row_restaurantmenu,arrModelDetails);
                             lv_menuList.setAdapter(adapterRestaurantMenuList);
                         }
                        strAddress =  getAddressFromLatLong(lat,lang);
                        if(address != null){
                            tv_restaurantDes.setText(strAddress);
                        }else {
                            tv_restaurantDes.setText("Address not available");
                        }
                    }else{
                        Toast.makeText(getActivity(), "Something went wrong, please try again later", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(),getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getAddressFromLatLong(String lat, String lang) {
        String strAddress = "";
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lang), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
             address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
             city = addresses.get(0).getLocality();
             state = addresses.get(0).getAdminArea();
             country = addresses.get(0).getCountryName();
             postalCode = addresses.get(0).getPostalCode();
             knownName = addresses.get(0).getFeatureName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        strAddress = address+","+ city +","+state+","+country;
        return strAddress;
    }

    private void getCartCount() {
        boolean isItemInDB = RealmController.getInstance().hasItemInDB();
        if(isItemInDB){
            List<ModelRestaurantDetails> cartCount = new ArrayList<ModelRestaurantDetails>();
            List<ModelRestaurantDetails> cartList  =  RealmController.getInstance().getRestaurantList();
            for(int i = 0; i < cartList.size(); i++){
                String strQty = cartList.get(i).getQty();
                if(!TextUtils.isEmpty(strQty)){
                    int qty = Integer.parseInt(strQty);
                    if(qty > 0){
                        cartCount.add(cartList.get(i));
                    }
                }
            }
            editor.putString("cartCount",cartCount.size()+"");

        }

        Intent intent = new Intent(getActivity(), UDashboardActivityNew.class);
        startActivity(intent);
    }

}
