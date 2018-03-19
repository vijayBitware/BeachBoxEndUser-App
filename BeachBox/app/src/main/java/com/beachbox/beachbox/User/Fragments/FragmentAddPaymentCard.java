package com.beachbox.beachbox.User.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.AddPaymentCardActivity;
import com.beachbox.beachbox.User.Activities.PaymentCardListActivity;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitwarepc on 21-Jul-17.
 */

public class FragmentAddPaymentCard extends Fragment {
    View view;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
     if(view == null){
         view = inflater.inflate(R.layout.acrtivity_add_paymentcard,container,false);
         inIt();
         if(FragmentCart.isEditCard.equalsIgnoreCase("Yes")){
             String s = sharedPreferences.getString("strSelectCardDetails", "");
             String s1 = s.substring(0, 4);
             String s2 = s.substring(4, 8);
             String s3 = s.substring(8, 12);
             String s4 = s.substring(12, 16);

             etCardNo.setText(s1+"-"+s2+"-"+s3+"-"+s4);
             String myMonth = "";
             String strCardMonth = sharedPreferences.getString("strSelectedExpMonth", "");
             if(strCardMonth.length() > 1){
                 myMonth = strCardMonth;
             }else{
                 myMonth = "0"+strCardMonth;
             }
             String strMonthYear = myMonth+"/"+sharedPreferences.getString("strSelectedExpYear", "");
             etmonth.setText(strMonthYear);
             etCvv.setText(sharedPreferences.getString("strSelectedCVV", ""));
             etZipCode.setText(sharedPreferences.getString("strSelectedZip", ""));
             chkRememberPass.setVisibility(View.INVISIBLE);
             String checkedStatus  = sharedPreferences.getString("chckedStatus", "");
             if(checkedStatus.equalsIgnoreCase("yes")){
                 chkRememberPass.setChecked(true);
             }else{
                 chkRememberPass.setChecked(false);
             }
         }
     }

        return view;
    }

    private void inIt() {

        cd = new ConnectionDetector(getActivity());
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();

        etCardNo = (EditText)view. findViewById(R.id.etCardNo);
        etZipCode = (EditText)view. findViewById(R.id.etZipCode);
        etmonth = (EditText)view. findViewById(R.id.etmonth);
        etCvv = (EditText)view. findViewById(R.id.etCvv);
        tvSaveCard = (TextView)view. findViewById(R.id.tvSaveCard);
        tvBackCards = (TextView)view. findViewById(R.id.tvBackCards);
        chkRememberPass = (CheckBox)view.findViewById(R.id.chkRememberPass);

        tvBackCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getFragmentManager().popBackStackImmediate();
                editor.putString("tabPosition","2");
                editor.commit();
                Intent i = new Intent(getActivity(),UDashboardActivityNew.class);
                startActivity(i);

            }
        });

        chkRememberPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    strCardSaveInDB = "Yes";
                }else{
                    strCardSaveInDB = "No";
                }
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
                    if (strCardNo.length()>=13 && strCardNo.length()<=16) {
                        if (!strMonth.isEmpty()) {
                            if (strMonth.length() == 7) {
                                if (!strCvv.isEmpty()) {
                                    if ( strCvv.length() >= 3 &&  strCvv.length() <= 4) {
                                        if (!strZipCode.isEmpty()) {
                                            if (strZipCode.length() == 5) {
                                                if(strCardSaveInDB.equalsIgnoreCase("Yes")){
                                                    if(FragmentCart.isEditCard.equalsIgnoreCase("yes")){ // For Edit card
                                                        new  saveCardData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + userSessionTokan + "\",\"cardholder_name\":\"" + username + "\" ," +
                                                                "\"card_num\":\"" + strCardNo + "\",\"cc_num\":\"" + strCvv + "\",\"exp_month_year\":\"" + strMonth + "\",\"zip\":\"" + strZipCode + "\",\"card_id\":\"" + sharedPreferences.getString("card_card_id","") + "\" }");
                                                    }else{      // normal plane save card
                                                        new  saveCardData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + userSessionTokan + "\",\"cardholder_name\":\"" + username + "\" ," +
                                                                "\"card_num\":\"" + strCardNo + "\",\"cc_num\":\"" + strCvv + "\",\"exp_month_year\":\"" + strMonth + "\",\"zip\":\"" + strZipCode + "\" }");
                                                    }
                                                }else{
                                                    String arrStr[] = strMonth.split("/");
                                                    String tempMonth = arrStr[0];
                                                    String tempYear = arrStr[1];

                                                    if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
                                                        editor.putString("pickupCC","Yes");
                                                    }
                                                    editor.putString("IsCardParmanant","No");
                                                    editor.putString("tabPosition","2");
                                                    editor.putString("tempCardNo",strCardNo);
                                                    editor.putString("tempCvvNo",strCvv);
                                                    editor.putString("tempCardMonth",tempMonth);
                                                    editor.putString("tempCardYear",tempYear);
                                                    editor.putString("tempZipCode",strZipCode);
                                                    editor.commit();

                                                    Intent i = new Intent(getActivity(),UDashboardActivityNew.class);
                                                    editor.putString("tabPosition","2");
                                                    startActivity(i);

                                                }
                                            } else {
                                                etZipCode.requestFocus();
                                                etZipCode.setError("Please enter valid 5 digits zipcode");
                                            }
                                        } else {
                                            etZipCode.requestFocus();
                                            etZipCode.setError("Please enter zipcode");
                                        }
                                    } else {
                                        etCvv.requestFocus();
                                        etCvv.setError("Enter valid 3 or 4 digits cvv");

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
                        etCardNo.setError("Please enter valid card no");
                    }
                } else {
                    etCardNo.requestFocus();
                    etCardNo.setError("Please enter card no");
                }
            }
        });

    }


    class saveCardData extends AsyncTask<String, Void, String> {
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
                    boolean isSuccess = jsonObject.getBoolean("is_success");

                    if (isSuccess) {
                        if( FragmentCart.isEditCard.equalsIgnoreCase("Yes")){  // Used for on cart Fragment if card is edited is successfullyf or setting the select card message
                            editor.remove("IsCardParmanant");
                            editor.apply();
                        }
                        FragmentCart.isEditCard = "No";
                        Toast.makeText(getActivity(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();
                       // Intent i = new Intent(getActivity(),)

                        ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentPaymentCardList());


                    } else {
                        String errmsg = jsonObject.getString("err_msg");
                        Toast.makeText(getActivity(),errmsg,Toast.LENGTH_SHORT).show();
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
