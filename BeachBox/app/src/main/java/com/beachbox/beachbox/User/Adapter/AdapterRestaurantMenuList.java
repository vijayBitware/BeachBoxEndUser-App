package com.beachbox.beachbox.User.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Database.DatabaseHandler;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityHome;
import com.beachbox.beachbox.User.Activities.ActivityRestaurantMenuList;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Model.ModelOrderDetail;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by bitware on 23/3/17.
 */

public class AdapterRestaurantMenuList extends ArrayAdapter<ModelRestaurantDetails> implements AdapterCartList.myInterface {

    ArrayList<ModelRestaurantDetails> arrRestaurantList;
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int counter = 1;
    private int[] counters;
    int actPos;
    DatabaseHandler dbHelper;
    Realm realm;

    public AdapterRestaurantMenuList(Context context, int resource, ArrayList<ModelRestaurantDetails> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;
        dbHelper = new DatabaseHandler(context);
        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        realm = RealmController.getInstance().getRealm();
        editor = sharedPreferences.edit();
        counters = new int[100];

    }

    @Override
    public void getCartTotalPrice(String mPrice) {

    }

    @Override
    public void updateCartCount(int cartCount) {

    }

    public static class ViewHolder {
        TextView tv_menuName, tv_menuDes, tv_menuPrice, tv_count;
        ImageView iv_add, iv_remove;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder viewHolder;
        View vi = convertView;
        actPos = getItemViewType(position);

        if (vi == null) {
            vi = inflater.inflate(R.layout.row_restaurantmenu, null);
            viewHolder = new ViewHolder();

            viewHolder.tv_menuName = (TextView) vi.findViewById(R.id.tv_menuName);
            viewHolder.tv_menuDes = (TextView) vi.findViewById(R.id.tv_menuDes);
            viewHolder.tv_menuPrice = (TextView) vi.findViewById(R.id.tv_menuPrice);
            viewHolder.iv_add = (ImageView) vi.findViewById(R.id.iv_add);
            viewHolder.iv_remove = (ImageView) vi.findViewById(R.id.iv_remove);
            viewHolder.tv_count = (TextView) vi.findViewById(R.id.tv_count);

            vi.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) vi.getTag();
        }
        viewHolder.iv_add.setTag(position);
        viewHolder.iv_remove.setTag(position);

        viewHolder.tv_menuName.setText(arrRestaurantList.get(position).getMenu_name());
        viewHolder.tv_menuPrice.setText("$ " + arrRestaurantList.get(position).getMenu_price());
        viewHolder.tv_count.setText(arrRestaurantList.get(position).getQty());
        String desc = arrRestaurantList.get(position).getMenu_description();

        if (!desc.equals("null")) {
            viewHolder.tv_menuDes.setText(desc);
        } else {
            viewHolder.tv_menuDes.setText("Description not available");
        }

        viewHolder.iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int counterval = Integer.parseInt(viewHolder.tv_count.getText().toString().trim());

                int temp = counterval;
                temp++;
                /////////////////
                String strCrtCount = sharedPreferences.getString("CartCnt", "");
                System.out.println("********cnt*********" + strCrtCount);
                if (strCrtCount.equalsIgnoreCase("") || strCrtCount.equalsIgnoreCase("null")) {
                    editor.putString("CartCnt", "0");
                    editor.commit();
                }

