package com.beachbox.beachbox.User.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.AddPaymentCardActivity;
import com.beachbox.beachbox.User.Activities.PaymentCardListActivity;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
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

import static android.content.Context.MODE_PRIVATE;
import static com.beachbox.beachbox.User.Fragments.FragmentCart.isEditCard;


/**
 * Created by bitwarepc on 21-Jul-17.
 */

public class FragmentPaymentCardList extends Fragment {
    View view;
    ListView lvCardListDetails;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    View footerView;
    String userSessionTokan;
    TextView tvAddPaymentCard,tvBackPaymentCards;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_payment_card_list,container,false);
            footerView = ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_payment_card_footer, null, false);

            init();

            if(isInternetPresent){
                new getPaymentCardList().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + userSessionTokan + "\"}");
            }else{
                Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void init() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();

        tvAddPaymentCard = (TextView)footerView.findViewById(R.id.tvAddPaymentCard);
        lvCardListDetails = (ListView)view.findViewById(R.id.lvCardListDetails);
        tvBackPaymentCards = (TextView)view.findViewById(R.id.tvBackPaymentCards);
        userSessionTokan = sharedPreferences.getString("userSessionTokan","");

        tvAddPaymentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditCard = "No";
                ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentAddPaymentCard());

            }
        });

        tvBackPaymentCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","2");
                editor.commit();
                Intent i = new Intent(getActivity(),UDashboardActivityNew.class);
                startActivity(i);

            }
        });
    }



    class getPaymentCardList extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
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
                            modelCardList.setZipCode(object.getString("zip"));

                            arrCardList.add(modelCardList);
                        }
                        if(getActivity() !=null){
                            AdapterPaymentCardsList adapterPaymentCardsList = new AdapterPaymentCardsList(getActivity(),R.layout.row_payment_list_card,arrCardList);
                            lvCardListDetails.setAdapter(adapterPaymentCardsList);
                            lvCardListDetails.addFooterView(footerView);
                        }
                    } else {
                        if(getActivity() !=null) {
                            Toast.makeText(getActivity(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                            AdapterPaymentCardsList adapterPaymentCardsList = new AdapterPaymentCardsList(getActivity(), R.layout.row_payment_list_card, arrCardList);
                            lvCardListDetails.setAdapter(adapterPaymentCardsList);
                            lvCardListDetails.addFooterView(footerView);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(),getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
