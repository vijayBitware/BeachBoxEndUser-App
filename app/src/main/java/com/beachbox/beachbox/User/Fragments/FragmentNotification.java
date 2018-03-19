package com.beachbox.beachbox.User.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Adapter.AdapterCartList;
import com.beachbox.beachbox.User.Adapter.AdapterNotification;

import com.beachbox.beachbox.User.Model.notificationResponse.Notification;
import com.beachbox.beachbox.User.Model.notificationResponse.ResponseNotificationNew;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used for displaying list of notification.
 * Created by bitwarepc on 11-Aug-17.
 */

public class FragmentNotification extends Fragment implements APIRequest.ResponseHandler {
    View view;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    LinearLayout llLoginOrder, llLoginSuccessOrder;
    ListView lv_notification;
    AdapterNotification adapterNotification ;
    ArrayList<ResponseNotificationNew> arrNoti = new ArrayList<>();
    TextView tvLoginOrder;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view=inflater.inflate(R.layout.fragment_notification,container,false);
            inIt();
            if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                llLoginSuccessOrder.setVisibility(View.VISIBLE);
                cd = new ConnectionDetector(getActivity());
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    APICallForGettingNotifications();
                } else {
                    Toast.makeText(getContext(), getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }
            } else {
                llLoginOrder.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    private void APICallForGettingNotifications() {
        JSONObject jsonObject = new JSONObject();
        String sessionUserTokan = sharedPreferences.getString("userSessionTokan", "");
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token", sessionUserTokan);
            String upcomingOrdersURL = Config.BASE_URL + "user/getAllNotification";
            System.out.println("************"+upcomingOrdersURL+jsonObject.toString());
            new APIRequest(getActivity(), jsonObject, upcomingOrdersURL, this, Config.API_GET_ALLNOTIFICATION, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void inIt() {
        lv_notification = (ListView)view.findViewById(R.id.lv_notification);
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        llLoginOrder = (LinearLayout) view.findViewById(R.id.llLoginOrder);
        llLoginSuccessOrder = (LinearLayout) view.findViewById(R.id.llLoginSuccessOrder);
        tvLoginOrder = (TextView) view.findViewById(R.id.tvLoginOrder);

        tvLoginOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","5");
                editor.commit();
                Intent intent = new Intent(getActivity(), ActivitySignIn.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

    }

    @Override
    public void onSuccess(BaseResponse response) {
        if (response.getApiName() == Config.API_GET_ALLNOTIFICATION) {
            ResponseNotificationNew notiResponse = (ResponseNotificationNew) response;
            if(notiResponse.getIsSuccess() == true){
                List<Notification> arrNotiList = notiResponse.getNotification();
                if(arrNotiList.size() > 0){

                    adapterNotification = new AdapterNotification(getActivity(),R.layout.row_notification,arrNotiList);
                    lv_notification.setAdapter(adapterNotification);

                }else{
                    Toast.makeText(getActivity(), "Notifications are not available yet!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(), "Something went wrong,Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }




}
