package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurantMenuList;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurants;
import com.beachbox.beachbox.User.Model.MOdelMenuLIst;
import com.beachbox.beachbox.User.Model.ModelRestList;
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

import io.realm.Realm;

/**
 * Created by bitware on 23/3/17.
 */

public class ActivityRestaurantMenuList extends Activity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);/*
        setContentView(R.layout.activity_restaurant_menu_list);
        init();

        /*tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","1");
                editor.commit();
                getCartCount();
            }
        });*/
    }


    private void init() {
        cd = new ConnectionDetector(this);
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        realm = RealmController.with(ActivityRestaurantMenuList.this).getRealm();

        tv_back= (TextView) findViewById(R.id.tv_back);
        lv_menuList= (ListView) findViewById(R.id.lv_menuList);

        tv_restaurantName = (TextView) findViewById(R.id.tv_restaurantName);
        tvActualRating = (TextView) findViewById(R.id.tvActualRating);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tv_restaurantDes = (TextView) findViewById(R.id.tv_restaurantDes);
        ivRestoFullImg = (ImageView) findViewById(R.id.ivRestoFullImg);

        String restoId =    sharedPreferences.getString("clickedRestoId","");
        String restoRatings =  sharedPreferences.getString("clickedRestoRating","");
        String restoName =  sharedPreferences.getString("clickedRestoName","");

        String imgAdvPath = sharedPreferences.getString("clickedRestoImg","");
        if(!imgAdvPath.isEmpty()){

            Glide.with(ActivityRestaurantMenuList.this).load(imgAdvPath)
                    .thumbnail(0.5f)
                    .into(ivRestoFullImg);
            // aQuery.id().image(restoList.get(position).getImage());
        }

        tvActualRating.setText(restoRatings);
        tv_restaurantName.setText(restoName);
        if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
             strType = "pickup";
        }else{
             strType = "delivery";
        }
        if (isInternetPresent) {
            new getRaustarantDetails().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"restaurant_id\":\"" + restoId +  "\",\"type\":\"" + strType + "\"}");
        } else {
            Toast.makeText(ActivityRestaurantMenuList.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }

    }


    class getRaustarantDetails extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityRestaurantMenuList.this);
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
                        String status = jsonObject.getString("status");
                        String rs_description = jsonObject.getString("rs_description");
                            if(rs_description != "null"){
                                 tv_restaurantDes.setText(rs_description);
                            }else{
                                 tv_restaurantDes.setText("Description not available");
                            }

                        tvStatus.setText(status);

                        JSONArray jsonArray = jsonObject.getJSONArray("menu_details");

                        for(int i = 0; i < jsonArray.length();i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            ModelRestaurantDetails modelDetails = new ModelRestaurantDetails();
                            modelDetails.setMenu_id(object.getString("menu_id"));
                            modelDetails.setMenu_name(object.getString("menu_name"));
                            modelDetails.setMenu_price(object.getString("menu_price"));
                            modelDetails.setMenu_description(object.getString("menu_description"));
                            modelDetails.setRestaurantId(sharedPreferences.getString("clickedRestoId",""));
                            modelDetails.setRestoName(sharedPreferences.getString("clickedRestoName",""));
                            arrModelDetails.add(modelDetails);
                        }

                        adapterRestaurantMenuList = new AdapterRestaurantMenuList(ActivityRestaurantMenuList.this,R.layout.row_restaurantmenu,arrModelDetails);
                        lv_menuList.setAdapter(adapterRestaurantMenuList);

                    }else{
                        Toast.makeText(ActivityRestaurantMenuList.this, "Something went wrong, please try again later", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ActivityRestaurantMenuList.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
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

        Intent intent = new Intent(ActivityRestaurantMenuList.this, UDashboardActivityNew.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putString("tabPosition","2");
        editor.commit();
      getCartCount();
    }
}
