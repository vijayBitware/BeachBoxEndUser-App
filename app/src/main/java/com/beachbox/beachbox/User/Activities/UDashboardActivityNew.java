package com.beachbox.beachbox.User.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Adapter.AdapterCartList;
import com.beachbox.beachbox.User.Fragments.BaseFragment;
import com.beachbox.beachbox.User.Fragments.FragmentNotification;
import com.beachbox.beachbox.User.Fragments.FragmentOrder;
import com.beachbox.beachbox.User.Fragments.FragmentRestaurants;
import com.beachbox.beachbox.User.Fragments.FragmentCart;
import com.beachbox.beachbox.User.Fragments.FragmentAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DateFormat;
import java.util.Date;

/**
 * This is main class which contains menu
 *
 */


public class UDashboardActivityNew extends AppCompatActivity implements AdapterCartList.myInterface, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    LocationManager locationManager;
    ///////////////////////////////////////////////////////
    protected static final String TAG = "location-updates-sample";
    /**
     * 10秒間隔で位置情報を更新。実際には多少頻度が多くなるかもしれない。
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * 最速の更新間隔。この値より頻繁に更新されることはない。
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 10;

    //private ActivityMainBinding mBinding;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    double latitude, longitude;
    ///////////////////////////////////////////////////////
    TextView tabText1,tabText2,tabText3,tabText4,tv_tabNotiText;
    ImageView tabImage1,tabImage2,tabImage3,tabImage4,tabImageNoti;
    LinearLayout ll1,ll2,ll3,ll4,ll_noti;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button btnISDCartCount;
    Boolean isInternetPresent;
    ConnectionDetector cd;

    boolean isGPSEnabled = false;
    boolean isResumeCalled = false;
    String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        init();
        if(Config.notiFlag.equalsIgnoreCase("yes")){
            editor.putString("tabPosition","5");
            editor.commit();
            Config.notiFlag = "No";
        }
        if (isInternetPresent) {
            CheckPermission();
            afterGPSCheck();
        }else{
            Toast.makeText(UDashboardActivityNew.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }
    }

    private void afterGPSCheck() {
        isGPSEnabled = checkGPSEnabled();
        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            if(isGPSEnabled){
                Config.locationUpdate = false;
//                Log.e("Token#", FirebaseInstanceId.getInstance().getToken());
                String tabPosition = sharedPreferences.getString("tabPosition","");
                System.out.println(">> My Tab position : "+tabPosition);
                if(!tabPosition.isEmpty()){
                    if(tabPosition.equalsIgnoreCase("0") || tabPosition.equalsIgnoreCase("1")){
                        firstTabIsCalled();
                    }else if (tabPosition.equalsIgnoreCase("2")){
                        secondTabIsCalled();
                    }else if(tabPosition.equalsIgnoreCase("3")){
                        thirdTabIsCalled();
                    }else if(tabPosition.equalsIgnoreCase("4")){
                        fourthTabIsCalled();
                    }else if(tabPosition.equalsIgnoreCase("5")){
                        notificationTabisClicked();
                    }
                }else{
                    if (isInternetPresent) {
                        isGPSEnabled = checkGPSEnabled();
                        if(isGPSEnabled){
                            firstTabIsCalled();
                        }else{
                            showSettingsAlert();
                        }
                    }else{
                        Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                    }

                }

                //restaurents menu click
                ll1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cd = new ConnectionDetector(UDashboardActivityNew.this);
                        isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){
                                firstTabIsCalled();
                            }else{
                                //////////////////////////////////////////////////////
                                System.out.println("*********restaurent***********");
                                editor.putString("tabPosition","0");
                                editor.commit();
                                Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                                startActivity(i);
                                //////////////////////////////////////////////////////
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }


                    }
                });

                //cart menu click
                ll2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cd = new ConnectionDetector(UDashboardActivityNew.this);
                        isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){

                                btnISDCartCount.setVisibility(View.VISIBLE);
                                secondTabIsCalled();
                            }else{
                                //////////////////////////////////////////////////////
                                System.out.println("*********Cart***********");
                                editor.putString("tabPosition","2");
                                editor.commit();
                                Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                                startActivity(i);
                                //////////////////////////////////////////////////////
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }


                    }
                });

                //orders menu click
                ll3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cd = new ConnectionDetector(UDashboardActivityNew.this);
                        isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){
                                thirdTabIsCalled();
                            }else{
                                //////////////////////////////////////////////////////
                                System.out.println("*********orders***********");
                                editor.putString("tabPosition","3");
                                editor.commit();
                                Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                                startActivity(i);
                                //////////////////////////////////////////////////////
                                showSettingsAlert();

                            }
                        }else{
                            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }


                    }
                });

                //notification menu click
                ll_noti.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cd = new ConnectionDetector(UDashboardActivityNew.this);
                        isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){
                                notificationTabisClicked();
                            }else{
                                //////////////////////////////////////////////////////
                                System.out.println("*********notification***********");
                                editor.putString("tabPosition","5");
                                editor.commit();
                                Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                                startActivity(i);
                                //////////////////////////////////////////////////////
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }


                    }
                });


                //account menu click
                ll4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        isGPSEnabled = checkGPSEnabled();
                        cd = new ConnectionDetector(UDashboardActivityNew.this);
                        isInternetPresent = cd.isConnectingToInternet();
                        System.out.println("******is net*******"+isInternetPresent);
                        if (isInternetPresent) {
                            if(isGPSEnabled){
                                System.out.println("*********net account***********");
                                fourthTabIsCalled();
                            }else{
                                //////////////////////////////////////////////////////
                                System.out.println("*********account***********");
                                editor.putString("tabPosition","4");
                                editor.commit();
                                Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                                startActivity(i);
                                //////////////////////////////////////////////////////
                                showSettingsAlert();
                            }

                        }else{
                            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }else{
                Config.locationUpdate = true;
                showSettingsAlert();
            }
        }else{
            Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

        }

    }


    //when notification tab clicked
    private void notificationTabisClicked() {
        replaceFragment(new FragmentNotification());

        tabImage1.setImageResource(R.drawable.resto_black);
        tabText1.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage2.setImageResource(R.drawable.cart_black);
        tabText2.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage3.setImageResource(R.drawable.order_black);
        tabText3.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage4.setImageResource(R.drawable.account_black);
        tabText4.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImageNoti.setImageResource(R.drawable.noti_green);
        tv_tabNotiText.setTextColor(getResources().getColor(R.color.colorAccent));

    }

    //when account tab is clicked
    private void fourthTabIsCalled() {
        btnISDCartCount.setVisibility(View.VISIBLE);
        replaceFragment(new FragmentAccount());
        tabImage1.setImageResource(R.drawable.resto_black);
        tabText1.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage2.setImageResource(R.drawable.cart_black);
        tabText2.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage3.setImageResource(R.drawable.order_black);
        tabText3.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage4.setImageResource(R.drawable.account_green);
        tabText4.setTextColor(getResources().getColor(R.color.colorAccent));

        tabImageNoti.setImageResource(R.drawable.noti_black);
        tv_tabNotiText.setTextColor(getResources().getColor(R.color.back_text_color));
    }

    //when orders tab is clicked
    private void thirdTabIsCalled() {
        btnISDCartCount.setVisibility(View.VISIBLE);
        replaceFragment(new FragmentOrder());
        tabImage1.setImageResource(R.drawable.resto_black);
        tabText1.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage2.setImageResource(R.drawable.cart_black);
        tabText2.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage3.setImageResource(R.drawable.order_green);
        tabText3.setTextColor(getResources().getColor(R.color.colorAccent));

        tabImage4.setImageResource(R.drawable.account_black);
        tabText4.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImageNoti.setImageResource(R.drawable.noti_black);
        tv_tabNotiText.setTextColor(getResources().getColor(R.color.back_text_color));
    }

    //when cart tab is clicked
    private void secondTabIsCalled() {
        replaceFragment(new FragmentCart());
        tabImage1.setImageResource(R.drawable.resto_black);
        tabText1.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage2.setImageResource(R.drawable.cart_green);
        tabText2.setTextColor(getResources().getColor(R.color.colorAccent));

        tabImage3.setImageResource(R.drawable.order_black);
        tabText3.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage4.setImageResource(R.drawable.account_black);
        tabText4.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImageNoti.setImageResource(R.drawable.noti_black);
        tv_tabNotiText.setTextColor(getResources().getColor(R.color.back_text_color));
    }

    //when restaurent tab is clicked
    private void firstTabIsCalled() {
        replaceFragment(new FragmentRestaurants());
        tabImage1.setImageResource(R.drawable.restaurant_green);
        tabText1.setTextColor(getResources().getColor(R.color.colorAccent));

        tabImage2.setImageResource(R.drawable.cart_black);
        tabText2.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage3.setImageResource(R.drawable.order_black);
        tabText3.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImage4.setImageResource(R.drawable.account_black);
        tabText4.setTextColor(getResources().getColor(R.color.back_text_color));

        tabImageNoti.setImageResource(R.drawable.noti_black);
        tv_tabNotiText.setTextColor(getResources().getColor(R.color.back_text_color));
    }

    private void init() {
//        frame = (FrameLayout) findViewById(R.id.frame);
        tabText1 = (TextView) findViewById(R.id.tv_tabText1);
        tabText2 = (TextView) findViewById(R.id.tv_tabText2);
        tabText3 = (TextView) findViewById(R.id.tv_tabText3);
        tabText4 = (TextView) findViewById(R.id.tv_tabText4);
        tv_tabNotiText = (TextView) findViewById(R.id.tv_tabNotiText);

        tabImage1 = (ImageView) findViewById(R.id.iv_account);
        tabImage2 = (ImageView) findViewById(R.id.iv_cart);
        tabImage3 = (ImageView) findViewById(R.id.iv_account2);
        tabImage4 = (ImageView) findViewById(R.id.iv_account3);
        tabImageNoti  = (ImageView) findViewById(R.id.tabImageNoti);

        ll1 = (LinearLayout) findViewById(R.id.ll_one);
        ll2 = (LinearLayout) findViewById(R.id.ll_two);
        ll3 = (LinearLayout) findViewById(R.id.ll_three);
        ll4 = (LinearLayout) findViewById(R.id.ll_four);
        ll_noti = (LinearLayout) findViewById(R.id.ll_noti);

        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        btnISDCartCount= (Button) findViewById(R.id.btnCartCount);

        String strCrtCount = sharedPreferences.getString("CartCnt","");//sharedPreferences.getString("cartCount","");

        if(!strCrtCount.isEmpty()){
            btnISDCartCount.setText(strCrtCount);
        }else{
            btnISDCartCount.setText("0");
        }


        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";


        buildGoogleApiClient();
        if (!isPlayServicesAvailable(this)) return;
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
        } else {
            return;
        }

        if (Build.VERSION.SDK_INT < 23) {
            // setButtonsEnabledState();
            startLocationUpdates();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // setButtonsEnabledState();
            startLocationUpdates();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //showRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }


        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(UDashboardActivityNew.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){
                        firstTabIsCalled();
                    }else{
                        //////////////////////////////////////////////////////
                        System.out.println("*********restaurent***********");
                        editor.putString("tabPosition","0");
                        editor.commit();
                        Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        startActivity(i);
                        //////////////////////////////////////////////////////
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }


            }
        });

        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(UDashboardActivityNew.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){

                        btnISDCartCount.setVisibility(View.VISIBLE);
                        secondTabIsCalled();
                    }else{
                        //////////////////////////////////////////////////////
                        System.out.println("*********Cart***********");
                        editor.putString("tabPosition","2");
                        editor.commit();
                        Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        startActivity(i);
                        //////////////////////////////////////////////////////
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }


            }
        });

        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(UDashboardActivityNew.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){
                        thirdTabIsCalled();
                    }else{
                        //////////////////////////////////////////////////////
                        System.out.println("*********orders***********");
                        editor.putString("tabPosition","3");
                        editor.commit();
                        Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        startActivity(i);
                        //////////////////////////////////////////////////////
                        showSettingsAlert();

                    }
                }else{
                    Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }


            }
        });
        ll_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(UDashboardActivityNew.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){
                        notificationTabisClicked();
                    }else{
                        //////////////////////////////////////////////////////
                        System.out.println("*********notification***********");
                        editor.putString("tabPosition","5");
                        editor.commit();
                        Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        startActivity(i);
                        //////////////////////////////////////////////////////
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }


            }
        });


        ll4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isGPSEnabled = checkGPSEnabled();
                cd = new ConnectionDetector(UDashboardActivityNew.this);
                isInternetPresent = cd.isConnectingToInternet();
                System.out.println("******is net*******"+isInternetPresent);
                if (isInternetPresent) {
                    if(isGPSEnabled){
                        System.out.println("*********net account***********");
                        fourthTabIsCalled();
                    }else{
                        //////////////////////////////////////////////////////
                        System.out.println("*********account***********");
                        editor.putString("tabPosition","4");
                        editor.commit();
                        Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        startActivity(i);
                        //////////////////////////////////////////////////////
                        showSettingsAlert();
                    }

                }else{
                    Toast.makeText(UDashboardActivityNew.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }

            }
        });
    }



    public void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction =
                    fragmentManager.beginTransaction().addToBackStack(fragment.getClass().toString());
            transaction.replace(R.id.frame, fragment);
            transaction.commit();
        }
    }

    public void popAll(){
        int count = getFragmentManager().getBackStackEntryCount();
        System.out.println(">>>> -- "+count);
        for (int i = 0; i < count; i++) {
            getFragmentManager().popBackStack();
        }
    }

    public void showSettingsAlert() {
       /* android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(UDashboardActivityNew.this);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isResumeCalled = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();*/
        buildGoogleApiClient();
    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) UDashboardActivityNew.this.getSystemService(Context.LOCATION_SERVICE);
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

    @Override
    public void onBackPressed() {
       showExitDialog();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit!");
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                       /* popAll();;
                        finish();*/
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory( Intent.CATEGORY_HOME );
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void getCartTotalPrice(String mPrice) {

    }

    @Override
    public void updateCartCount(int cartCount) {
       btnISDCartCount.setText(""+sharedPreferences.getString("CartCnt", ""));
    }

    //check runtime permissions
    private void CheckPermission() {
        if(ActivityCompat.checkSelfPermission(UDashboardActivityNew.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(UDashboardActivityNew.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(UDashboardActivityNew.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(UDashboardActivityNew.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[3]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                AlertDialog.Builder builder = new AlertDialog.Builder(UDashboardActivityNew.this);
                builder.setTitle(UDashboardActivityNew.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(UDashboardActivityNew.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(UDashboardActivityNew.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(UDashboardActivityNew.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(UDashboardActivityNew.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(UDashboardActivityNew.this);
                builder.setTitle(UDashboardActivityNew.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(UDashboardActivityNew.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(UDashboardActivityNew.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Location,Storage permission", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(UDashboardActivityNew.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(UDashboardActivityNew.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        }else{
            proceedAfterPermission();
            System.out.println("YOU HAVE PERMISSION PROCESS ");
        }
    }

    private void proceedAfterPermission() {
        afterGPSCheck();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if(allgranted){
                proceedAfterPermission();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(UDashboardActivityNew.this,permissionsRequired[3]))
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(UDashboardActivityNew.this);
                builder.setTitle(UDashboardActivityNew.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(UDashboardActivityNew.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(UDashboardActivityNew.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(UDashboardActivityNew.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(UDashboardActivityNew.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(UDashboardActivityNew.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                if (isResumeCalled) {
                    afterGPSCheck();
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        System.out.println("****************create*******");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates");

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        // 現在位置の取得の前に位置情報の設定が有効になっているか確認する
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // 設定が有効になっているので現在位置を取得する
                        System.out.println("*************success************");
                        if(Config.locationUpdate)
                        {
                            Config.locationUpdate = false;
                            Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                            startActivity(i);
                        }
                        //Intent i = new Intent(UDashboardActivityNew.this, UDashboardActivityNew.class);
                        //startActivity(i);
                       /* if (ContextCompat.checkSelfPermission(UDashboardActivityNew.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UDashboardActivityNew.this);

                        }*/
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // 設定が有効になっていないのでダイアログを表示する
                        try {
                            status.startResolutionForResult(UDashboardActivityNew.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    private void updateUI() {
        if (mCurrentLocation == null) return;


    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");

    }



    public static boolean isPlayServicesAvailable(Context context) {
        // Google Play Service APKが有効かどうかチェックする
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPlayServicesAvailable(this);

        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        /*if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }*/
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        //mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    public double getLatitude()
    {
        latitude = mCurrentLocation.getLatitude();

        return latitude;
        //longitude = mCurrentLocation.getLongitude();

    }


    public double getLongitude()
    {
        //latitude = mCurrentLocation.getLatitude();


        longitude = mCurrentLocation.getLongitude();
        return longitude;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        //Toast.makeText(this, "location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }
}
