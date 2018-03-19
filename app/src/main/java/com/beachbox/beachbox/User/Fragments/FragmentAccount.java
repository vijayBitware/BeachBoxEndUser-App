package com.beachbox.beachbox.User.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.ActivityOrderStatus;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Activities.ActivityUpdateProfile;
import com.beachbox.beachbox.User.Activities.HelpActivity;
import com.beachbox.beachbox.User.Activities.PaymentCardListActivity;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurant;
import com.beachbox.beachbox.User.Model.loginResponse.LogOut;
import com.beachbox.beachbox.User.Model.responseplaceorder.ResponsePlaceOrder;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class used for updating user profile.
 * Created by bitware on 22/3/17.
 */

public class FragmentAccount extends Fragment implements APIRequest.ResponseHandler {

    View view;
    Boolean isInternetPresent, isSuccess;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView tvLogout, tv_fullName, tv_email, tvEditProfile, tvLoginAccount;
    int customer_id;
    String userSessionTokan = null;
    ImageView image_cover;
    AQuery aQuery;
    LinearLayout llLoginSuccess, llLogin;
    TextView tvPayment, tvHelp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_profile, container, false);
            aQuery = new AQuery(getActivity());
            init();
           /*if(sharedPreferences.getString("isUserLoggedIn","").equalsIgnoreCase("Yes")){
               llLoginSuccess.setVisibility(View.VISIBLE);
               if (isInternetPresent){
                   new getUserProfileData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_usertoken\":\"" + userSessionTokan + "\"}");
               }else {
                   Toast.makeText(getContext(),getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
               }
           }else{
               llLogin.setVisibility(View.VISIBLE);
           }*/
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
            llLoginSuccess.setVisibility(View.VISIBLE);
            cd = new ConnectionDetector(getContext());
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {
                new getUserProfileData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_usertoken\":\"" + userSessionTokan + "\"}");
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
            }
        } else {
            llLogin.setVisibility(View.VISIBLE);
        }
    }

    //initialization
    private void init() {
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tvLogout = (TextView) view.findViewById(R.id.tvLogout);
        tv_fullName = (TextView) view.findViewById(R.id.tv_fullName);
        tv_email = (TextView) view.findViewById(R.id.tv_email);
        image_cover = (ImageView) view.findViewById(R.id.image_cover);
        tvEditProfile = (TextView) view.findViewById(R.id.tvEditProfile);
        llLoginSuccess = (LinearLayout) view.findViewById(R.id.llLoginSuccess);
        llLogin = (LinearLayout) view.findViewById(R.id.llLogin);
        tvLoginAccount = (TextView) view.findViewById(R.id.tvLoginAccount);
        userSessionTokan = sharedPreferences.getString("userSessionTokan", "");
        tvPayment = (TextView) view.findViewById(R.id.tvPayment);
        tvHelp = (TextView) view.findViewById(R.id.tvHelp);

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((UDashboardActivityNew) getActivity()).replaceFragment(new FragmentUpdateProfile());
            }
        });

        tvLoginAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition", "4");
                editor.commit();
                Intent intent = new Intent(getActivity(), ActivitySignIn.class);
                startActivity(intent);
            }
        });

        tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdapterRestaurant.strAdvFlag = "No";
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
            }
        });

        tvPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("fromAccount", "Yes");
                editor.commit();

                ((UDashboardActivityNew) getActivity()).replaceFragment(new FragmentPaymentCardList());

            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

    }

    //logout response
    @Override
    public void onSuccess(BaseResponse response) {
        System.out.println("###>>> : " + response);

        LogOut logoutResponse = (LogOut) response;
        if (logoutResponse.getIsSuccess()) {
            Toast.makeText(getActivity(), logoutResponse.getErrMsg().toString(), Toast.LENGTH_SHORT).show();

            editor.putString("isUserLoggedIn", "No");
            editor.commit();

            editor.remove("card_cc_transaction_id");
            //editor.remove("card_cvv_num");
            editor.remove("card_user_id");
            // editor.remove("card_exp_month");
            //editor.remove("card_exp_year");
            editor.remove("card_card_id");
            editor.remove("getSelectedCard");
            editor.apply();

            ((UDashboardActivityNew) getActivity()).replaceFragment(new FragmentAccount());

        } else {
            Toast.makeText(getActivity(), logoutResponse.getErrMsg().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {
        Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
    }

    //get user profile data from server
    class getUserProfileData extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getContext());
            p.setMessage("Please wait..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
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
                    .url(Config.BASE_URL + "user/profiledetails")
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
            System.out.println(">>> Profile Detail result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    isSuccess = jsonObject.getBoolean("is_success");

                    if (isSuccess == true) {
                        tv_fullName.setText(jsonObject.getString("customer_name"));
                        String strProfile = jsonObject.getString("email") + " . " + jsonObject.getString("customer_phone");
                        tv_email.setText(strProfile);
                        String userImg = jsonObject.getString("customer_pic");
                        editor.putString("CustomerPic", userImg);
                        editor.commit();

                        if (userImg.isEmpty() || userImg.equals("")) {
                            image_cover.setImageResource(R.drawable.profile);

                        } else {
                            Picasso.with(getActivity())
                                    .load(userImg)
                                    .into(image_cover);
                          /*  Glide.with(getActivity())
                                    .load(userImg)
                                    .into(image_cover);
*/
                        }
                        editor.putString("first_name", jsonObject.getString("first_name"));
                        editor.putString("last_name", jsonObject.getString("last_name"));
                        editor.putString("email", jsonObject.getString("email"));
                        editor.putString("customer_phone", jsonObject.getString("customer_phone"));
                        editor.putString("customer_pic", jsonObject.getString("customer_pic"));
                        editor.putString("helpenduser_url", jsonObject.getString("help_end_user"));
                        // help_end_user
                        editor.commit();
                        //aQuery.id(R.id.image_cover).image(jsonObject.getString("customer_pic"));

                    } else {
                        p.dismiss();
                        Toast.makeText(getContext(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(getContext(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Logout Application");
        alertDialogBuilder
                .setMessage("Are you sure want to logout ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                logout();

                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //logout API call
    private void logout() {
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            JSONObject jsonObject = new JSONObject();
            String sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
            try {
                jsonObject.put("accesstoken", Config.accessToken);
                jsonObject.put("session_user_token", sessionUserTokan);
                String logOutURL = Config.BASE_URL + "getLogout";
                new APIRequest(getActivity(), jsonObject, logOutURL, this, Config.API_LOG_OUT, Config.POST);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
        }
    }

}
