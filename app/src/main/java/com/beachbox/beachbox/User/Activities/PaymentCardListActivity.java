package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Adapter.AdapterPaymentCardsList;
import com.beachbox.beachbox.User.Model.ModelPaymentsCardList;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 * This class for showing list of added cards.
 * Created by bitwarepc on 11-Jul-17.
 */

public class PaymentCardListActivity extends Activity {
    ListView lvCardListDetails;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    View footerView;
    String userSessionTokan;
    TextView tvAddPaymentCard,tvBackPaymentCards;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_card_list);
        footerView = ((LayoutInflater)PaymentCardListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_payment_card_footer, null, false);
        inIt();
        if(isInternetPresent){
            new getPaymentCardList().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + userSessionTokan + "\"}");
        }else{
            Toast.makeText(PaymentCardListActivity.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }
    }

    //initialization
    private void inIt() {
        cd = new ConnectionDetector(this);
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();

         tvAddPaymentCard = (TextView)footerView.findViewById(R.id.tvAddPaymentCard);
         lvCardListDetails = (ListView)findViewById(R.id.lvCardListDetails);
         tvBackPaymentCards = (TextView)findViewById(R.id.tvBackPaymentCards);
         userSessionTokan = sharedPreferences.getString("userSessionTokan","");

        tvAddPaymentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentCardListActivity.this,AddPaymentCardActivity.class);
                startActivity(intent);
                finish();

            }
        });

        tvBackPaymentCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","1");
                editor.commit();
                Intent intent = new Intent(PaymentCardListActivity.this,UDashboardActivityNew.class);
                startActivity(intent);
                finish();
            }
        });

    }

    //get added card list
    class getPaymentCardList extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(PaymentCardListActivity.this);
            p.setMessage("Please wait..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> PaymentCard params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"listCards")
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
            System.out.println(">>> PaymentCard  result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    ArrayList<ModelPaymentsCardList> arrCardList = new ArrayList<>();
                    boolean isSuccess = jsonObject.getBoolean("is_success");
                    if (isSuccess) {
                        JSONArray jsonArray = jsonObject.getJSONArray("creditcardslist");
                        for (int i = 0; i < jsonArray.length() ; i++) {
                            ModelPaymentsCardList modelCardList = new ModelPaymentsCardList();
                            JSONObject object = jsonArray.getJSONObject(i);

                            modelCardList.setCardholder_name(object.getString("cardholder_name"));
                            modelCardList.setCredit_card_mask(object.getString("credit_card_mask"));
                            modelCardList.setBilling_address_id(object.getString("billing_address_id"));
                            modelCardList.setPayment_profile_id(object.getString("payment_profile_id"));
                            modelCardList.setUser_profile_id(object.getString("user_profile_id"));
                            modelCardList.setId(object.getString("id"));
                            modelCardList.setCard_type(object.getString("card_type"));
                            modelCardList.setUser_id(object.getString("user_id"));
                            modelCardList.setCard_exp_month(object.getString("exp_month"));
                            modelCardList.setCard_exp_year(object.getString("exp_year"));
                            modelCardList.setCard_cvv(object.getString("cvv"));

                            arrCardList.add(modelCardList);
                        }

                        AdapterPaymentCardsList adapterPaymentCardsList = new AdapterPaymentCardsList(PaymentCardListActivity.this,R.layout.row_payment_list_card,arrCardList);
                        lvCardListDetails.setAdapter(adapterPaymentCardsList);
                        lvCardListDetails.addFooterView(footerView);

                    } else {
                        Toast.makeText(PaymentCardListActivity.this,jsonObject.getString("err_msg"),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(PaymentCardListActivity.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putString("tabPosition","1");
        editor.commit();
        Intent intent = new Intent(PaymentCardListActivity.this,UDashboardActivityNew.class);
        startActivity(intent);
        finish();
    }
}
