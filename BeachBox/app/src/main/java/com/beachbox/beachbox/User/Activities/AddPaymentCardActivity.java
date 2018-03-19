package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitwarepc on 11-Jul-17.
 */

public class AddPaymentCardActivity extends Activity {
        EditText etCardNo,etZipCode,etmonth,etCvv;
        TextView tvSaveCard;
    String strCardNo,strZipCode,strMonth,strCvv;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent,isSuccess;
    public int pos=0;
    TextView tvBackCards;
    CheckBox chkRememberPass;
    String strCardSaveInDB = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrtivity_add_paymentcard);

        cd = new ConnectionDetector(this);
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();

        etCardNo = (EditText) findViewById(R.id.etCardNo);
        etZipCode = (EditText) findViewById(R.id.etZipCode);
        etmonth = (EditText) findViewById(R.id.etmonth);
        etCvv = (EditText) findViewById(R.id.etCvv);
        tvSaveCard = (TextView) findViewById(R.id.tvSaveCard);
        tvBackCards = (TextView) findViewById(R.id.tvBackCards);
        chkRememberPass = (CheckBox)findViewById(R.id.chkRememberPass);

        tvBackCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent i = new Intent(AddPaymentCardActivity.this,PaymentCardListActivity.class);
                startActivity(i);
                finish();
            }
        });

        etCardNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pos=etCardNo.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(etCardNo.getText().length()==4 && pos!=5)
                {   etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(5);
                }else if (etCardNo.getText().length()==9 && pos!=10){
                    etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(10);
                }
                else if (etCardNo.getText().length()==14 && pos!=15){
                    etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(15);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etmonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pos=etmonth.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(etmonth.getText().length()==2 && pos!=3) {
                    etmonth.setText(etmonth.getText().toString() + "/");
                    etmonth.setSelection(3);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        tvSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strCardNo = etCardNo.getText().toString().replace("-","").trim();
                strZipCode = etZipCode.getText().toString().trim();
                strMonth = etmonth.getText().toString().trim();
                strCvv = etCvv.getText().toString().trim();

                System.out.println("strCardNo >>> " +strCardNo);
                System.out.println("Expiry month >>> " +strMonth);
                System.out.println("CVV >>> " +strCvv);
                System.out.println("Zipcode >>> " +strZipCode);

                String  userSessionTokan = sharedPreferences.getString("userSessionTokan","");
                String  username = sharedPreferences.getString("username","");

                if (!strCardNo.isEmpty()) {
                    if (strCardNo.length() == 16) {
                    if (!strMonth.isEmpty()) {
                        if (strMonth.length() == 7) {
                        if (!strCvv.isEmpty()) {
                            if (strCvv.length() == 5) {
                                if (!strZipCode.isEmpty()) {
                                    if (strZipCode.length() == 4) {

                                        System.out.println(">>> strCardSaveInDB :"+strCardSaveInDB);
                                        if(strCardSaveInDB.equalsIgnoreCase("Yes")){

                                            new saveCardData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + userSessionTokan + "\",\"cardholder_name\":\"" + username + "\" ," +
                                                    "\"card_num\":\"" + strCardNo + "\",\"cc_num\":\"" + strCvv + "\",\"exp_month_year\":\"" + strMonth + "\",\"zip\":\"" + strZipCode + "\" }");
                                        }else{

                                            String arrStr[] = strMonth.split("/");
                                            String tempMonth = arrStr[0];
                                            String tempYear = arrStr[1];

                                            if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
                                                editor.putString("pickupCC","Yes");
                                            }
                                            editor.putString("tabPosition","2");
                                            editor.putString("tempCardNo",strCardNo);
                                            editor.putString("tempCvvNo",strCvv);
                                            editor.putString("tempCardMonth",tempMonth);
                                            editor.putString("tempCardYear",tempYear);
                                            editor.putString("tempZipCode",strZipCode);
                                            editor.commit();


                                            Intent intent = new Intent(AddPaymentCardActivity.this,UDashboardActivityNew.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                    } else {
                                        etZipCode.requestFocus();
                                        etZipCode.setError("Please enter valid 4 digits zipcode");
                                    }
                                } else {
                                    etZipCode.requestFocus();
                                    etZipCode.setError("Please enter zipcode");
                                }
                            } else {
                                etCvv.requestFocus();
                                etCvv.setError("Please enter valid 5 digits cvv no");

                            }
                        } else {
                            etCvv.requestFocus();
                            etCvv.setError("Please enter cvv no");
                        }
                        } else {
                            etmonth.requestFocus();
                            etmonth.setError("Please enter valid month & year");
                        }
                    } else {
                        etmonth.requestFocus();
                        etmonth.setError("Please enter  month & year");

                    }
                } else {
                    etCardNo.requestFocus();
                    etCardNo.setError("Please enter valid 16 digits card no");
                }
            } else {
                etCardNo.requestFocus();
                etCardNo.setError("Please enter card no");
            }
            }
        });


    }

    public void checkBoxClicked(View v) {
        //code to check if this checkbox is checked!
        CheckBox checkBox = (CheckBox)v;
        if(checkBox.isChecked()){
            strCardSaveInDB = "Yes";
        }else{
            strCardSaveInDB = "No";

        }
    }

        class saveCardData extends AsyncTask<String, Void, String> {
            ProgressDialog p;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                p = new ProgressDialog(AddPaymentCardActivity.this);
                p.setMessage("Please wait..");
                p.show();
            }
            @Override
            protected String doInBackground(String... params) {
                Response response = null;
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
                client.setReadTimeout(120, TimeUnit.SECONDS);
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                System.out.println(">>> Save card params :"+ params[0]);
                RequestBody body = RequestBody.create(JSON, params[0]);
                Request request = new Request.Builder()
                        .url(Config.BASE_URL+"saveCard")
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
                System.out.println(">>> Save card result :" + s);
                p.dismiss();
                if (s != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        String isSuccess = jsonObject.getString("is_success");

                        if (isSuccess.equalsIgnoreCase("true")) {
                            Toast.makeText(AddPaymentCardActivity.this, jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddPaymentCardActivity.this,PaymentCardListActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            JSONObject object = jsonObject.getJSONObject("err_msg");
                            Toast.makeText(AddPaymentCardActivity.this,object.getString("0"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AddPaymentCardActivity.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
                }
            }
    }
}
