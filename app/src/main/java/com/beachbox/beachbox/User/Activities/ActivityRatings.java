package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

/**
 *
 * This class for rating delivery boy.
 * Created by bitwarepc on 17-Jul-17.
 */

public class ActivityRatings extends Activity {
    TextView tvBackCards,tvDateDetails,tvPrice,tv_restaurantName, tvCustomerName,tvPriceSmall,tv_SubmitRatings;;
    RatingBar ratingBar;
    String strRating = "";
    ImageView iv_restaurantImage;
    ConnectionDetector cd;
    boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AQuery aQuery;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(ActivityRatings.this);
        isInternetPresent = cd.isConnectingToInternet();

        aQuery = new AQuery(ActivityRatings.this);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        tvBackCards = (TextView) findViewById(R.id.tvBackCards);
        iv_restaurantImage = (ImageView) findViewById(R.id.iv_restaurantImage);
        tvDateDetails = (TextView) findViewById(R.id.tvDateDetails);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tv_restaurantName = (TextView) findViewById(R.id.tv_restaurantName);
        tvCustomerName = (TextView) findViewById(R.id.tvCustomerName);
        tvPriceSmall = (TextView) findViewById(R.id.tvPriceSmall);
        tv_SubmitRatings = (TextView) findViewById(R.id.tv_SubmitRatings);


        String resImage = sharedPreferences.getString("resImage","");
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            Glide.with(ActivityRatings.this).load(R.drawable.blank_resturant).into(iv_restaurantImage);
        }else {
            Glide.with(ActivityRatings.this).load(resImage).into(iv_restaurantImage);
        }

        tvCustomerName.setText(sharedPreferences.getString("resCustName",""));
        tvDateDetails.setText(sharedPreferences.getString("resDateTime",""));
        tvPrice.setText("$ "+sharedPreferences.getString("resPrice",""));
        tvPriceSmall.setText("$ "+sharedPreferences.getString("resPrice",""));
        tv_restaurantName.setText(sharedPreferences.getString("resName",""));

        tvBackCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityRatings.super.onBackPressed();
            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                strRating = String.valueOf(rating);
            }
        });

        tv_SubmitRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String orderId = sharedPreferences.getString("order_id","");
                String restaurant_id = sharedPreferences.getString("restoId","");
                String rating = strRating;
                String review_desc = "Good";
                String session_user_token = sharedPreferences.getString("userSessionTokan","");
                if(isInternetPresent){
                    new updateReview().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + session_user_token + "\",\"restaurant_id\":\"" + restaurant_id + "\",\"rating\":\"" + rating + "\",\"review_desc\":\"" + review_desc + "\",\"order_id\":\"" + orderId + "\"}");
                }else{
                    Toast.makeText(ActivityRatings.this, R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class updateReview extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityRatings.this);
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Signup params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"user/updateOrderRating")
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
            System.out.println(">>> Update rating :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    String is_success = jsonObject.getString("is_success");
                    if(is_success.equalsIgnoreCase("true")){
                        Toast.makeText(ActivityRatings.this,jsonObject.getString("err_msg"),Toast.LENGTH_SHORT).show();
                                 editor.putString("tabPosition","3");
                                 editor.commit();
                                startActivity(new Intent(ActivityRatings.this,UDashboardActivityNew.class));
                    }else{
                        Toast.makeText(ActivityRatings.this,jsonObject.getString("err_msg"),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ActivityRatings.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }


}
