package com.beachbox.beachbox.User.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityRatings;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitwarepc on 01-Aug-17.
 */

public class FragmentRating extends Fragment {
    View view;
    TextView tvBackCards,tvDateDetails,tvPrice,tv_restaurantName, tvCustomerName,tvPriceSmall,tv_SubmitRatings;;
    RatingBar ratingBar;
    String strRating = "";
    ImageView iv_restaurantImage;
    ConnectionDetector cd;
    boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String orderId;
    AQuery aQuery;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_ratings, container, false);
            init();

            callOrderDetailsAPI();  //Call Order Details API any how even  update location API response is ok or not


        }
        return  view;
    }

    private void init() {
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();

        aQuery = new AQuery(getActivity());
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        tvBackCards = (TextView)view.findViewById(R.id.tvBackCards);
        iv_restaurantImage = (ImageView)view.findViewById(R.id.iv_restaurantImage);
        tvDateDetails = (TextView)view.findViewById(R.id.tvDateDetails);
        tvPrice = (TextView)view.findViewById(R.id.tvPrice);
        tv_restaurantName = (TextView)view.findViewById(R.id.tv_restaurantName);
        tvCustomerName = (TextView)view.findViewById(R.id.tvCustomerName);
        tvPriceSmall = (TextView)view.findViewById(R.id.tvPriceSmall);
        tv_SubmitRatings = (TextView)view.findViewById(R.id.tv_SubmitRatings);
        orderId = sharedPreferences.getString("order_id", "");


        String resImage = sharedPreferences.getString("resImage","");
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            Glide.with(getActivity()).load(R.drawable.blank_resturant).into(iv_restaurantImage);
        }else {
            Glide.with(getActivity()).load(resImage).into(iv_restaurantImage);
        }


        tvBackCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","3");
                editor.commit();
                startActivity(new Intent(getActivity(),UDashboardActivityNew.class));
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
                    Toast.makeText(getActivity(), R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            System.out.println(">>>Orders details result in Ratings screen :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");
                    if (is_success == true) {
                        JSONArray jsonArray = jsonObject.getJSONArray("menudetails");

                        System.out.println(">>> JSON ARRY : "+jsonArray.length());
                        String pName = jsonArray.getJSONObject(0).getString("menu_description");
                        System.out.println("### :"+pName);
                        tvCustomerName.setText(pName);
                        System.out.println("****date******"+sharedPreferences.getString("resDateTime",""));
                       // tvDateDetails.setText(sharedPreferences.getString("resDateTime",""));
                        tvDateDetails.setText(getFormatedDateTime(sharedPreferences.getString("resDateTime","")));
                        tvPrice.setText("$ "+sharedPreferences.getString("resPrice",""));
                        tvPriceSmall.setText("$ "+sharedPreferences.getString("resPrice",""));
                        tv_restaurantName.setText(sharedPreferences.getString("resName",""));
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
    class updateReview extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Signup params :" + params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "user/updateOrderRating")
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
            System.out.println(">>> Update rating :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    String is_success = jsonObject.getString("is_success");
                    if (is_success.equalsIgnoreCase("true")) {
                        Toast.makeText(getActivity(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                        editor.putString("tabPosition", "3");
                        editor.commit();
                        startActivity(new Intent(getActivity(), UDashboardActivityNew.class));
                    } else {
                        Toast.makeText(getActivity(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.noResponseMsg), Toast.LENGTH_SHORT).show();
            }


        }
    }

    public String getFormatedDateTime(String orderedDate) {
        String myMonth = "";
        String[] arrTest = orderedDate.split("\\s+");
        String mDate = arrTest[0];
        String mTime = arrTest[1];

        String[] actDate = mDate.split("-");
        String dateYear = actDate[0];
        String datemonth = actDate[1];
        String datday = actDate[2];


        switch (datemonth){
            case "01":
                myMonth = "Jan";
                break;
            case "02":
                myMonth = "Feb";
                break;
            case "03":
                myMonth = "Mar";
                break;
            case "04":
                myMonth = "Apr";
                break;
            case "05":
                myMonth = "May";
                break;
            case "06":
                myMonth = "Jun";
                break;
            case "07":
                myMonth = "Jul";
                break;
            case "08":
                myMonth = "Aug";
                break;
            case "09":
                myMonth = "Sept";
                break;
            case "10":
                myMonth = "Oct";
                break;
            case "11":
                myMonth = "Nov";
                break;

            case "12":
                myMonth = "Dec";
                break;
        }
        String newVal = myMonth+" "+datday;

        String[] actTime = mTime.split(":");
        String time1 = actTime[0];
        String time2 = actTime[1];
        String time3 = actTime[2];
        String strAMPMVal = "";
        if(Integer.parseInt(time1) < 12){
            strAMPMVal = "AM";
        }else{
            strAMPMVal = "PM";
        }
        String newtime = time1+":"+time2;//+" "+strAMPMVal;


        String start_dt = newtime;
        String formatedTime = null;
        DateFormat parser = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = (Date) parser.parse(start_dt);
            DateFormat formatter = new SimpleDateFormat("hh:mm");
            System.out.println("******dateeeeeeeeeee******"+formatter.format(date));
            formatedTime = formatter.format(date)+" "+strAMPMVal;
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return newVal+" "+formatedTime;
    }

}
