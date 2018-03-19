package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.AndroidMultiPartEntity;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.MultipartBasic;
import com.beachbox.beachbox.R;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import cz.msebera.android.httpclient.client.ClientProtocolException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 *
 * This class to update user details.
 * Created by bitwarepc on 06-Jul-17.
 */

public class ActivityUpdateProfile extends Activity {

    TextView tvUpdateSave, tv_back;
    EditText etUpdateFirstName, etUpdateLastName, etUpdateEmail, etUpdatePhone;
    ImageView img_photo_profile, ivPhotoUpload;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String strFirstName = "", strLastName = "", strEmail = "", strPhone = "";
    Boolean idCamera = false;
    private Uri fileUriId = null;
    private final int SELECT_PHOTO = 1;
    String profileImagePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        inIt();

        tvUpdateSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strFirstName = etUpdateFirstName.getText().toString().trim();
                strLastName = etUpdateLastName.getText().toString().trim();
                strEmail = etUpdateEmail.getText().toString().trim();
                strPhone = etUpdatePhone.getText().toString().trim();


                if (isInternetPresent) {
                    if (!strFirstName.isEmpty()) {
                        if (!strLastName.isEmpty()) {
                            if (!strEmail.isEmpty()) {
                                if (strEmail.matches(Config.EMAIL_REGEX)) {
                                    if (!strPhone.isEmpty()) {
                                        if (isInternetPresent) {
                                            String tokenId = sharedPreferences.getString("userSessionTokan", "");
                                            // new updateProfileNew().execute("");
                                            new updateProfileData().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_usertoken\":\"" + tokenId + "\",\"first_name\":\"" + strFirstName + "\",\"last_name\":\"" + strLastName + "\",\"email\":\"" + strEmail + "\",\"phonenumber\":\"" + strPhone + "\"}");
                                        } else {
                                            Toast.makeText(ActivityUpdateProfile.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        etUpdatePhone.requestFocus();
                                        etUpdatePhone.setError("Please enter phone no");
                                    }
                                } else {
                                    etUpdateEmail.requestFocus();
                                    etUpdateEmail.setError("Please enter valid email");
                                }
                            } else {
                                etUpdateEmail.requestFocus();
                                etUpdateEmail.setError("Please enter email");
                            }
                        } else {
                            etUpdateLastName.requestFocus();
                            etUpdateLastName.setError("Please enter lastname");
                        }
                    } else {
                        etUpdateFirstName.requestFocus();
                        etUpdateFirstName.setError("Please enter firstname");
                    }
                } else {
                    Toast.makeText(ActivityUpdateProfile.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    //initialization
    private void inIt() {
        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        etUpdateFirstName = (EditText) findViewById(R.id.etUpdateFirstName);
        etUpdateLastName = (EditText) findViewById(R.id.etUpdateLastName);
      //  etUpdateEmail = (EditText) findViewById(R.id.tv);
        etUpdatePhone = (EditText) findViewById(R.id.etUpdatePhone);
        tvUpdateSave = (TextView) findViewById(R.id.tvUpdateSave);
        tv_back = (TextView) findViewById(R.id.tv_back);
        img_photo_profile = (ImageView) findViewById(R.id.img_photo_profile);
        ivPhotoUpload = (ImageView) findViewById(R.id.ivUpload);

        setValue();

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition", "3");
                editor.commit();
                Intent intent = new Intent(ActivityUpdateProfile.this, UDashboardActivityNew.class);
                startActivity(intent);
            }
        });

        ivPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfilePic();
            }
        });

    }

    //upload profile picture
    private void uploadProfilePic() {
        final CharSequence[] options = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityUpdateProfile.this);
        builder.setTitle("Add Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera")) {
                    idCamera = true;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUriId = MultipartBasic.getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, SELECT_PHOTO);
                } else if (options[item].equals("Gallery")) {
                    idCamera = false;
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (idCamera) {
                        profileImagePath = fileUriId.getPath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(profileImagePath, options);
                        img_photo_profile.setImageBitmap(bitmap);
                        System.out.println(">>> Camera profileImagePath :" + profileImagePath);
                    } else {
                        Uri selectedImageUri = data.getData();
                        String imagepath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        img_photo_profile.setImageBitmap(bitmap);
                        profileImagePath = imagepath;
                        System.out.println(">>> Galllery profileImagePath :" + profileImagePath);
                    }
                    if (isInternetPresent) {
                        new  uploadProfilePic().execute("");
                    } else {
                        Toast.makeText(ActivityUpdateProfile.this, ActivityUpdateProfile.this.getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
        }
    }

    private void setValue() {
       /* etUpdateFirstName = (EditText)findViewById(R.id.etUpdateFirstName);
         = (EditText)findViewById(R.id.etUpdateLastName);
        etUpdateEmail = (EditText)findViewById(R.id.etUpdateEmail);
        etUpdatePhone*/
        etUpdateFirstName.setText(sharedPreferences.getString("first_name", ""));
        etUpdateLastName.setText(sharedPreferences.getString("last_name", ""));
        etUpdateEmail.setText(sharedPreferences.getString("email", ""));
        etUpdatePhone.setText(sharedPreferences.getString("customer_phone", ""));
        //  customer_pic.setText(sharedPreferences.getString("customer_pic",""));

    }


    class updateProfileData extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityUpdateProfile.this);
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            //  client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            //client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Update Profile params :" + params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "user/updateprofile")
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>> Update Profile up result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    boolean is_success = jsonObject.getBoolean("is_success");
                    if (is_success == true) {
                        Toast.makeText(ActivityUpdateProfile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        editor.putString("tabPosition", "1");
                        editor.commit();
                        //Intent intent = new Intent(ActivityUpdateProfile.this, UDashboardActivityNew.class);
                        //startActivity(intent);
                        //finish();
                    } else {
                        String strMsg = jsonObject.getString("err_msg");
                        Toast.makeText(ActivityUpdateProfile.this, strMsg, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Toast.makeText(ActivitySignUp.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }


    class uploadProfilePic extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityUpdateProfile.this);
            p.setMessage("In Progress..");
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String responseString = null;
            org.apache.http.client.HttpClient httpclient = new org.apache.http.impl.client.DefaultHttpClient();
            org.apache.http.client.methods.HttpPost httppost = new org.apache.http.client.methods.HttpPost(Config.BASE_URL+"user/profile");
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                //publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                // Adding file data to http body
                // Extra parameters if you want to pass to server

                File fileObj = new File(profileImagePath);
                entity.addPart("file",  new FileBody(fileObj));
                entity.addPart("session_user_token",  new StringBody(sharedPreferences.getString("userSessionTokan","")));
                entity.addPart("accesstoken",  new StringBody(Config.accessToken));

                httppost.setEntity(entity);
                HttpResponse response = (HttpResponse) httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                responseString = EntityUtils.toString(r_entity);
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("Upload profile pic :"+s);
            if (s != null) {
                p.dismiss();
                try {
                    JSONObject object = new JSONObject(s);
                    if(object.getString("is_success").equalsIgnoreCase("true")){

                    } else{
                        Toast.makeText(ActivityUpdateProfile.this, object.getString("err_msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
            }
        }
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
