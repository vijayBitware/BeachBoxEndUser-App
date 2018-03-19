package com.beachbox.beachbox.User.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.GPSTracker;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Model.loginResponse.LoginResponse;
import com.beachbox.beachbox.User.Model.responseplaceorder.ResponsePlaceOrder;
import com.beachbox.beachbox.User.Model.signupResponse.SignUpResponse;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 21/3/17.
 */

public class ActivitySignIn extends Activity  implements APIRequest.ResponseHandler{

    TextView tv_back,tv_signIn;
    EditText edtEmail,edtPassword;
    String response_msg="",email="",password="";
    ConnectionDetector cd;
    Boolean isInternetPresent;
    LoginButton facebookLogin;
    CallbackManager callbackManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    LinearLayout fb;
    String response_message_eng ="",strEmailFromUser = "",fbEmail = "",fbId = "",
            firstName="",lastName="",fbName = "";
    AccessTokenTracker accessTokenTracker;
    TextView newusersignup,login,tvOkEmailDialog,tvForgotPass,tv_createAnAccount;
    Dialog dialogService1;
    public int screenWidth, screenHeight;
    String phoneNumber = "";
    String APICall = "";
    String hasEmail = "";
    public int textPos=0,txtFBPos = 0;
    String fbImageURL = "";
    String strFCMTokan = "";

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
    boolean isGPSEnabled = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        init();

