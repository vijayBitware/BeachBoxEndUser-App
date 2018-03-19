package com.beachbox.beachbox.User.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Activities.ActivitySignUp;
import com.beachbox.beachbox.User.Adapter.AdapterOrderHistory;
import com.beachbox.beachbox.User.Adapter.AdapterUpcomingOrder;
import com.beachbox.beachbox.User.Model.ModelHistoryOrder;
import com.beachbox.beachbox.User.Model.ModelUpcomingOrder;
import com.beachbox.beachbox.User.Model.historyOrderResponse.HistoryOrderResponse;
import com.beachbox.beachbox.User.Model.historyOrderResponse.Orderdetail;
import com.beachbox.beachbox.User.Model.upcomingOrders.UpcomingOrderResponse;
import com.beachbox.beachbox.User.Model.upcomingOrders.Upcomingorder;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static com.beachbox.beachbox.R.id.llLogin;
import static com.beachbox.beachbox.R.id.llLoginSuccess;

/**
 * Created by bitware on 10/7/17.
 */

public class FragmentOrder extends Fragment implements APIRequest.ResponseHandler {

    View view;
    ListView lv_upcomingOrders;
    TextView tv_upcoming, tv_history,tvListHasNoData;
    AdapterUpcomingOrder adapterUpcomingOrder;
    AdapterOrderHistory adapterOrderHistory;
    ArrayList<ModelUpcomingOrder> arrUpcomingOrder;
    ArrayList<ModelHistoryOrder> arrHistoryOrder;
    ImageView iv_upcoming, iv_history;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    String sessionUserTokan = "";
    LinearLayout llLoginOrder, llLoginSuccessOrder;
    TextView tvLoginOrder;
    String APICall = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_order, container, false);
            init();
            if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                llLoginSuccessOrder.setVisibility(View.VISIBLE);
                sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
                if (isInternetPresent) {
                    APICallForUpComingOrders();
                } else {
                    Toast.makeText(getContext(), getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }
            } else {
                llLoginOrder.setVisibility(View.VISIBLE);
            }

            tv_history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_history.setVisibility(View.VISIBLE);
                    iv_upcoming.setVisibility(View.INVISIBLE);
                    tvListHasNoData.setVisibility(View.GONE);
                    sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
                    if (isInternetPresent) {

                        APICallForOrderHistory();
                        //new OrderHistory().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"from_date\":\"" + "" + "\",\"to_date\":\"" + "" + "\",\"status\":\"" + "" + "\",\"session_user_token\":\"" + sessionUserTokan+ "\"}");
                    } else {
                        Toast.makeText(getContext(), getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tv_upcoming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_upcoming.setVisibility(View.VISIBLE);
                    iv_history.setVisibility(View.INVISIBLE);
                    tvListHasNoData.setVisibility(View.GONE);
                    sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
                    if (isInternetPresent) {
                        APICallForUpComingOrders();
                        // new UpcomingOrders().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + sessionUserTokan+ "\"}");
                    } else {
                        Toast.makeText(getContext(), getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tvLoginOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putString("tabPosition","3");
                    editor.commit();
                    Intent intent = new Intent(getActivity(), ActivitySignIn.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });

        }

        return view;
    }

    private void init() {
        sharedPreferences = getContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        iv_upcoming = (ImageView) view.findViewById(R.id.iv_upcoming);
        iv_history = (ImageView) view.findViewById(R.id.iv_history);
        tv_upcoming = (TextView) view.findViewById(R.id.tv_upcoming);
        tv_history = (TextView) view.findViewById(R.id.tv_history);
        lv_upcomingOrders = (ListView) view.findViewById(R.id.lv_orders);
        llLoginOrder = (LinearLayout) view.findViewById(R.id.llLoginOrder);
        llLoginSuccessOrder = (LinearLayout) view.findViewById(R.id.llLoginSuccessOrder);
        tvLoginOrder = (TextView) view.findViewById(R.id.tvLoginOrder);
        tvListHasNoData = (TextView) view.findViewById(R.id.tvListHasNoData);
    }

    private void APICallForUpComingOrders() {
        APICall = "Upcoming";
        JSONObject jsonObject = new JSONObject();
        sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sessionUserTokan);
            String upcomingOrdersURL = Config.BASE_URL + "upcomingorders";
            new APIRequest(getActivity(), jsonObject, upcomingOrdersURL, this, Config.API_GET_UPCOMING_ORDER, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void APICallForOrderHistory() {
        APICall = "OrderHistory";
        JSONObject jsonObject = new JSONObject();
        sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sessionUserTokan);
            jsonObject.put("from_date", "");
            jsonObject.put("to_date", "");
            jsonObject.put("status", "");

            String orderHistoryURL = Config.BASE_URL + "orderhistory";
            System.out.println(">>> History request body :" + jsonObject);
            new APIRequest(getActivity(), jsonObject, orderHistoryURL, this, Config.API_GET_HISTORY_ORDER, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(BaseResponse response) {
        if (isAdded() && getActivity() != null) {
            System.out.println("api name "+APICall);
            if (response.getApiName()==Config.API_GET_UPCOMING_ORDER) {
                UpcomingOrderResponse upcomingOrderResponse = (UpcomingOrderResponse) response;
                if (upcomingOrderResponse.getIsSuccess()) {
                    List<Upcomingorder> upcomingorders = upcomingOrderResponse.getUpcomingorders();
                    if (upcomingorders.size() > 0) {
                        if (getActivity() != null) {
                            adapterUpcomingOrder = new AdapterUpcomingOrder(getActivity(), R.layout.row_upcoming_orders, upcomingorders);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                        }

                    } else {
                        if (getActivity() != null) {
                            tvListHasNoData.setVisibility(View.VISIBLE);
                            tvListHasNoData.setText(getActivity().getResources().getString(R.string.upcomingOrderErrMsg));
                           /* adapterUpcomingOrder = new AdapterUpcomingOrder(getActivity(), R.layout.row_upcoming_orders, upcomingorders);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                            Toast.makeText(getActivity(), "No upcoming orders available,", Toast.LENGTH_SHORT).show();*/
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Something went wrong! please try again later", Toast.LENGTH_SHORT).show();
                }
            } else if (response.getApiName()==Config.API_GET_HISTORY_ORDER){
                HistoryOrderResponse historyOrderResponse = (HistoryOrderResponse) response;
                if (historyOrderResponse.getIsSuccess()) {
                    List<Orderdetail> listHistoryOrders = historyOrderResponse.getOrderdetails();
                    if (listHistoryOrders.size() > 0) {
                        if (getActivity() != null) {
                            adapterOrderHistory = new AdapterOrderHistory(getActivity(), R.layout.row_history_orders, listHistoryOrders);
                            lv_upcomingOrders.setAdapter(adapterOrderHistory);
                        }

                    } else {
                        if (getActivity() != null) {
                            tvListHasNoData.setVisibility(View.VISIBLE);
                            tvListHasNoData.setText(getActivity().getResources().getString(R.string.historyOrderErrMsg));
   }
                    }
                } else {
                    Toast.makeText(getActivity(), "Something went wrong! please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }



}
