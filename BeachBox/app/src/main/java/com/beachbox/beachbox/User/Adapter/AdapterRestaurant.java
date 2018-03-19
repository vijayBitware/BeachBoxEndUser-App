package com.beachbox.beachbox.User.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityOrderStatus;
import com.beachbox.beachbox.User.Activities.ActivityRestaurantMenuList;
import com.beachbox.beachbox.User.Activities.ActivitySignIn;
import com.beachbox.beachbox.User.Activities.HelpActivity;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentAccount;
import com.beachbox.beachbox.User.Fragments.FragmentOrder;
import com.beachbox.beachbox.User.Fragments.FragmentPaymentCardList;
import com.beachbox.beachbox.User.Fragments.FragmentRating;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurantMenuDetails;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurants;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurantsDetails;
import com.beachbox.beachbox.User.Model.ModelRestList;
import com.beachbox.beachbox.User.Model.favouriteRestaurant.ResponseFavUnfavRestaurant;
import com.beachbox.beachbox.User.Model.restaurantlist.Restaurantslist;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.beachbox.beachbox.R.id.ivAdv;

/**
 * Created by bitwarepc on 15-Jul-17.
 */

public class AdapterRestaurant extends RecyclerView.Adapter<AdapterRestaurant.MyViewHolder> implements APIRequest.ResponseHandler {

    AQuery aQuery;
    String imgPath = "";
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    View itemView;
    List<Restaurantslist> restoList = new ArrayList<>();
    int favUnfavPosition;
    LinearLayout llbg;
    public static String strAdvFlag = "";
    int posi;
    boolean isGPSEnabled = false;

