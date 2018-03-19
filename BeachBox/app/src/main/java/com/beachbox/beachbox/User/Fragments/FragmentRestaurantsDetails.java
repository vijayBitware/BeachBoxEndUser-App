package com.beachbox.beachbox.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.beachbox.beachbox.Database.DatabaseHandler;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityRestaurantMenuList;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurantMenuList;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Bitware Marketing on 27-03-2017.
 */

public class FragmentRestaurantsDetails extends Fragment {

    ListView lv_menuList;
    AdapterRestaurantMenuList adapterRestaurantMenuList;
    ArrayList<ModelRestaurantDetails> arrMenuList;
    TextView tv_back;
    View view;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DatabaseHandler databaseHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_restaurant_menu_list, container, false);
            init();
        }
        return view;
    }

    private void init() {

        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        tv_back= (TextView)view. findViewById(R.id.tv_back);
        lv_menuList= (ListView)view. findViewById(R.id.lv_menuList);

        if(getActivity() != null){
            adapterRestaurantMenuList=new AdapterRestaurantMenuList(getActivity(), R.layout.row_restaurantmenu,arrMenuList);
            lv_menuList.setAdapter(adapterRestaurantMenuList);
        }

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  editor.putString("tabPosition","0");
                editor.commit();*/
                Intent intent = new Intent(getActivity(), UDashboardActivityNew.class);
                startActivity(intent);
            }
        });
    }



}