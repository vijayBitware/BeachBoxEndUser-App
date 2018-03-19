package com.beachbox.beachbox.User.Adapter;

/**
 * Created by bitwarepc on 11-Jul-17.
 */

/*
public class AdapterPaymentCardsList {
}
*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Model.ModelPaymentsCardList;
import com.beachbox.beachbox.User.Model.removePaymentCard.ResponseRemoveCard;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitwarepc on 04-Jul-17.
 */

public class AdapterPaymentCardsList extends ArrayAdapter<ModelPaymentsCardList> implements APIRequest.ResponseHandler {

    List<ModelPaymentsCardList> arrRestaurantList = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    AQuery aQuery;
    String imgPath = "";
    int removedPos;
    String removedCard = "";
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterPaymentCardsList(Context context, int resource, List<ModelPaymentsCardList> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;
        aQuery = new AQuery(context);

        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent=cd.isConnectingToInternet();
    }



    public static class ViewHolder {
        TextView tvCardNo,tvDefaultText;
        ImageView ivRemoveCard;
        LinearLayout llMain;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_payment_list_card, null);

        holder = new ViewHolder();
        holder.tvCardNo= (TextView) convertView.findViewById(R.id.tvDisplayCardNo);
        holder.tvDefaultText = (TextView) convertView.findViewById(R.id.tvDefaultText);
        holder.ivRemoveCard= (ImageView) convertView.findViewById(R.id.ivRemoveCard);
        holder.llMain = (LinearLayout) convertView.findViewById(R.id.llMain);

        convertView.setTag(holder);

