package com.beachbox.beachbox.User.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentOrderStatus;
import com.beachbox.beachbox.User.Model.notificationResponse.Notification;
import com.squareup.picasso.Picasso;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class used for showing notification list.
 * Created by bitwarepc on 11-Aug-17.
 */

public class AdapterNotification extends ArrayAdapter<Notification> {

    List<Notification> arrRestaurantList = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterNotification(Context context, int resource, List<Notification> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;

        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tvNotiMsg,tvNotiDate,tvNotitypeOrder;
        ImageView iv_notiImg;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_notification, null);
            holder = new ViewHolder();

            holder.iv_notiImg = (ImageView) convertView.findViewById(R.id.iv_notiImg);
            holder.tvNotiMsg= (TextView) convertView.findViewById(R.id.tvNotiMsg);
            holder.tvNotiDate= (TextView) convertView.findViewById(R.id.tvNotiDate);
            holder.tvNotitypeOrder = (TextView)  convertView.findViewById(R.id.tvNotitypeOrder);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String resImage = arrRestaurantList.get(position).getRestaurantImage();
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            holder.iv_notiImg.setImageResource(R.drawable.blank_resturant);
        }else {
            Picasso.with(context)
                    .load(arrRestaurantList.get(position).getRestaurantImage())
                    .into(holder.iv_notiImg);
        }
          holder.tvNotiMsg.setText(arrRestaurantList.get(position).getNotification().toString().trim());
          holder.tvNotiDate.setText(getFormatedDateTime(arrRestaurantList.get(position).getNotificationDate()));
          String orderType = arrRestaurantList.get(position).getOrderType();
          holder.tvNotitypeOrder.setText(orderType.substring(0, 1).toUpperCase() + orderType.substring(1));


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("navigation","Notification");
                editor.putString("orderStatus",arrRestaurantList.get(position).getOrderStatus());
                editor.putString("order_id",arrRestaurantList.get(position).getOrderId()+"");
                editor.putString("orderType",arrRestaurantList.get(position).getOrderType()+"");

                System.out.println("*********nt*******"+arrRestaurantList.get(position).getOrderStatus());
                editor.commit();

                ((UDashboardActivityNew)context).replaceFragment(new FragmentOrderStatus());


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