    public AdapterRestaurant(UDashboardActivityNew contextt, ArrayList<Restaurantslist> arrRestList) {
        this.context = contextt;
        this.restoList = arrRestList;
        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        aQuery = new AQuery(this.context);
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        if (restoList.get(position).getAdvertisement() != null) {
            return 0; //If object contain Advertisement
        } else {
            return 1; //If object  does not contain Advertisement
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAdv;
        private TextView tv_restaurantName, tv_restaurantDes, tv_restaurantRating;
        private ImageView ivrateOne, ivrateTwo, ivrateThree, ivrateFour, ivrateFive, iv_restaurantImage, ivfavouriteicon;

        public MyViewHolder(View view, int viewType) {
            super(view);

            if (viewType == 0) {
                ivAdv = (ImageView) view.findViewById(R.id.ivAdv);
            } else {

                tv_restaurantName = (TextView) view.findViewById(R.id.tv_restaurantName);
                tv_restaurantDes = (TextView) view.findViewById(R.id.tv_restaurantDes);
                tv_restaurantRating = (TextView) view.findViewById(R.id.tv_rating);
                iv_restaurantImage = (ImageView) view.findViewById(R.id.iv_restaurantImage);
                ivrateOne = (ImageView) view.findViewById(R.id.ivrateOne);
                ivrateTwo = (ImageView) view.findViewById(R.id.ivrateTwo);
                ivrateThree = (ImageView) view.findViewById(R.id.ivrateThree);
                ivrateFour = (ImageView) view.findViewById(R.id.ivrateFour);
                ivrateFive = (ImageView) view.findViewById(R.id.ivrateFive);
                ivfavouriteicon = (ImageView) view.findViewById(R.id.ivfavouriteicon);
                llbg = (LinearLayout) view.findViewById(R.id.llbg);
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup view, int viewType) {
        MyViewHolder vh = null;
        switch (viewType) {
            case 0:
                itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.row_resto_advertisement, view, false);
                vh = new MyViewHolder(itemView, viewType);
                return vh;

            case 1:
                itemView = LayoutInflater.from(view.getContext()).inflate(R.layout.row_restaurants, view, false);
                vh = new MyViewHolder(itemView, viewType);
                return vh;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final int itemType = getItemViewType(position);
        posi = holder.getAdapterPosition();
        final Restaurantslist modelResto = restoList.get(position);

        if (itemType == 0) {
            String imgAdvPath = modelResto.getImage().trim();
            if (!imgAdvPath.isEmpty()) {
                Picasso.with(context)
                        .load(modelResto.getImage().trim())
                        .into(holder.ivAdv);

            } else {
              //  holder.ivAdv.setImageResource(R.drawable.blank_resturant);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(modelResto.getLink()!=null){
                        editor.putString("title",modelResto.getTitle());
                        editor.putString("helpenduser_url",modelResto.getLink());
                        editor.commit();
                        strAdvFlag = "Yes";
                        Intent i = new Intent(context, HelpActivity.class);
                        context.startActivity(i);
                    }else {
                        Toast.makeText(context, "Oops, Advertisement link not available", Toast.LENGTH_SHORT).show();

                    }
                    Toast.makeText(context, "" + position, Toast.LENGTH_SHORT).show();
                }
            });


        } else if (itemType==1){
            // Setting Restaurant data
            if (modelResto.getRsOpenClosedStatus().equalsIgnoreCase("0")) {
                llbg.setBackgroundColor(context.getResources().getColor(R.color.bgthree));
            }
            if (modelResto.getFavoriteFlag() == 0) {
                holder.ivfavouriteicon.setImageResource(R.drawable.ic_unfav);
            } else {
                holder.ivfavouriteicon.setImageResource(R.drawable.ic_fav);
            }
            holder.tv_restaurantName.setText(modelResto.getRestaurantName());
            if (modelResto.getDescription() != null) {
                holder.tv_restaurantDes.setText(modelResto.getDescription());
            } else {
                holder.tv_restaurantDes.setText("Description not available");
            }
            imgPath = modelResto.getRestaurantImage().trim();
            if (imgPath.equals("") || imgPath.equals(null)) {
                holder.iv_restaurantImage.setImageResource(R.drawable.blank_resturant);
            } else {
                Picasso.with(context)
                        .load(imgPath)
                        .into(holder.iv_restaurantImage);
            }
            holder.tv_restaurantRating.setText("0");
            if (modelResto.getRating() != null) {
                Double resRating = modelResto.getRating();
                holder.tv_restaurantRating.setText(String.valueOf(resRating));
            } else {
                holder.tv_restaurantRating.setText("0");
            }

           holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isInternetPresent) {
                        isGPSEnabled = checkGPSEnabled();
                        if(isGPSEnabled){
                            if(modelResto.getRsOpenClosedStatus().equalsIgnoreCase("0")){
                                Toast.makeText(context, context.getResources().getString(R.string.restauranrtisclosed), Toast.LENGTH_SHORT).show();
                            }else{
                                editor.putString("clickedRestoName", modelResto.getRestaurantName());
                                editor.putString("clickedRestoId", String.valueOf(modelResto.getRestaurantId()));
                                editor.putString("clickedRestoRating", String.valueOf(modelResto.getRating()));
                                editor.putString("clickedRestoImg", modelResto.getRestaurantImage());
                                editor.commit();

                                System.out.println(">>> MY RESTO CLICKED DETAILS :"+String.valueOf(modelResto.getRestaurantId()+"--"+String.valueOf(modelResto.getTitle())));

                                ((UDashboardActivityNew)context).replaceFragment(new FragmentRestaurantMenuDetails());

                            }
                        }else{
                            showSettingsAlert();
                        }
                    }else{
                        Toast.makeText(context, context.getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                    }


                    //Toast.makeText(context, "" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.ivfavouriteicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInternetPresent) {
                        if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("accesstoken", Config.accessToken);
                                jsonObject.put("session_user_token", sharedPreferences.getString("userSessionTokan", ""));
                                jsonObject.put("restaurant_id", restoList.get(position).getRestaurantId());
                                if (restoList.get(position).getFavoriteFlag() == 0) {
                                    jsonObject.put("status", "1");   //for favourite
                                } else {
                                    jsonObject.put("status", "0");   // for Unfavourite
                                }
                                String favouriteUnfavouriteURL = Config.BASE_URL + "user/favouriteUnfavourite";
                                System.out.println(">>>> FAV Un Fav request :" + jsonObject);
                                jsonrequestFavUnfav(jsonObject, favouriteUnfavouriteURL, position);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.favRestoMsg), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                    }
                }


            });
        }
    }

    private void jsonrequestFavUnfav(JSONObject jsonObject, String favouriteUnfavouriteURL, int position) {
        favUnfavPosition = position;
        new APIRequest(context, jsonObject, favouriteUnfavouriteURL, this, Config.API_FAV_UNFAV_RESTAURANT, Config.POST);
    }

    @Override
    public void onSuccess(BaseResponse response) {
        ResponseFavUnfavRestaurant favUnfavRestaurant = (ResponseFavUnfavRestaurant) response;
        if (favUnfavRestaurant.getIsSuccess()) {

            Toast.makeText(context, favUnfavRestaurant.getErrMsg(), Toast.LENGTH_SHORT).show();
           /* restoList.get(favUnfavPosition).setFavoriteFlag(1);
            notifyDataSetChanged();*/
            FragmentManager fragmentManager = ((UDashboardActivityNew) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.replace(R.id.frame, new FragmentRestaurants());
            fragmentTransaction.commit();

        } else {
            Toast.makeText(context, favUnfavRestaurant.getErrMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }

    public void add(List<Restaurantslist> models) {
        restoList.addAll(models);
        notifyDataSetChanged();
    }

    public void update(List<Restaurantslist> models) {
        restoList.clear();
        restoList.addAll(models);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return restoList.size();
    }


    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps_enabled && network_enabled) {
                resVal = true;
            } else {
                resVal = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resVal;
    }
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

}

