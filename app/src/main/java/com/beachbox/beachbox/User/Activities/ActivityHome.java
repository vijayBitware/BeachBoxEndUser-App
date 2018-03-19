package com.beachbox.beachbox.User.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.GPSTracker;
import com.beachbox.beachbox.Database.RealmController;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Model.responseUpdateVersion.UpdateVersionResponse;
import com.beachbox.beachbox.User.Model.responseUpdateVersion.Version;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
/**
 * This class for making choice for food delivery and pickup option.
 */

public class ActivityHome extends AppCompatActivity{

    TextView tv_signIn, tv_createAccount, tv_delivery, tv_pickup;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static String strFlowType = "";
    boolean isGPSEnabled = false;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    String locationArray = "";
    boolean isResumeCalled = false;
    int mVersionCode = 0;
    LocationManager locManager;
    LocationListener locListener;
    Location CurrentLocation;
    LocationManager locationManager;
    boolean locationUpdated = false;
    boolean flagupdate = false;

    String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

           };

    private boolean sentToSettings = true;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    LinearLayout llAcountTitle;
    boolean isUpdateDialogShow = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chooser);
        locationManager = (LocationManager) ActivityHome.this.getSystemService(Context.LOCATION_SERVICE);
        init();
            if (isInternetPresent) {
                callVersionAPI();
            }else{
                Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
            }

    }

    //check the current uploaded version
    private void callVersionAPI() {
        new UpdateAndroidVersion().execute("{\"device_type\":\"" + Config.DEVICE_TYPE + "\",\"version\":\"" + String.valueOf(mVersionCode) + "\"}");
    }

    //update app dialog when new app available on playstore
    private void updateAppDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityHome.this);
        builder.setTitle(ActivityHome.this.getResources().getString(R.string.uodateTitle));
        builder.setMessage(ActivityHome.this.getResources().getString(R.string.updateMsg));
        builder.setCancelable(false);
        builder.setPositiveButton(ActivityHome.this.getResources().getString(R.string.updateOk), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.cancel();
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    isUpdateDialogShow = true;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        builder.setNegativeButton(ActivityHome.this.getResources().getString(R.string.updateCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);

            }
        });
        builder.show();

    }

    //checking runtime permission
    private void CheckPermission() {
        if(ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
        || (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
        || (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
        || (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED )
        || (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[4]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this,permissionsRequired[0])
            ||ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this,permissionsRequired[1])
            || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this,permissionsRequired[2])
            || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this,permissionsRequired[3])
            || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this,permissionsRequired[4]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityHome.this);
                builder.setTitle(ActivityHome.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityHome.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityHome.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivityHome.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityHome.this);
                builder.setTitle(ActivityHome.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityHome.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityHome.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    flagupdate = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(getBaseContext(), "Go to Permissions to Grant Location,Storage permission", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton(ActivityHome.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }else {
                //just request the permission
                ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
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
       // AfterGPSOn();
        turnOnGps();
    }

    //turn on gps setting after permission
    private void turnOnGps() {
        //showLocationDialog();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showLocationDialog();
        }
        else{

            AfterGPSOn();

        }
    }


    //shows location dialog
    private void showLocationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityHome.this);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              //  isResumeCalled = true;
                flagupdate = true;
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
        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[3])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[4])) {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityHome.this);
                builder.setTitle(ActivityHome.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityHome.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityHome.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityHome.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivityHome.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    //initialization
    private void init() {

        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        Realm realm = RealmController.with(ActivityHome.this).getRealm();
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_signIn = (TextView) findViewById(R.id.tv_signIn);
        tv_createAccount = (TextView) findViewById(R.id.tv_createAnAccount);
        tv_delivery = (TextView) findViewById(R.id.tvDelivery);
        tv_pickup = (TextView) findViewById(R.id.tvPickUp);
        llAcountTitle = (LinearLayout) findViewById(R.id.llAcountTitle);

        if(sharedPreferences.getString("isUserLoggedIn","").equalsIgnoreCase("Yes")){
            llAcountTitle.setVisibility(View.INVISIBLE);
        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionCode = packageInfo.versionCode;
            System.out.println(">>> mVersionCode "+mVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        tv_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (isGPSEnabled) {
                cd = new ConnectionDetector(ActivityHome.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showLocationDialog();
                }
                else{

                    if (isInternetPresent) {
                        editor.putString("tabPosition", "1");
                        strFlowType = "delivery";
                        editor.putString("FlowType",strFlowType);
                        editor.commit();
                        Intent intent = new Intent(ActivityHome.this, UDashboardActivityNew.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }

                }


               /* } else {
                    showSettingsAlert();
                }*/
            }
        });
        tv_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (isGPSEnabled) {
                cd = new ConnectionDetector(ActivityHome.this);
                isInternetPresent = cd.isConnectingToInternet();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showLocationDialog();
                }
                else {
                    if (isInternetPresent) {
                        strFlowType = "pickup";
                        editor.putString("FlowType", strFlowType);
                        editor.putString("tabPosition", "1");
                        editor.commit();
                        Intent intent = new Intent(ActivityHome.this, UDashboardActivityNew.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }
                }


               /* } else {
                    showSettingsAlert();
                }*/

            }
        });

        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if (isGPSEnabled) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showLocationDialog();
                }
                else {
                    if (isInternetPresent) {
                        editor.putString("tabPosition", "0");
                        editor.commit();
                        Intent intent = new Intent(ActivityHome.this, ActivitySignIn.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }

                }
                /*} else {
                    showSettingsAlert();
                }*/

            }
        });

        tv_createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isGPSEnabled) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    showLocationDialog();
                }
                else {
                    if (isInternetPresent) {
                        Intent intent = new Intent(ActivityHome.this, ActivitySignUp.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }

                }
                /*} else {
                    showSettingsAlert();
                }*/

            }
        });

        GPSTracker gpsTracker = new GPSTracker(ActivityHome.this);
        gpsTracker.getLocation();
        if (sharedPreferences.getString("isUserLoggedIn", "").equalsIgnoreCase("Yes")) {
            //callUpdateLocationAPI();
        }
    }



    private void AfterGPSOn() {
        //isGPSEnabled = checkGPSEnabled();
        isInternetPresent = cd.isConnectingToInternet();
        tv_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if (isGPSEnabled) {
                if (isInternetPresent) {
                    editor.putString("tabPosition", "1");
                    strFlowType = "delivery";
                    editor.putString("FlowType",strFlowType);
                    editor.commit();
                    Intent intent = new Intent(ActivityHome.this, UDashboardActivityNew.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }

               /* } else {
                    showSettingsAlert();
                }*/
            }
        });
        tv_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if (isGPSEnabled) {
                if (isInternetPresent) {
                    strFlowType = "pickup";
                    editor.putString("FlowType",strFlowType);
                    editor.putString("tabPosition", "1");
                    editor.commit();
                    Intent intent = new Intent(ActivityHome.this, UDashboardActivityNew.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }


               /* } else {
                    showSettingsAlert();
                }*/

            }
        });

        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // if (isGPSEnabled) {
                if (isInternetPresent) {
                    editor.putString("tabPosition","0");
                    editor.commit();
                    Intent intent = new Intent(ActivityHome.this, ActivitySignIn.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }


                /*} else {
                    showSettingsAlert();
                }*/

            }
        });

        tv_createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isGPSEnabled) {
                if (isInternetPresent) {
                    Intent intent = new Intent(ActivityHome.this, ActivitySignUp.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityHome.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }


                /*} else {
                    showSettingsAlert();
                }*/

            }
        });

        //if (isGPSEnabled) {

        //}
    }

    @Override
    public void onBackPressed() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Exit!");
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

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
        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }



    class UpdateAndroidVersion extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityHome.this);
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> update Version Api  :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"user/getVersion")
                    .post(body)
                    .build();
            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>> Update version  :" + s);
            p.dismiss();
            if (s != null) {
                try {
                        JSONObject jsonObject = new JSONObject(s);
                        String isSuccess = jsonObject.getString("is_success");
                        if (isSuccess.equalsIgnoreCase("true")) {
                            flagupdate = false;
                            CheckPermission();
                        } else {
                            updateAppDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            } else {
                Toast.makeText(ActivityHome.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("*******************RESULT*************"+requestCode+"**"+resultCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("****************resume1************" + sentToSettings);


        /////////////////////////////////////////////////////
        System.out.println("****************resume1************" + sentToSettings + "**" + ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[0])
                + "***" + ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[4]));

        if (flagupdate) {
            System.out.println("******************flagupdate");
            if (sentToSettings) {
                if (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[0]) == 0 && ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[1]) == 0
                        && ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[2]) == 0 && ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[3]) == 0
                        && ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[4]) == 0) {
                    //AfterGPSOn();
                /*if (isResumeCalled) {
                    AfterGPSOn();
                    System.out.println("****************resume22************");
                    //Intent i = new Intent(ActivityHome.this, ActivityHome.class);
                    //startActivity(i);
                }*/if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                            .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        System.out.println("*********resume gps*********");
                        turnOnGps();
                    } else {
                        AfterGPSOn();

                    }

                } else {
                    //just request the permission
                    ActivityCompat.requestPermissions(ActivityHome.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                }
            } else {
                if (isUpdateDialogShow) {
                    startActivity(getIntent());
                    Intent i = new Intent(ActivityHome.this, ActivityHome.class);
                    startActivity(i);
                }
            }
        }
    }
}