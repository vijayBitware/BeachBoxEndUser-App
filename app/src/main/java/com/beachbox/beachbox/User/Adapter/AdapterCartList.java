package com.beachbox.beachbox.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentCart;
import com.beachbox.beachbox.User.Model.ModelRestaurantDetails;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 *  This class used for showing list of added item in cart.
 * Created by bitware on 24/3/17.
 */

public class AdapterCartList extends ArrayAdapter<ModelRestaurantDetails> {

    ArrayList<ModelRestaurantDetails> arrRestaurantList ;
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int actPosition;
    int mPrice = 0;
    int FifteenPercent = 0 ,TwentyPercentage = 0;
    myInterface anInterface;
    int cartTotalPrice = 0;
    FragmentCart mFragment;

    public AdapterCartList(Context context, int resource, ArrayList<ModelRestaurantDetails> arrRestaurantList, FragmentCart fragmentCart, myInterface myInterface) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;
        this.anInterface = myInterface;
        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mFragment = fragmentCart;
    }

    public static class ViewHolder {
        TextView tv_menuName,tv_menuDes, tv_menuPricing,tv_count,tvRemoveFromCart;
        ImageView iv_add,iv_remove;

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
        actPosition = getItemViewType(position);

        if (convertView == null) {

            vi = inflater.inflate(R.layout.row_cart, null);
            viewHolder = new ViewHolder();

            viewHolder.tv_menuName = (TextView) vi.findViewById(R.id.tv_menuName);
            viewHolder.tv_menuDes = (TextView) vi.findViewById(R.id.tv_menuDes);
            viewHolder.tv_menuPricing = (TextView) vi.findViewById(R.id.tv_menuPrice);
            viewHolder.iv_add = (ImageView) vi.findViewById(R.id.iv_add);
            viewHolder.iv_remove = (ImageView) vi.findViewById(R.id.iv_remove);
            viewHolder.tv_count = (TextView) vi.findViewById(R.id.tv_count);
            viewHolder.tvRemoveFromCart = (TextView) vi.findViewById(R.id.tvRemoveFromCart);

            vi.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) vi.getTag();
        }

        viewHolder.iv_add.setTag(position);
        viewHolder.iv_remove.setTag(position);

        boolean hasItems = RealmController.getInstance().hasItemInDB();
        if(hasItems){
            viewHolder.tv_menuName.setText(arrRestaurantList.get(position).getMenu_name());
            String desc = arrRestaurantList.get(position).getMenu_description();
            if (!desc.equals("null")) {
                viewHolder.tv_menuDes.setText(desc);
            } else {
                viewHolder.tv_menuDes.setText("Description not available");
            }
            viewHolder.tv_menuPricing.setText("$" + arrRestaurantList.get(position).getMenu_price());
            viewHolder.tv_count.setText(arrRestaurantList.get(position).getQty());
        }

        viewHolder.iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertItemInDB(position, viewHolder);
            }
        });

        viewHolder.iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /////////////////
                System.out.println("********removed updatedCnt*********"+ sharedPreferences.getString("CartCnt", ""));
                if(sharedPreferences.getString("CartCnt", "").toString().equals(""))
                {

                }else {
                    if (!sharedPreferences.getString("CartCnt", "").toString().equals("0")) {

                        int storedCnt = Integer.parseInt(sharedPreferences.getString("CartCnt", ""));
                        int updatedCnt = storedCnt - 1;
                        System.out.println("********removed updatedCnt*********" + updatedCnt);
                        editor.putString("CartCnt", String.valueOf(updatedCnt));
                        editor.commit();
                    }
                }
                ////////////////

                /***************************/
                if (sharedPreferences.getString("CartCnt", "").toString().equals("0")){
                    editor.remove("cartComment");
                    editor.apply();
                    Config.tip = "";
                    Config.tipStatus = "no tip";
                }

                setCartCount();
                ModelRestaurantDetails modelRestaurantDetails = null;
                Realm realm = RealmController.getInstance().getRealm();
                realm.beginTransaction();
                int temp = Integer.parseInt(arrRestaurantList.get(position).getQty()) ;
                if (temp !=0){
                    System.out.println("************if*********");
                    temp--;
                    viewHolder.tv_count.setText(String.valueOf(temp));
                    arrRestaurantList.get(position).setQty(String.valueOf(temp));
                    modelRestaurantDetails = arrRestaurantList.get(position);
                    modelRestaurantDetails.setQty(String.valueOf(temp));
                    List<ModelRestaurantDetails> cartList = RealmController.getInstance().getRestaurantList();
                    double finalAmt = 0;
                    for (int i = 0; i < cartList.size(); i++) {
                        int qty = Integer.parseInt(cartList.get(i).getQty());
                        double mPrice = Double.parseDouble(cartList.get(i).getMenu_price());
                       // int price = Integer.parseInt(cartList.get(i).getMenu_price());
                        finalAmt = finalAmt + (qty * mPrice) ;
                    }
                    System.out.println(" >>> FInal Cart Amt minus>>  "+finalAmt);
                    anInterface.getCartTotalPrice(getFormatedval(finalAmt));
                    if(temp ==0){
                        remove(arrRestaurantList.get(position));
                        notifyDataSetChanged();
                        RealmResults<ModelRestaurantDetails> results = RealmController.getInstance().getRestaurantList();
                        results.remove(position);
                        setCartCount();

                    }
                }else {
                    System.out.println("************else*********");
                    modelRestaurantDetails = arrRestaurantList.get(position);
                    modelRestaurantDetails.setQty(String.valueOf(temp));

                    remove(arrRestaurantList.get(position));
                    notifyDataSetChanged();

                    RealmResults<ModelRestaurantDetails> results = RealmController.getInstance().getRestaurantList();
                    results.remove(position);
                    setCartCount();
                    //((UDashboardActivityNew) context).updatedCartCount(arrRestaurantList.size());
                }

                realm.commitTransaction();
                Config.addRemoveFromCart(modelRestaurantDetails);


            }
        });

        viewHolder.tvRemoveFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///////////////////////////////////////////////////
                System.out.println("********removed updatedCnt*********"+ sharedPreferences.getString("CartCnt", "")+"*****qty*****"+arrRestaurantList.get(position).getQty());
                if(sharedPreferences.getString("CartCnt", "").toString().equals(""))
                {

                }else {
                    if (!sharedPreferences.getString("CartCnt", "").toString().equals("0")) {

                        int storedCnt = Integer.parseInt(sharedPreferences.getString("CartCnt", ""));
                        int updatedCnt = storedCnt - Integer.parseInt(arrRestaurantList.get(position).getQty());
                        System.out.println("********removed updatedCnt*********" + updatedCnt);
                        editor.putString("CartCnt", String.valueOf(updatedCnt));
                        editor.commit();
                    }
                }

                /**************************************/
                if (sharedPreferences.getString("CartCnt", "").toString().equals("0")){
                        Config.tip = "";
                    Config.tipStatus = "no tip";
                    editor.remove("cartComment");
                    editor.apply();
                }
                //////////////////////////////////////////////////
                remove(arrRestaurantList.get(position));
                notifyDataSetChanged();

               // anInterface.updateCartCount(arrRestaurantList.size());
                Realm realm = RealmController.getInstance().getRealm();
                RealmResults<ModelRestaurantDetails> results = RealmController.getInstance().getRestaurantList();
                realm.beginTransaction();
                results.remove(position);
                setCartCount();

                List<ModelRestaurantDetails> cartList = RealmController.getInstance().getRestaurantList();
                double finalAmt = 0;
                for (int i = 0; i < cartList.size(); i++) {

                    int qty = Integer.parseInt(cartList.get(i).getQty());
                    double mPrice = Double.parseDouble(cartList.get(i).getMenu_price());
                    // int price = Integer.parseInt(cartList.get(i).getMenu_price());
                    finalAmt = finalAmt + (qty * mPrice) ;
                }
                System.out.println(" >>> FInal Cart Amt >>  "+getFormatedval(finalAmt));
                anInterface.getCartTotalPrice(getFormatedval(finalAmt));

                realm.commitTransaction();

            }
        });


        return vi;
    }


    private void insertItemInDB(int position, ViewHolder viewHolder) {
        Realm realm = RealmController.getInstance().getRealm();
        realm.beginTransaction();
        int temp = Integer.parseInt(arrRestaurantList.get(position).getQty()) ;
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
        //counters[actPosition] = temp;
        viewHolder.tv_count.setText(String.valueOf(temp));
        arrRestaurantList.get(position).setQty(String.valueOf(temp));

        ModelRestaurantDetails modelRestaurantDetails = arrRestaurantList.get(position);
        modelRestaurantDetails.setQty(String.valueOf(temp));

        List<ModelRestaurantDetails> cartList = RealmController.getInstance().getRestaurantList();
        double finalAmt = 0;
        for (int i = 0; i < cartList.size(); i++) {

            int qty = Integer.parseInt(cartList.get(i).getQty());
            double price = Double.parseDouble(cartList.get(i).getMenu_price());

            finalAmt = finalAmt + (qty * price) ;
        }
        System.out.println(" >>> FInal Cart Amt >>  "+finalAmt);
        anInterface.getCartTotalPrice(getFormatedval(finalAmt));

        realm.commitTransaction();
        Config.addRemoveFromCart(modelRestaurantDetails);
        setCartCount();
    }

    public interface myInterface {

         public void getCartTotalPrice(String mPrice);
         public void updateCartCount(int cartCount);
    }

    private  boolean checkHasItemInCart() {
        boolean hasItem = RealmController.getInstance().hasItemInDB();

        return hasItem ;
    }


    private void setCartCount() {
        List<ModelRestaurantDetails> cartCountSize = new ArrayList<ModelRestaurantDetails>();
        List<ModelRestaurantDetails> countSize = RealmController.getInstance().getRestaurantList();

        for (int i = 0; i < countSize.size(); i++) {
            if (Integer.parseInt(countSize.get(i).getQty()) > 0) {
                cartCountSize.add(countSize.get(i));
            }
        }
        if(cartCountSize.size() <= 0){
            editor.remove("cartCount");
            editor.remove("cartComment");
            editor.apply();
            Realm realm = RealmController.getInstance().getRealm();
            realm.clear(ModelRestaurantDetails.class);
            mFragment.hideRestoName();
            mFragment.updateView();

        }
        ((UDashboardActivityNew) getContext()).updateCartCount(cartCountSize.size());
    }

    private String getFormatedval(double currentLat) {
        String strMyVal = "";
        String  last2Chars;
        if(String.valueOf(currentLat).contains(".")){
            String[] arr=String.valueOf(currentLat).split("\\.");
            long[] intArr=new long[2];
            intArr[0]=Long.parseLong(arr[0]); // 1
            intArr[1]=Long.parseLong(arr[1]); //
            String mainDigits = String.valueOf(intArr[0]);
            String strLenght = String.valueOf(intArr[1]);
            if(strLenght.length() > 2){
                last2Chars = strLenght.substring(0,2);
            }else{
                last2Chars =  strLenght;
            }
            String strFinal = mainDigits+"."+last2Chars;
            System.out.println(">>>> adapter Final output is  strFinal --- :"+strFinal);
            strMyVal = strFinal;
            System.out.println("adapter >>>> Double return val is --- :"+strMyVal);
        }else {
            strMyVal = String.valueOf(currentLat);
        }
        return strMyVal;
    }
}

