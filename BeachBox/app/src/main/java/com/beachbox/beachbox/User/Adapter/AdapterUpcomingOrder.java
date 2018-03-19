package com.beachbox.beachbox.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityOrderStatus;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentOrder;
import com.beachbox.beachbox.User.Fragments.FragmentOrderStatus;
import com.beachbox.beachbox.User.Fragments.FragmentRating;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurantMenuDetails;
import com.beachbox.beachbox.User.Model.ModelUpcomingOrder;
import com.beachbox.beachbox.User.Model.upcomingOrders.Upcomingorder;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitware on 23/3/17.
 */

public class AdapterUpcomingOrder  extends ArrayAdapter<Upcomingorder> {

    List<Upcomingorder> arrRestaurantList = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterUpcomingOrder(Context context, int resource, List<Upcomingorder> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;

        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tv_restaurantName,tv_restaurantDes,tv_restaurantRating,tvresType;
        ImageView iv_restaurantImage;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_upcoming_orders, null);
            holder = new ViewHolder();

            holder.tv_restaurantName= (TextView) convertView.findViewById(R.id.tv_restaurantName);
            holder.tv_restaurantDes= (TextView) convertView.findViewById(R.id.tv_restaurantDes);
            holder.tv_restaurantRating= (TextView) convertView.findViewById(R.id.tv_rating);
            holder.iv_restaurantImage = (ImageView) convertView.findViewById(R.id.iv_restaurantImage);
            holder.tvresType = (TextView)  convertView.findViewById(R.id.resType);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_restaurantName.setText(arrRestaurantList.get(position).getRsName());
        String mType = arrRestaurantList.get(position).getOrderType();
        holder.tvresType.setText(mType.substring(0, 1).toUpperCase() + mType.substring(1));

        String resImage = arrRestaurantList.get(position).getRestaurantImage();
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            holder.iv_restaurantImage.setImageResource(R.drawable.blank_resturant);
        }else {
            Picasso.with(context)
                    .load(arrRestaurantList.get(position).getRestaurantImage())
                    .into(holder.iv_restaurantImage);
        }
        if (arrRestaurantList.get(position).getRsDescription() == null || arrRestaurantList.get(position).getRsDescription().equals("null")){
            holder.tv_restaurantDes.setText("Description Not Available");
        }else {
            holder.tv_restaurantDes.setText(arrRestaurantList.get(position).getRsDescription());
        }
        if (arrRestaurantList.get(position).getRating() == null || arrRestaurantList.get(position).getRating().equals("null")){
            holder.tv_restaurantRating.setText("0");
        }else {
            holder.tv_restaurantRating.setText(arrRestaurantList.get(position).getRating()+"");
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("navigation","fromUpcomingOrder");
                editor.putString("orderStatus",arrRestaurantList.get(position).getStatus());
                editor.putString("order_id",arrRestaurantList.get(position).getId()+"");
                editor.putString("userLat",arrRestaurantList.get(position).getUserLat()+"");
                editor.putString("userLang",arrRestaurantList.get(position).getUserLng()+"");
                editor.putString("orderType",arrRestaurantList.get(position).getOrderType()+"");
                editor.commit();
                ((UDashboardActivityNew)context).replaceFragment(new FragmentOrderStatus());
               // context.startActivity(new Intent(context, ActivityOrderStatus.class));
            }
        });
        return convertView;
    }

    /*public void replaceFragmentNew(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = ((UDashboardActivityNew) getContext()).getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction().addToBackStack(fragment.getClass().toString());;
            transaction.replace(R.id.frame, fragment);
            transaction.commit();
        }
    }*/

}



