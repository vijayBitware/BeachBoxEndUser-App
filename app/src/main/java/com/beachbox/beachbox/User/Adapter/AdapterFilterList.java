package com.beachbox.beachbox.User.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Model.ModelFilterRestaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used for showing filter restaurents list.
 * Created by bitwarepc on 12-Jul-17.
 */

public class AdapterFilterList extends ArrayAdapter<ModelFilterRestaurant> {

    List<ModelFilterRestaurant> arrFilter = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AQuery aQuery;
    ConnectionDetector cd;
    Boolean isInternetPresent;
int pos;

    public AdapterFilterList(Context context, int resource, List<ModelFilterRestaurant> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrFilter = arrRestaurantList;
        this.context = context;
        aQuery = new AQuery(context);

        cd = new ConnectionDetector(context);
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tvFilterText;
    }

    @Override
    public int getCount() {
        return arrFilter.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_filter_list, null);

        holder = new ViewHolder();
        holder.tvFilterText= (TextView) convertView.findViewById(R.id.tvFilterText);

        convertView.setTag(holder);
        holder.tvFilterText.setText(arrFilter.get(position).getFilterText());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, arrFilter.get(position).getFilterText(), Toast.LENGTH_LONG).show();
                editor.putString("filterVal",arrFilter.get(position).getFilterText());
                editor.commit();
            }
        });

        return convertView;

    }

}


