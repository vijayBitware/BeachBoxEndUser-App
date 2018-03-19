package com.beachbox.beachbox.User.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityFilterRestaurant;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Model.filterSubMenuResponse.ResponseFilterSubMenu;
import com.beachbox.beachbox.expandable.CustomAdapter1;
import com.beachbox.beachbox.expandable.GroupInfo1;
import com.beachbox.beachbox.expandable.Menu;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class used for filtering restaurent list depending on user choice.
 * Created by bitwarepc on 24-Jul-17.
 */

public class FragmentFilter extends Fragment implements APIRequest.ResponseHandler, CustomAdapter1.MyInterface {
    View view;
    TextView tvDonefilter, tvFilterBack;
    //ArrayList<ModelFilterRestaurant> arrFilter = new ArrayList<>();
    private ExpandableListView lvFilter;
    //TextView tvFilterText;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent;

    ArrayList<Integer> arrCuisine = new ArrayList<>();
    ArrayList<Integer> arrDietry = new ArrayList<>();
    ArrayList<Integer> arrRating = new ArrayList<>();
    ArrayList<Integer> arrFavourite = new ArrayList<>();

    View footerView;
    String favouriteFlag,ratingsFlag;;
    CheckBox chkFavourite,chkRating;
    public static JSONObject jObj;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_filter_restaurant, container, false);
            footerView = inflater.inflate(R.layout.filter_footerview, null);
            inIt();

            if (isInternetPresent) {
                JSONObject jsonObject = new JSONObject();
                try {
                    String singInURL = Config.BASE_URL + "user/restaurants-filter";
                    new APIRequest(getActivity(), jsonObject, singInURL, this, Config.API_FILTER_SUB_MENU, Config.GET);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
            }
        }
        return view;
    }

    //initialization
    private void inIt() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        tvDonefilter = (TextView)view. findViewById(R.id.tvDonefilter);
        lvFilter = (ExpandableListView)view. findViewById(R.id.lvFilter);
        tvFilterBack = (TextView)view. findViewById(R.id.tvFilterBack);
        chkFavourite = (CheckBox) footerView. findViewById(R.id.chkFavourite);
        chkRating = (CheckBox) footerView.findViewById(R.id.chkRating);

        tvDonefilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject subObj = new JSONObject();
                try {
                    if(arrDietry.size() > 0){
                        editor.putString("isFilter","Yes");
                        subObj.put("dietary",new JSONArray(arrDietry));
                    }
                    if(arrCuisine.size() > 0){
                        editor.putString("isFilter","Yes");
                        subObj.put("cuisines",new JSONArray(arrCuisine));
                    }
                    if(arrRating.size() > 0){
                        editor.putString("isFilter","Yes");
                        subObj.put("rating",new JSONArray(arrRating));
                    }
                    if(arrFavourite.size() > 0){
                        editor.putString("isFilter","Yes");
                        subObj.put("favourite",new JSONArray(arrFavourite));
                    }
                    editor.commit();
                    jObj = subObj;

                    ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentRestaurants());


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        tvFilterBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition", "0");
                editor.commit();

                ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentRestaurants());

            }
        });


        chkFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    arrFavourite.add(1);

                }else{
                    arrFavourite.remove(0);  //0 is position
                }

            }
        });
        chkRating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    arrRating.add(1);
                }else{
                    arrRating.remove(0); //0 is position
                }
            }
        });

    }

    @Override
    public void OnCheck(int groupPosition, Menu menu, String action) {
        if (action.equals("add")) {
            if(groupPosition == 0){ //for cuisine
                arrCuisine.add(menu.getId());
            }else{
                arrDietry.add(menu.getId());
            }

        } else {
            if(groupPosition == 0){
                arrCuisine.remove(menu.getId());
            }else{
                arrDietry.remove(menu.getId());
            }
        }
    }

    @Override
    public void onSuccess(BaseResponse response) {
        ResponseFilterSubMenu filterSubMenu = (ResponseFilterSubMenu) response;
        if (filterSubMenu.getIsSuccess()) {

            ArrayList<GroupInfo1> menuList = new ArrayList<>();

            GroupInfo1 menuItem1 = new GroupInfo1();
            menuItem1.setName("Cuisine");
            menuItem1.setProductList(filterSubMenu.getCuisinesMenu());
            menuList.add(menuItem1);

            GroupInfo1 menuItem2 = new GroupInfo1();
            menuItem2.setName("Dietary Restrictions");
            menuItem2.setProductList(filterSubMenu.getDietaryMenu());
            menuList.add(menuItem2);

            CustomAdapter1 customAdapter1 = new CustomAdapter1(getActivity(), menuList, this);
            lvFilter.addFooterView(footerView);
            lvFilter.setAdapter(customAdapter1);

        } else {
            Toast.makeText(getActivity(), "Somehting went wrong please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }
}