        if(arrRestaurantList.size()>0){
            String strCard = arrRestaurantList.get(position).getCredit_card_mask();
            if(strCard.length()>4){
                String strMaskedString = strCard.substring(strCard.length()-4);
                 holder.tvCardNo.setText("************"+strMaskedString);
            }else{
                holder.tvCardNo.setText("************"+strCard);
            }

            String defaultCard = sharedPreferences.getString("SelectedDefaultCard","");
            if(defaultCard.equalsIgnoreCase(arrRestaurantList.get(position).getCredit_card_mask())){
                holder.tvDefaultText.setText("Default");
            }
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removedCard = arrRestaurantList.get(position).getCredit_card_mask();
                //means defalt card & selected card is same then move directly else ask default dialog
                if(sharedPreferences.getString("SelectedDefaultCard","").equalsIgnoreCase(arrRestaurantList.get(position).getCredit_card_mask())){ // for removing default cart
                    editor.putString("IsCardParmanant","Yes");   //used for send card data to the server.
                    //editor.putString("SelectedId",arrRestaurantList.get(position).getId());
                    editor.putString("getSelectedCard",arrRestaurantList.get(position).getCredit_card_mask());
                    editor.putString("card_cc_transaction_id",arrRestaurantList.get(position).getCredit_card_mask());
                    editor.putString("card_cvv_num",arrRestaurantList.get(position).getCard_cvv());
                    editor.putString("card_user_id",arrRestaurantList.get(position).getUser_id());
                    editor.putString("card_exp_month",arrRestaurantList.get(position).getCard_exp_month());
                    editor.putString("card_exp_year",arrRestaurantList.get(position).getCard_exp_year());
                    editor.putString("card_card_id",arrRestaurantList.get(position).getId());
                    editor.putString("card_zip_code",arrRestaurantList.get(position).getZipCode());
                    editor.commit();

                    Intent intent = new Intent(context,UDashboardActivityNew.class);
                    context.startActivity(intent);
                    editor.putString("tabPosition","2");
                    editor.commit();
                }else{
                    askDefaultSetPaymentDialog(position);
                }


            }
        });

        holder.ivRemoveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetPresent){
                    if(sharedPreferences.getString("isUserLoggedIn","").equalsIgnoreCase("Yes")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Beach Box Enterprise");
                        builder.setMessage("Do you want to remove this payment card permanently?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        removePaymentCard(position);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }else{
                        Toast.makeText(context,"For removing cards you have to login first",Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getContext(),getContext().getResources().getString(R.string.noNetworkMsg) , Toast.LENGTH_SHORT).show();
                }
            }


        });

        return convertView;
    }

    private void askDefaultSetPaymentDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Beach Box Enterprise");
        builder.setMessage("Do you want to set this payment as a default?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
                            editor.putString("pickupCC","Yes");
                        }
                        editor.putString("setDefaultCard","Yes");
                        editor.putString("IsCardParmanant","Yes");   //used for send card data to the server.
                        //editor.putString("SelectedId",arrRestaurantList.get(position).getId());
                        editor.putString("SelectedDefaultCard",arrRestaurantList.get(position).getCredit_card_mask());
                        editor.putString("getSelectedCard",arrRestaurantList.get(position).getCredit_card_mask());
                        editor.putString("card_cc_transaction_id",arrRestaurantList.get(position).getCredit_card_mask());
                        editor.putString("card_cvv_num",arrRestaurantList.get(position).getCard_cvv());
                        editor.putString("card_user_id",arrRestaurantList.get(position).getUser_id());
                        editor.putString("card_exp_month",arrRestaurantList.get(position).getCard_exp_month());
                        editor.putString("card_exp_year",arrRestaurantList.get(position).getCard_exp_year());
                        editor.putString("card_card_id",arrRestaurantList.get(position).getId());
                        editor.putString("card_zip_code",arrRestaurantList.get(position).getZipCode());
                        editor.commit();

                        Intent intent = new Intent(context,UDashboardActivityNew.class);
                        context.startActivity(intent);
                        editor.putString("tabPosition","2");
                        editor.commit();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(ActivityHome.strFlowType.equalsIgnoreCase("pickup")){
                            editor.putString("pickupCC","Yes");
                        }
                       // editor.putString("setDefaultCard","No");
                        editor.putString("IsCardParmanant","Yes");   //used for send card data to the server.
                        //editor.putString("SelectedId",arrRestaurantList.get(position).getId());
                        editor.putString("getSelectedCard",arrRestaurantList.get(position).getCredit_card_mask());
                        editor.putString("card_cc_transaction_id",arrRestaurantList.get(position).getCredit_card_mask());
                        editor.putString("card_cvv_num",arrRestaurantList.get(position).getCard_cvv());
                        editor.putString("card_user_id",arrRestaurantList.get(position).getUser_id());
                        editor.putString("card_exp_month",arrRestaurantList.get(position).getCard_exp_month());
                        editor.putString("card_exp_year",arrRestaurantList.get(position).getCard_exp_year());
                        editor.putString("card_card_id",arrRestaurantList.get(position).getId());
                        editor.putString("card_zip_code",arrRestaurantList.get(position).getZipCode());
                        editor.commit();

                        Intent intent = new Intent(context,UDashboardActivityNew.class);
                        context.startActivity(intent);
                        editor.putString("tabPosition","2");
                        editor.commit();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removePaymentCard(int position) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accesstoken", Config.accessToken);
            jsonObject.put("session_user_token",sharedPreferences.getString("userSessionTokan",""));
            jsonObject.put("card_id",arrRestaurantList.get(position).getId());
            removedCard = arrRestaurantList.get(position).getCredit_card_mask();
            String removeCardURL  = Config.BASE_URL+"removeCards";
            removeCardRequest(jsonObject, removeCardURL,position);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeCardRequest(JSONObject jsonObject, String removeCardURL, int position) {
        removedPos = position;
        new APIRequest(getContext(), jsonObject, removeCardURL, this, Config.REMOVE_PAYMENT_CARD, Config.POST);
    }

    @Override
    public void onSuccess(BaseResponse response) {
        ResponseRemoveCard removeCard = (ResponseRemoveCard) response;
        if(removeCard.getIsSuccess()){

            System.out.println(" >>> Selcted Card :"+sharedPreferences.getString("getSelectedCard",""));
            System.out.println(" >>> Removed Card :"+removedCard);

            if(sharedPreferences.getString("getSelectedCard","").equalsIgnoreCase(removedCard)){ // for removing default cart
                editor.remove("getSelectedCard");
                editor.apply();
            }
            arrRestaurantList.remove(removedPos);
            notifyDataSetChanged();
            Toast.makeText(getContext(), removeCard.getErrMsg(), Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getContext(), removeCard.getErrMsg(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onFailure(BaseResponse response) {

    }

}