        if(isInternetPresent){
            CheckPermission();
        }else {
            Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }
        //afterGettingPermission();
    }

    private void init() {
        cd = new ConnectionDetector(this);
        isInternetPresent=cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor =sharedPreferences.edit();

        tv_back= (TextView) findViewById(R.id.tv_back);
        tv_signIn= (TextView) findViewById(R.id.tv_signIn);
        tvForgotPass = (TextView) findViewById(R.id.tvForgotPass);
        edtEmail= (EditText) findViewById(R.id.edtEmail);
        edtPassword= (EditText) findViewById(R.id.edt_password);
        facebookLogin = (LoginButton) findViewById(R.id.login_button);
        fb = (LinearLayout) findViewById(R.id.fb);
        tv_createAnAccount = (TextView) findViewById(R.id.tv_createAnAccount);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        edtPassword.setTransformationMethod(new PasswordTransformationMethod());
        strFCMTokan = FirebaseInstanceId.getInstance().getToken();
        System.out.println(">> On Login FCM tokan id :"+ strFCMTokan );

    }

    private void afterGettingPermission() {
        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetPresent){
                    isGPSEnabled = checkGPSEnabled();
                    if (isGPSEnabled) {
/*edtEmail.setText("testandroid@gmail.com");
                edtPassword.setText("test123");*/
                        email = edtEmail.getText().toString().trim();
                        password = edtPassword.getText().toString().trim();

                        if (isInternetPresent) {
                            if (!email.isEmpty()) {
                                if (email.matches(Config.EMAIL_REGEX)) {
                                    if (!password.isEmpty()) {
                                        APICall = "SingIn";
                                        singInRequest();
                                    }else{
                                        edtPassword.requestFocus();
                                        edtPassword.setError("Please enter password.");
                                    }
                                }else{
                                    edtEmail.requestFocus();
                                    edtEmail.setError("Please enter valid email.");
                                }
                            }else{
                                edtEmail.requestFocus();
                                edtEmail.setError("Please enter email.");
                            }
                        } else {
                            Toast.makeText(ActivitySignIn.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }

            }

        });


        tv_createAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignIn.this, ActivitySignUp.class);
                startActivity(intent);
                finish();
            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySignIn.this, ActivityForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getString("tabPosition","").equalsIgnoreCase("0")){
                    Intent intent = new Intent(ActivitySignIn.this, ActivityHome.class);
                    startActivity(intent);
                    finish();

                }else{
                    Intent intent = new Intent(ActivitySignIn.this, UDashboardActivityNew.class);
                    startActivity(intent);
                    finish();

                }
                 }
        });

        fbInItData();
    }

    private void singInRequest() {
        JSONObject loginObject = new JSONObject();
        try {
            loginObject.put("email",email);
            loginObject.put("password",password);
            loginObject.put("accesstoken", Config.accessToken);
            loginObject.put("device_type", Config.DEVICE_TYPE);
            loginObject.put("device_push_token", strFCMTokan);

            String singInURL = Config.BASE_URL+"user/login";
            System.out.println(">>> Login request :"+loginObject);
            new APIRequest(ActivitySignIn.this, loginObject, singInURL, this, Config.API_SIGNIN, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void fbInItData() {
        facebookLogin.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
        callbackManager = CallbackManager.Factory.create();

        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                System.out.println(">>>> loginResult :"+loginResult);
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject fbObject, GraphResponse response) {
                                LoginManager.getInstance().logOut();
                                try {
                                    String hasEmail = "";
                                    fbId = fbObject.getString("id");
                                    fbName = fbObject.getString("name");
                                    firstName =  fbObject.getString("first_name");
                                    lastName = fbObject.getString("last_name");
                                    fbImageURL = "https://graph.facebook.com/" + fbId + "/picture?type=large";
                                    System.out.println(">>> fbId"+fbId);
                                    System.out.println(">>> fbName"+fbName);
                                    System.out.println(">>> firstName"+firstName);
                                    System.out.println(">>> lastName"+lastName);
                                    System.out.println(">>> fbImageURL"+fbImageURL);

                                    // fbImgUrl = "https://graph.facebook.com/" + fbId + "/picture?type=large";
                                    if(fbObject.has("email")){

                                        System.out.println(">>> IN MAILLLL ");

                                        //IF FB user has email Id normal way signup
                                        if (!fbObject.getString("email").equals("") || !fbObject.getString("email").equals("null")) {
                                            email = fbObject.getString("email");
                                            System.out.println(">>> IN MAILLLL HAS EMAIL ID "+email);
                                            hasEmail = "Yes";  //if email present set it to the email text already
                                            if (isInternetPresent) {
                                                hasEmail = "Yes";
                                                editor.putString("signFBEmailId",email);
                                                editor.commit();
                                                System.out.println(">>> Before Dialog ");
                                                askEmailFromUserDialog(hasEmail);
                                            }else{
                                                Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }else {


                                        // if email is not available from FB user
                                        if (isInternetPresent) {
                                            hasEmail = "No";
                                            askEmailFromUserDialog(hasEmail);
                                        }else{
                                            Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
/**
 * AccessTokenTracker to manage logout
 */
                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                               AccessToken currentAccessToken) {
                        if (currentAccessToken == null) {
                            //tv_profile_name.setText("");
                        }
                    }
                };
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    public void onClick(View v) {
        if (v == fb) {
            facebookLogin.performClick();
        }
    }
    private void fbSingUp(String strEmail, String strPhone) {
        JSONObject fbsignupObject = new JSONObject();
        try {
            fbsignupObject.put("accesstoken",Config.accessToken);
            fbsignupObject.put("first_name",firstName);
            fbsignupObject.put("last_name", lastName);
            fbsignupObject.put("email", strEmail);
            fbsignupObject.put("password", password);
            fbsignupObject.put("phone_number", strPhone);
            fbsignupObject.put("facebook_token", fbId);
            fbsignupObject.put("avatar", fbImageURL);
            fbsignupObject.put("device_type", Config.DEVICE_TYPE);
            fbsignupObject.put("device_push_token", strFCMTokan);

            System.out.println(">>> FB SIGUP Login request :"+fbsignupObject);

            String singUpURL = Config.BASE_URL+"user/registration";
            new APIRequest(ActivitySignIn.this, fbsignupObject, singUpURL, this, Config.API_SIGNUP, Config.POST);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void askEmailFromUserDialog(String hasEmail) {
        dialogService1 = new Dialog(ActivitySignIn.this);
        dialogService1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogService1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogService1.setContentView(R.layout.ask_email_from_user);
        dialogService1.setCanceledOnTouchOutside(false);
        tvOkEmailDialog = (TextView) dialogService1.findViewById(R.id.tvOkEmailDialog);
        final EditText etUserEmailFB = (EditText) dialogService1.findViewById(R.id.etUserEmailFB);
        final EditText etUserPhoneFB = (EditText) dialogService1.findViewById(R.id.etUserPhoneFB);
        final ImageView ivClose = (ImageView) dialogService1.findViewById(R.id.ivClose);

        if(hasEmail.equalsIgnoreCase("Yes")){
            etUserEmailFB.setText(email);
        }

        etUserPhoneFB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textPos=etUserPhoneFB.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(etUserPhoneFB.getText().length()==3 && textPos!= 4)
                {
                    etUserPhoneFB.setText( "("+etUserPhoneFB.getText().toString()+")"+ " ");
                    etUserPhoneFB.setSelection(6);

                }else if (etUserPhoneFB.getText().length()==9 && textPos!=10){
                    etUserPhoneFB.setText(etUserPhoneFB.getText().toString()+"-");
                    etUserPhoneFB.setSelection(10);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogService1.dismiss();
            }
        });

        tvOkEmailDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = etUserEmailFB.getText().toString();
                String fPass = "";
                if(!email.equals("") ){
                    if(email.matches(Config.EMAIL_REGEX)){
                        String strPhone = etUserPhoneFB.getText().toString().trim();
                        if(strPhone.length() >= 10 && strPhone.length() <= 14){
                            dialogService1.dismiss();
                            if (isInternetPresent) {
                                fbSingUp(email,strPhone);
                            }else{
                                Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.minphonenomsg), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.PleaseEnterTheValidEmail), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(ActivitySignIn.this, getApplicationContext().getResources().getString(R.string.PleaseEnterTheValidEmail), Toast.LENGTH_SHORT).show();
                }
            }
        });


        WindowManager.LayoutParams wmlp = dialogService1.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER_HORIZONTAL;
        dialogService1.show();
        dialogService1.getWindow().setLayout((int) ((screenWidth / 5) * 4.2), (int) ((screenHeight / 10.5) * 5));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(BaseResponse response) {
        if(response.getApiName() == Config.API_SIGNIN){      //when SingIn response API is called
            LoginResponse loginResponse = (LoginResponse) response;
            if(loginResponse.getIsSuccess()){
                editor.putString("username",loginResponse.getLogindetails().getDisplayname());
                editor.putString("useremail",loginResponse.getLogindetails().getEmail());
                editor.putString("userSessionTokan",loginResponse.getLogindetails().getSessionUsertoken());
                editor.putString("isUserLoggedIn","Yes");
                editor.commit();
                inserUserLocationAPI();
            }else{
                Toast.makeText(ActivitySignIn.this,loginResponse.getErrMsg(),Toast.LENGTH_LONG).show();
            }
        }else if(response.getApiName() == Config.API_SIGNUP){


            SignUpResponse signupResponse = (SignUpResponse) response;
            if(signupResponse.getIsSuccess()){
                editor.putString("username",signupResponse.getLogindetails().getDisplayName());
                editor.putString("useremail",signupResponse.getLogindetails().getEmail());
                editor.putString("userSessionTokan",signupResponse.getLogindetails().getActivateHash());
                editor.putString("isUserLoggedIn","Yes");
                editor.commit();

                inserUserLocationAPI();
            }else{
                Toast.makeText(ActivitySignIn.this,signupResponse.getErrMsg(),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ActivitySignIn.this ,ActivityHome.class);
        startActivity(intent);
        finish();
    }

    private void inserUserLocationAPI() {
        GPSTracker gpsTracker = new GPSTracker(ActivitySignIn.this);
        /*String  firstTimeLat = getFormatedLatLong(gpsTracker.getLatitude());
        String  firstTimeLang = getFormatedLatLong(gpsTracker.getLongitude());
        */
        String  firstTimeLat = String.valueOf(gpsTracker.getLatitude());
        String  firstTimeLang = String.valueOf(gpsTracker.getLongitude());

        System.out.println(">>> User first time Lat Long :"+firstTimeLat+"--"+firstTimeLang);
        JSONObject loationObj = new JSONObject();
        try {
            editor.putString("firstTimeLat",firstTimeLat);
            editor.putString("firstTimeLang",firstTimeLang);
            editor.commit();


            loationObj.put("latitude",firstTimeLat);
            loationObj.put("longitude",firstTimeLang);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray locationArray = new JSONArray();
        locationArray.put(loationObj);
        if (isInternetPresent){
            new updateLocationAPI().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_user_token\":\"" + sharedPreferences.getString("userSessionTokan","") + "\",\"location_details\":" + locationArray + " }");
        }else {
            Toast.makeText(ActivitySignIn.this,R.string.noNetworkMsg,Toast.LENGTH_SHORT).show();
        }
    }

    class updateLocationAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivitySignIn.this);
            p.setMessage("Loading..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Location Update params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"updateuserlocation")
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
            p.dismiss();
            System.out.println(">>> LocationUpdate Result:" + s);

              Intent intent = new Intent(ActivitySignIn.this,ActivityHome.class);
              startActivity(intent);
              finish();
        }
    }

    private String getFormatedLatLong(double currentLat) {
        String strLat = "";
        String[] arr=String.valueOf(currentLat).split("\\.");
        Long[] longArr=new Long[2];

        longArr[0]=Long.parseLong(arr[0]); // 1
        longArr[1]=Long.parseLong(arr[1]); //

        String mainDigits = String.valueOf(longArr[0]);
        String strLenght = String.valueOf(longArr[1]);
        String first4char = strLenght.substring(0,4);
        String strFinal = mainDigits+"."+first4char;
        System.out.println(">>>> return val --- :"+strFinal);
        return strFinal;
    }

    private void CheckPermission() {
        if(ActivityCompat.checkSelfPermission(ActivitySignIn.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(ActivitySignIn.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivitySignIn.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivitySignIn.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[3]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignIn.this);
                builder.setTitle(ActivitySignIn.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignIn.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignIn.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivitySignIn.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivitySignIn.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignIn.this);
                builder.setTitle(ActivitySignIn.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignIn.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignIn.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
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
                builder.setNegativeButton(ActivitySignIn.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(ActivitySignIn.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        }else{
            proceedAfterPermission();
            System.out.println("YOU HAVE PERMISSION PROCESS ");
        }
    }
    private void proceedAfterPermission()
    {
        afterGettingPermission();
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
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignIn.this,permissionsRequired[3]))
            {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignIn.this);
                builder.setTitle(ActivitySignIn.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignIn.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignIn.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivitySignIn.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivitySignIn.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
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
    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) ActivitySignIn.this.getSystemService(Context.LOCATION_SERVICE);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivitySignIn.this);
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
        alertDialog.show();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(ActivitySignIn.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                if (isResumeCalled) {
                    afterGettingPermission();
                }
            }
        }
    }
}
