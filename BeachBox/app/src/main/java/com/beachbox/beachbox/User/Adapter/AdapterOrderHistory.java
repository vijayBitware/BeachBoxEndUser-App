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
import com.beachbox.beachbox.User.Activities.ActivityRatings;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentAddPaymentCard;
import com.beachbox.beachbox.User.Fragments.FragmentCart;
import com.beachbox.beachbox.User.Fragments.FragmentOrderStatus;
import com.beachbox.beachbox.User.Fragments.FragmentPaymentCardList;
import com.beachbox.beachbox.User.Fragments.FragmentRating;
import com.beachbox.beachbox.User.Model.ModelHistoryOrder;
import com.beachbox.beachbox.User.Model.historyOrderResponse.Orderdetail;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bitware on 23/3/17.
 */

public class AdapterOrderHistory  extends ArrayAdapter<Orderdetail> {

        List<Orderdetail> arrRestaurantList = new ArrayList<>();
        LayoutInflater inflater;
        Context context;
        ViewHolder holder;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

public AdapterOrderHistory(Context context, int resource, List<Orderdetail> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;

        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        }

public static class ViewHolder {
    TextView tv_restaurantName, tv_dateTime, tv_restaurantRating,tv_price,tvRatingText,tvresType;
    ImageView iv_restaurantImage,ivStar;
}

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_history_orders, null);
            holder = new ViewHolder();

            holder.tv_restaurantName = (TextView) convertView.findViewById(R.id.tv_restaurantName);
            holder.tv_dateTime = (TextView) convertView.findViewById(R.id.tv_dateTime);
            holder.tv_restaurantRating = (TextView) convertView.findViewById(R.id.tv_ratingg);
            holder.iv_restaurantImage = (ImageView) convertView.findViewById(R.id.iv_restaurantImage);
            holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
            holder.tvRatingText = (TextView) convertView.findViewById(R.id.tvRatingText);
            holder.ivStar = (ImageView) convertView.findViewById(R.id.ivStar);
            holder.tvresType = (TextView) convertView.findViewById(R.id.resType);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_restaurantName.setText(arrRestaurantList.get(position).getRsName());

       // getFormatedDateTime(arrRestaurantList.get(position).getOrderedDate());
        holder.tv_dateTime.setText(getFormatedDateTime(arrRestaurantList.get(position).getOrderedDate()));
        holder.tv_price.setText("$"+arrRestaurantList.get(position).getPrice());
        String mType = arrRestaurantList.get(position).getOrderType();
        holder.tvresType.setText(mType.substring(0, 1).toUpperCase() + mType.substring(1));

        String resImage = arrRestaurantList.get(position).getRsPic();
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            holder.iv_restaurantImage.setImageResource(R.drawable.blank_resturant);
        }else {
            Picasso.with(context)
                    .load(arrRestaurantList.get(position).getRsPic())
                    .into(holder.iv_restaurantImage);
        }
        if (arrRestaurantList.get(position).getRating() == null){
            holder.tvRatingText.setVisibility(View.VISIBLE);
            holder.tv_restaurantRating.setVisibility(View.INVISIBLE);
            holder.ivStar.setVisibility(View.INVISIBLE);
        }else {
            int rating =  arrRestaurantList.get(position).getRating().intValue();
            holder.tv_restaurantRating.setText(String.valueOf(rating));
        }


        holder.tvRatingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("navigation","fromOrderHistory");

                editor.putString("order_id",String.valueOf(arrRestaurantList.get(position).getOrderId()));
                editor.putString("resDateTime",arrRestaurantList.get(position).getOrderedDate());
                editor.putString("resName",arrRestaurantList.get(position).getRsName());
                editor.putString("resImage",arrRestaurantList.get(position).getRsPic());
                editor.putString("resPrice",String.valueOf(arrRestaurantList.get(position).getPrice()));
                editor.putString("resCustName",arrRestaurantList.get(position).getCustomerName());
                editor.putString("restoId",String.valueOf(arrRestaurantList.get(position).getRestaurantId()));
                editor.commit();

                ((UDashboardActivityNew)context).replaceFragment(new FragmentRating());
            }
        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("navigation","fromOrderHistory");

                editor.putString("order_id",String.valueOf(arrRestaurantList.get(position).getOrderId()));
                editor.putString("resDateTime",arrRestaurantList.get(position).getOrderedDate());
                editor.putString("resName",arrRestaurantList.get(position).getRsName());
                editor.putString("resImage",arrRestaurantList.get(position).getRsPic());
                editor.putString("resPrice",String.valueOf(arrRestaurantList.get(position).getPrice()));
                editor.putString("resCustName",arrRestaurantList.get(position).getCustomerName());
                editor.putString("restoId",String.valueOf(arrRestaurantList.get(position).getRestaurantId()));
                editor.commit();

                ((UDashboardActivityNew)context).replaceFragment(new FragmentOrderStatus());
               // context.startActivity(new Intent(context, ActivityRatings.class));

            }
        });
        return convertView;
    }

    public String getFormatedDateTime(String orderedDate) {
        String myMonth = "";
        String[] arrTest = orderedDate.split("\\s+");

        String mDate = arrTest[0];
        String mTime = arrTest[1];

        String[] actDate = mDate.split("-");
        String dateYear = actDate[0];
        String datemonth = actDate[1];
        String datday = actDate[2];


        switch (datemonth){
            case "01":
                myMonth = "Jan";
                break;
            case "02":
                myMonth = "Feb";
                break;
            case "03":
                myMonth = "Mar";
                break;
            case "04":
                myMonth = "Apr";
                break;
            case "05":
                myMonth = "May";
                break;
            case "06":
                myMonth = "Jun";
                break;
            case "07":
                myMonth = "Jul";
                break;
            case "08":
                myMonth = "Aug";
                break;
            case "09":
                myMonth = "Sept";
                break;
            case "10":
                myMonth = "Oct";
                break;
            case "11":
                myMonth = "Nov";
                break;

            case "12":
                myMonth = "Dec";
                break;
        }
        String newVal = myMonth+" "+datday;

        String[] actTime = mTime.split(":");
        String time1 = actTime[0];
        String time2 = actTime[1];
        String time3 = actTime[2];
        String strAMPMVal = "";
        if(Integer.parseInt(time1) < 12){
            strAMPMVal = "AM";
        }else{
            strAMPMVal = "PM";
        }
            String newtime = time1+":"+time2;//+" "+strAMPMVal;


        String start_dt = newtime;
        String formatedTime = null;
        DateFormat parser = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = (Date) parser.parse(start_dt);
            DateFormat formatter = new SimpleDateFormat("hh:mm");
            System.out.println("******dateeeeeeeeeee******"+formatter.format(date));
            formatedTime = formatter.format(date)+" "+strAMPMVal;
        } catch (ParseException e) {
            e.printStackTrace();
        }
       

        return newVal+" "+formatedTime;
    }

}
