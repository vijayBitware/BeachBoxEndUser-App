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
import com.beachbox.beachbox.User.Model.signupResponse.SignUpResponse;
import com.beachbox.beachbox.volly.APIRequest;
import com.beachbox.beachbox.volly.BaseResponse;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by bitware on 22/3/17.
 */

public class ActivitySignUp extends Activity  implements APIRequest.ResponseHandler{

    TextView tv_back,tv_signIn,tv_goToSignIn;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent,isSuccess;
    EditText edtFirstName,edtLastName,edtEmail,edtPhoneNumber,edtPassword,edtConfirmPassword;
    private String response_msg="",firstName="",lastName="",email="",phoneNumber="",confirmPass="",password="",fbImageURL = "";
    int mobileNumber;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    LinearLayout llFBLogin;
    LoginButton signup_fb_button;
    String response_message_eng ="",fbEmail = "",fbId = "",
            strPhoneNoFromUser = "" , strMobileFromUser = "",fbName = "";
    TextView newusersignup,login,tvOkEmailDialog;
    Dialog dialogService1;
    public int screenWidth, screenHeight;
    public int textPos=0,txtFBPos = 0;
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
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_create_account);
        init();
        if(isInternetPresent){
            CheckPermission();
        }else {
            Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }

    }

    private void init() {

        cd = new ConnectionDetector(this);
        isInternetPresent =cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_back= (TextView) findViewById(R.id.tv_back);
        tv_signIn = (TextView) findViewById(R.id.tvSignIn);
        tv_goToSignIn = (TextView) findViewById(R.id.tv_signIn);
        llFBLogin = (LinearLayout)findViewById(R.id.llFBLogin);
        signup_fb_button = (LoginButton) findViewById(R.id.signup_fb_button);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        edtFirstName = (EditText) findViewById(R.id.edt_signUpFirstName);
        edtLastName = (EditText) findViewById(R.id.edt_signUpLastName);
        edtEmail= (EditText) findViewById(R.id.edt_signUpEmail);
        edtPassword = (EditText) findViewById(R.id.edt_signUpPassword);
        edtConfirmPassword = (EditText) findViewById(R.id.edt_signUpConfirmPassword);
        edtPhoneNumber= (EditText) findViewById(R.id.edt_signUpPhoneNumber);

        strFCMTokan = FirebaseInstanceId.getInstance().getToken();
        System.out.println(">> On Signup FCM tokan id :"+ strFCMTokan );

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        edtConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
        edtPassword.setTransformationMethod(new PasswordTransformationMethod());

       // ;

    }

    private void afterGetPermission() {
        isGPSEnabled = checkGPSEnabled();
        if (isGPSEnabled) {
            tv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivitySignUp.this,ActivityHome.class);
                    startActivity(intent);
                    finish();
                }
            });

            tv_goToSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivitySignUp.this,ActivitySignIn.class);
                    startActivity(intent);
                    finish();
                }
            });


            edtPhoneNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    textPos=edtPhoneNumber.getText().length();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(edtPhoneNumber.getText().length()==3 && textPos!= 4)
                    {
                        edtPhoneNumber.setText( "("+edtPhoneNumber.getText().toString()+")"+ " ");
                        edtPhoneNumber.setSelection(6);

                    }else if (edtPhoneNumber.getText().length()==9 && textPos!=10){
                        edtPhoneNumber.setText(edtPhoneNumber.getText().toString()+"-");
                        edtPhoneNumber.setSelection(10);
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            tv_signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firstName = edtFirstName.getText().toString().trim();
                    lastName = edtLastName.getText().toString().trim();
                    email=edtEmail.getText().toString().trim();
                    password=edtPassword.getText().toString().trim();
                    confirmPass=edtConfirmPassword.getText().toString().trim();
                    phoneNumber= edtPhoneNumber.getText().toString().trim();

                    if (isInternetPresent){
                        if (!firstName.isEmpty()){
                            if (!lastName.isEmpty()){
                                if (!email.isEmpty()){
                                    if (email.matches(Config.EMAIL_REGEX)){
                                        if (!phoneNumber.isEmpty()){
                                            if (((phoneNumber.length()) <= 14) && ((phoneNumber.length()) >= 10)){
                                                if (!password.isEmpty()){
                                                    if (password.length() >= 8){
                                                        if (!confirmPass.isEmpty()){
                                                            if (password.equals(confirmPass)) {
                                                                if (isInternetPresent){

                                                                    requestSingupAPI();
                                                                    //  new SingUpTask().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"first_name\":\"" + firstName + "\",\"last_name\":\"" + lastName + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"phone_number\":\"" + phoneNumber + "\"}");
                                                                }else {
                                                                    Toast.makeText(ActivitySignUp.this,getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }else {
                                                                edtConfirmPassword.requestFocus();
                                                                edtConfirmPassword.setError("Password & confirm password should be match");                                                        }
                                                        }else {
                                                            edtConfirmPassword.requestFocus();
                                                            edtConfirmPassword.setError("Please enter confirm password");
                                                        }
                                                    }else {
                                                        edtPassword.requestFocus();
                                                        edtPassword.setError(ActivitySignUp.this.getResources().getString(R.string.minEightCharPass));
                                                    }
                                                }else {
                                                    edtPassword.requestFocus();
                                                    edtPassword.setError(ActivitySignUp.this.getResources().getString(R.string.minEightCharPass));
                                                }
                                            }else {
                                                edtPhoneNumber.requestFocus();
                                                edtPhoneNumber.setError(ActivitySignUp.this.getResources().getString(R.string.minphonenomsg));                                    }
                                        }else {
                                            edtPhoneNumber.requestFocus();
                                            edtPhoneNumber.setError("Please enter phone no");
                                        }
                                    }else {
                                        edtEmail.requestFocus();
                                        edtEmail.setError("Please enter valid email");
                                    }
                                }else {
                                    edtEmail.requestFocus();
                                    edtEmail.setError("Please enter email");
                                }
                            }else {
                                edtLastName.requestFocus();
                                edtLastName.setError("Please enter last name");
                            }
                        }else {
                            edtFirstName.requestFocus();
                            edtFirstName.setError("Please enter first name");
                        }
                    }else {
                        Toast.makeText(ActivitySignUp.this,getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                    }
                }
            });

            fbInItData();
        } else {
        showSettingsAlert();
    }
    }

    private void fbInItData() {
        signup_fb_button.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
        callbackManager = CallbackManager.Factory.create();

        signup_fb_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
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
                                    if(fbObject.has("email")){
                                        //IF FB user has email Id normal way signup
                                        if (!fbObject.getString("email").equals("") || !fbObject.getString("email").equals("null")) {
                                            email = fbObject.getString("email");
                                            hasEmail = "Yes";  //if email present set it to the email text already
                                            if (isInternetPresent) {
                                                hasEmail = "Yes";
                                                editor.putString("signFBEmailId",email);
                                                editor.commit();
                                                askEmailFromUserDialog(hasEmail);
                                            }else{
                                                Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }else {
                                        // if email is not available from FB user
                                        if (isInternetPresent) {
                                            hasEmail = "No";
                                            askEmailFromUserDialog(hasEmail);
                                        }else{
                                            Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
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
    private void requestSingupAPI() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accesstoken",Config.accessToken);
            jsonObject.put("first_name",firstName);
            jsonObject.put("last_name", lastName);
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("phone_number", phoneNumber);
           jsonObject.put("device_type", Config.DEVICE_TYPE);
           jsonObject.put("device_push_token", strFCMTokan);

            String singUpURL = Config.BASE_URL+"user/registration";

            new APIRequest(ActivitySignUp.this, jsonObject, singUpURL, this, Config.API_SIGNUP, Config.POST);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void fbSingUp(String email, String phone) {

        isGPSEnabled = checkGPSEnabled();
        if(isGPSEnabled){
             JSONObject fbsignupObject = new JSONObject();
                    try {
                        fbsignupObject.put("accesstoken",Config.accessToken);
                        fbsignupObject.put("first_name",firstName);
                        fbsignupObject.put("last_name", lastName);
                        fbsignupObject.put("email", email);
                        fbsignupObject.put("password", password);
                        fbsignupObject.put("phone_number", phone);
                        fbsignupObject.put("facebook_token", fbId);
                        fbsignupObject.put("avatar", fbImageURL);
                       fbsignupObject.put("device_type", Config.DEVICE_TYPE);
                       fbsignupObject.put("device_push_token", strFCMTokan);

                        System.out.println(">>> FB Signup request is :"+fbsignupObject);
                        String singUpURL = Config.BASE_URL+"user/registration";
                        new APIRequest(ActivitySignUp.this, fbsignupObject, singUpURL, this, Config.API_SIGNUP, Config.POST);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
        }else{
            showSettingsAlert();
        }

    }
    public void onClick(View v) {
        if (v == llFBLogin) {
            signup_fb_button.performClick();
        }
    }

    private void askEmailFromUserDialog(String hasEmail) {
        dialogService1 = new Dialog(ActivitySignUp.this);
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
                                Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                        Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.minphonenomsg), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.PleaseEnterTheValidEmail), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(ActivitySignUp.this, getApplicationContext().getResources().getString(R.string.PleaseEnterTheValidEmail), Toast.LENGTH_SHORT).show();
                }
            }
        });


        WindowManager.LayoutParams wmlp = dialogService1.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER_HORIZONTAL;
        dialogService1.show();
        dialogService1.getWindow().setLayout((int) ((screenWidth / 5) * 4.2), (int) ((screenHeight / 10) * 5));
    }
    @Override
    public void onSuccess(BaseResponse response) {
        SignUpResponse signupResponse = (SignUpResponse) response;
        if(signupResponse.getIsSuccess()){

            editor.putString("username",signupResponse.getLogindetails().getDisplayName());
            editor.putString("useremail",signupResponse.getLogindetails().getEmail());
            editor.putString("userSessionTokan",signupResponse.getLogindetails().getActivateHash());
            editor.putString("isUserLoggedIn","Yes");
            editor.commit();

            inserUserLocationAPI();

        }else{
            Toast.makeText(ActivitySignUp.this,signupResponse.getErrMsg(),Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onFailure(BaseResponse response) {
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void inserUserLocationAPI() {
        GPSTracker gpsTracker = new GPSTracker(ActivitySignUp.this);

        double firstTimeLat= gpsTracker.getLatitude();
        double firstTimeLang= gpsTracker.getLongitude();
        System.out.println(">>> User first time Lat Long :"+firstTimeLat+"--"+firstTimeLang);
        JSONObject loationObj = new JSONObject();
        try {
            editor.putString("firstTimeLat",String.valueOf(firstTimeLat));
            editor.putString("firstTimeLang",String.valueOf(firstTimeLang));
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
            Toast.makeText(ActivitySignUp.this,R.string.noNetworkMsg,Toast.LENGTH_SHORT).show();
        }
    }

    class updateLocationAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivitySignUp.this);
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
            System.out.println(">>> LocationUpdate Result:" + s);
            p.dismiss();
            Intent intent = new Intent(ActivitySignUp.this,ActivityHome.class);
            startActivity(intent);
            finish();
        }
    }


    private void CheckPermission() {
        if(ActivityCompat.checkSelfPermission(ActivitySignUp.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(ActivitySignUp.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivitySignUp.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivitySignUp.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[3]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignUp.this);
                builder.setTitle(ActivitySignUp.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignUp.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignUp.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivitySignUp.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivitySignUp.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignUp.this);
                builder.setTitle(ActivitySignUp.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignUp.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignUp.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
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
                builder.setNegativeButton(ActivitySignUp.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(ActivitySignUp.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
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
        afterGetPermission();
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
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivitySignUp.this,permissionsRequired[3]))
            {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivitySignUp.this);
                builder.setTitle(ActivitySignUp.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivitySignUp.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivitySignUp.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivitySignUp.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivitySignUp.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
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
        LocationManager lm = (LocationManager) ActivitySignUp.this.getSystemService(Context.LOCATION_SERVICE);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivitySignUp.this);
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
            if (ActivityCompat.checkSelfPermission(ActivitySignUp.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                if (isResumeCalled) {
                    afterGetPermission();
                }
            }
        }
    }
}