                int storedCnt = Integer.parseInt(sharedPreferences.getString("CartCnt", ""));
                int updatedCnt = storedCnt + 1;
                System.out.println("********updatedCnt*********" + storedCnt + "&&&&&&&&&" + updatedCnt);
                editor.putString("CartCnt", String.valueOf(updatedCnt));
                editor.commit();
                ////////////////
                counters[actPos] = temp;
                if (checkHasItemInCart()) {  // Checking first data is present in DB or not
                    String restoId = RealmController.getInstance().getRestaurantList().get(0).getRestaurantId();
                    String mainCategory = RealmController.getInstance().getRestaurantList().get(0).getMainCategory();

                    if (mainCategory.equalsIgnoreCase(ActivityHome.strFlowType) && restoId.equals(sharedPreferences.getString("clickedRestoId", ""))) {
                        viewHolder.tv_count.setText(String.valueOf(counters[actPos]));
                        ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                        modelRestaurantDetails.setQty(String.valueOf(counters[actPos]));
                        Config.addRemoveFromCart(modelRestaurantDetails);
                        setCartCount();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle(getContext().getResources().getString(R.string.beachBoxEnterprise));
                        alertDialog.setMessage(getContext().getResources().getString(R.string.selectOtherResto));
                        alertDialog.setPositiveButton("Yes,Please", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ////////////////////////////////////////////////////
                                editor.putString("CartCnt", "1");
                                editor.commit();
                                /////////////////////////////////////////////////////

                                if (sharedPreferences.getString("FlowType", "").toString().equalsIgnoreCase("pickup")) {
                                    System.out.println("*****FlowType**********" + sharedPreferences.getString("FlowType", ""));

                                    for (int i = 0; i < arrRestaurantList.size(); i++) {
                                        arrRestaurantList.get(i).setQty("0");
                                        notifyDataSetChanged();
                                    }

                                    /* for (int i = 0; i < arrRestaurantList.size(); i++) {
                                        RealmController.getInstance().clearAll();
                                        realm.beginTransaction();
                                        realm.clear(ModelRestaurantDetails.class);
                                        int counterval = 0;
                                        int temp = counterval;
                                        temp++;
                                        viewHolder.tv_count.setText(String.valueOf(temp));
                                        ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(i);
                                        modelRestaurantDetails.setQty(String.valueOf(temp));
                                        realm.commitTransaction();
                                        Config.addRemoveFromCart(modelRestaurantDetails);
                                    }*/
                                } else {
                                    System.out.println("*****FlowType**********" + sharedPreferences.getString("FlowType", ""));

                                }
                                RealmController.getInstance().clearAll();
                                realm.beginTransaction();
                                realm.clear(ModelRestaurantDetails.class);
                                int counterval = 0;
                                int temp = counterval;
                                temp++;
                                viewHolder.tv_count.setText(String.valueOf(temp));
                                ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                                modelRestaurantDetails.setQty(String.valueOf(temp));
                                realm.commitTransaction();
                                Config.addRemoveFromCart(modelRestaurantDetails);
                                setCartCount();

                            }
                        });
                        alertDialog.setNegativeButton("No,Thanks", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                } else {
                    viewHolder.tv_count.setText(String.valueOf(counters[actPos]));
                    ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                    modelRestaurantDetails.setQty(String.valueOf(counters[actPos]));
                    Config.addRemoveFromCart(modelRestaurantDetails);
                    setCartCount();
                }

            }
        });

        viewHolder.iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countValue = Integer.parseInt(viewHolder.tv_count.getText().toString());

                /////////////////
                if (countValue != 0) {
                    System.out.println("********removed updatedCnt*********" + sharedPreferences.getString("CartCnt", ""));
                    if (sharedPreferences.getString("CartCnt", "").toString().equals("")) {

                    } else {
                        if (!sharedPreferences.getString("CartCnt", "").toString().equals("0")) {

                            int storedCnt = Integer.parseInt(sharedPreferences.getString("CartCnt", ""));
                            int updatedCnt = storedCnt - 1;
                            System.out.println("********removed updatedCnt*********" + updatedCnt);
                            editor.putString("CartCnt", String.valueOf(updatedCnt));
                            editor.commit();
                        }
                    }
                }
                ////////////////

                if (countValue != 0) {
                    actPos = (Integer) v.getTag();
                    int temp = countValue;
                    temp--;
                    counters[actPos] = temp;
                    String restoId = "";
                    if (checkHasItemInCart()) {  // Checking firts data is present in DB or not
                        if (RealmController.getInstance().getRestaurantList().size() > 0) {
                            restoId = RealmController.getInstance().getRestaurantList().get(0).getRestaurantId();
                        }
                        System.out.println(">>> MY DB restoId :" + restoId);
                        System.out.println(">>> MY Preferences :" + sharedPreferences.getString("clickedRestoId", ""));
                        if (restoId.equals(sharedPreferences.getString("clickedRestoId", ""))) {
                            viewHolder.tv_count.setText(String.valueOf(counters[actPos]));
                            ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                            modelRestaurantDetails.setQty(String.valueOf(counters[actPos]));
                            Config.addRemoveFromCart(modelRestaurantDetails);
                            setCartCount();
                            RealmResults<ModelRestaurantDetails> results = null;

                         /*   if(temp == 0){
                                ModelRestaurantDetails modelRestaurantDetails1 = arrRestaurantList.get(position);
                                Realm realm = RealmController.getInstance().getRealm();
                                results = RealmController.getInstance().getRestaurantList();
                                realm.beginTransaction();
                                results.remove(modelRestaurantDetails1);
                                ArrayList<ModelRestaurantDetails> arrPositionCount = new ArrayList<>();

                                for (int i = 0; i < results.size(); i++) {
                                    Integer qty = Integer.parseInt(results.get(i).getQty());
                                    if(qty > 0){
                                        arrPositionCount.add(results.get(i));
                                    }
                                }
                                System.out.println(">>> after removal  Size is-- "+arrPositionCount.size());
                                if(arrPositionCount.size() <= 0){
                                    editor.remove("clickedRestoId");
                                    editor.commit();
                                    realm.clear(ModelRestaurantDetails.class);
                                }
                                System.out.println(">>> after clear  check data in DB- result-  "+checkHasItemInCart());
                            }*/

                            realm.commitTransaction();
                        } else {
                            Toast.makeText(context, getContext().getResources().getString(R.string.selectOtherResto), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        viewHolder.tv_count.setText(String.valueOf(counters[actPos]));
                        ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                        modelRestaurantDetails.setQty(String.valueOf(counters[actPos]));
                        Config.addRemoveFromCart(modelRestaurantDetails);
                        setCartCount();
                    }

                } else {


                    ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
                    modelRestaurantDetails.setQty("0");
                    Config.addRemoveFromCart(modelRestaurantDetails);
                    setCartCount();
                }
            }
        });
        return vi;
    }

    private void setCartCount() {
        List<ModelRestaurantDetails> cartCountSize = new ArrayList<ModelRestaurantDetails>();
        List<ModelRestaurantDetails> countSize = RealmController.getInstance().getRestaurantList();
        for (int i = 0; i < countSize.size(); i++) {
            if (Integer.parseInt(countSize.get(i).getQty()) > 0) {
                cartCountSize.add(countSize.get(i));
            }
        }

        if (cartCountSize.size() <= 0) {
            realm.beginTransaction();
            realm.clear(ModelRestaurantDetails.class);
            realm.commitTransaction();

        }
        editor.putString("cartCount", String.valueOf(cartCountSize.size()));
        editor.commit();
        ((UDashboardActivityNew) getContext()).updateCartCount(cartCountSize.size());

        List<ModelRestaurantDetails> arrCount = RealmController.getInstance().getRestaurantList();
        System.out.println(">>> After removela co" + arrCount.size());
        System.out.println(">>> Check In DB Data :" + checkHasItemInCart());

    }

    private boolean checkHasItemInCart() {
        boolean hasItem = RealmController.getInstance().hasItemInDB();
        return hasItem;
    }


}

