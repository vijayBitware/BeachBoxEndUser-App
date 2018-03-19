package com.beachbox.beachbox.User.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Model.ModelRestList;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 13/7/17.
 */

public class ActivityForgotPassword extends AppCompatActivity {

    EditText edtEmail;
    TextView tv_send ,tv_back_photo;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    String userEmail="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        init();
        tv_back_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityForgotPassword.this,ActivitySignIn.class));
                finish();
            }
        });
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = edtEmail.getText().toString();

                if (!userEmail.isEmpty()) {
                    if (userEmail.matches(Config.EMAIL_REGEX)) {
                        if (isInternetPresent) {
                            new callFoegotPassAPI().execute("{\"email\":\"" + userEmail + "\",\"accesstoken\":\"" + Config.accessToken + "\"}");
                        } else {
                            Toast.makeText(ActivityForgotPassword.this, R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivityForgotPassword.this, getResources().getString(R.string.pleaseEnterTheValidEmailid), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ActivityForgotPassword.this,"Please Enter Email Id", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        cd = new ConnectionDetector(this);
        isInternetPresent=cd.isConnectingToInternet();
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        tv_send = (TextView) findViewById(R.id.tv_send);
        tv_back_photo = (TextView) findViewById(R.id.tv_back_photo);
    }

    class callFoegotPassAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityForgotPassword.this);
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            //  client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            //client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Restaurant params :" + params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "user/forgotpassword")
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
            System.out.println(">>> Forgot password result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    String is_success = jsonObject.getString("is_success");
                    String err_msg = jsonObject.getString("err_msg");

                    if (is_success.equalsIgnoreCase("true")) {
                        Toast.makeText(ActivityForgotPassword.this, err_msg, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ActivityForgotPassword.this,ActivitySignIn.class));
                        finish();
                    } else {
                        String strMsg = jsonObject.getString("err_msg");
                        Toast.makeText(ActivityForgotPassword.this, strMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ActivityForgotPassword.this, getResources().getString(R.string.noResponseMsg), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
