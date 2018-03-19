package com.beachbox.beachbox.User.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.AndroidMultiPartEntity;
import com.beachbox.beachbox.Config.Config;
import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.Config.MultipartBasic;
import com.beachbox.beachbox.Config.UploadStuff;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.client.ClientProtocolException;

import static android.R.attr.bitmap;
import static android.R.attr.text;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by bitwarepc on 22-Jul-17.
 */

public class FragmentUpdateProfile  extends Fragment{
    View view;
    TextView tvUpdateSave,tv_back;
    EditText etUpdateFirstName,etUpdateLastName,etUpdatePhone;
    TextView tvUpdateEmail;
    ImageView img_photo_profile,ivPhotoUpload;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String strFirstName = "",strLastName = "",strEmail = "",strPhone = "";
    Boolean idCamera = false;
    private Uri fileUriId = null;
    private final int SELECT_PHOTO= 1;
    String uploadImagePath;
    int textPos = 0;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    ImageView CamprofilePic;
    boolean lock = false;

      String[] permissionsRequired = new String[]{
            Manifest.permission.CAMERA };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       if(view == null){
           view = inflater.inflate(R.layout.activity_update_profile,container,false);
           inIt();
       }


        return view;
    }

    private void inIt() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent =cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        permissionStatus = getActivity().getSharedPreferences("permissionStatus",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        etUpdateFirstName = (EditText)view.findViewById(R.id.etUpdateFirstName);
        etUpdateLastName = (EditText)view.findViewById(R.id.etUpdateLastName);
        tvUpdateEmail = (TextView) view.findViewById(R.id.tvUpdateEmail);
        etUpdatePhone = (EditText)view.findViewById(R.id.etUpdatePhone);
        tvUpdateSave = (TextView)view.findViewById(R.id.tvUpdateSave);
        tv_back = (TextView)view.findViewById(R.id.tv_back);
        img_photo_profile = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.img_photo_profile);
        ivPhotoUpload =  (ImageView) view.findViewById(R.id.ivUpload);
        CamprofilePic = (ImageView) view.findViewById(R.id.CamprofilePic);

        setValue();

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getFragmentManager().popBackStackImmediate();
                editor.putString("tabPosition","4");
                editor.commit();
                Intent i = new Intent(getActivity(), UDashboardActivityNew.class);
                startActivity(i);
            }
        });


        ivPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();

            }
        });

        phoneEditText();

        tvUpdateSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strFirstName = etUpdateFirstName.getText().toString().trim();
                strLastName = etUpdateLastName.getText().toString().trim();
                strEmail = tvUpdateEmail.getText().toString().trim();
                strPhone = etUpdatePhone.getText().toString().trim();

                if(isInternetPresent){
                    if (!strFirstName.isEmpty()){
                        if (!strLastName.isEmpty()){
                            if (!strEmail.isEmpty()){
                                if (strEmail.matches(Config.EMAIL_REGEX)){
                                    if (!strPhone.isEmpty()){
                                       if(strPhone.length() >= 10 && strPhone.length() <= 14){
                                       // if (strPhone.matches(Config.PHONE_REGEX)){
                                            if (isInternetPresent){
                                                String tokenId = sharedPreferences.getString("userSessionTokan","");
                                                // new updateProfileNew().execute("");
                                                new updateProfile().execute("{\"accesstoken\":\"" + Config.accessToken + "\",\"session_usertoken\":\"" + tokenId + "\",\"first_name\":\"" + strFirstName + "\",\"last_name\":\"" + strLastName + "\",\"email\":\"" + strEmail + "\",\"phonenumber\":\"" + strPhone + "\"}");
                                            }else {
                                                Toast.makeText(getActivity(),getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Toast.makeText(getActivity(),getResources().getString(R.string.minphonenomsg),Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        etUpdatePhone.requestFocus();
                                        etUpdatePhone.setError("Please enter phone no");                                                        }
                                }else {
                                    //tvUpdateEmail.requestFocus();
                                   // tvUpdateEmail.setError("Please enter valid email");
                                }
                            }else {
                               // tvUpdateEmail.requestFocus();
                               // tvUpdateEmail.setError("Please enter email");
                            }
                        }else {
                            etUpdateLastName.requestFocus();
                            etUpdateLastName.setError("Please enter lastname");                                }
                    }else{
                        etUpdateFirstName.requestFocus();
                        etUpdateFirstName.setError("Please enter firstname");
                    }
                }else{
                    Toast.makeText(getActivity(),getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

    private void selectProfilePic() {
        final CharSequence[] options = {"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera"))
                {
                    idCamera  = true;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUriId = UploadStuff.getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUriId);
                    startActivityForResult(intent, SELECT_PHOTO);

                }else if (options[item].equals("Gallery"))
                {
                    idCamera  = false;
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

    private void setValue() {
        etUpdateFirstName.setText(sharedPreferences.getString("first_name",""));
        etUpdateLastName.setText(sharedPreferences.getString("last_name",""));
        tvUpdateEmail.setText(sharedPreferences.getString("email",""));
        etUpdatePhone.setText(sharedPreferences.getString("customer_phone",""));
        String userImg = sharedPreferences.getString("CustomerPic","");
        if(userImg.isEmpty() ||userImg.equals("") ){
            img_photo_profile.setImageResource(R.drawable.profile);
        } else {
            Picasso.with(getActivity())
                    .load(userImg)
                    .into(img_photo_profile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (idCamera) {
                        uploadImagePath = fileUriId.getPath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(uploadImagePath, options);

                        ExifInterface ei = null;
                        try {
                            ei = new ExifInterface(uploadImagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);

                        Bitmap rotatedBitmap = null;
                        switch(orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotatedBitmap = rotateImage(bitmap, 90);
                                img_photo_profile.setImageBitmap(rotatedBitmap);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotatedBitmap = rotateImage(bitmap, 180);
                                img_photo_profile.setImageBitmap(rotatedBitmap);

                                break;

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotatedBitmap = rotateImage(bitmap, 270);
                                img_photo_profile.setImageBitmap(rotatedBitmap);

                                break;

                            case ExifInterface.ORIENTATION_NORMAL:
                            default:
                                rotatedBitmap = bitmap;
                                img_photo_profile.setImageBitmap(rotatedBitmap);

                        }




                    } else {
                        Uri selectedImageUri = data.getData();
                        String imagepath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        img_photo_profile.setImageBitmap(bitmap);
                        uploadImagePath = imagepath;
                        System.out.println(">>> Galllery uploadImagePath :"+ uploadImagePath);
                    }



                    if(isInternetPresent){
                        new uploadProfilePic().execute("");
                    }else{
                        Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission();
            }
        }
    }

    class uploadProfilePic extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setMessage("In Progress..");
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String responseString = null;
            org.apache.http.client.HttpClient httpclient = new org.apache.http.impl.client.DefaultHttpClient();
            org.apache.http.client.methods.HttpPost httppost = new org.apache.http.client.methods.HttpPost(Config.PHOTO_UPLOAD_URL+"user/profile");
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

                System.out.println(""+Config.accessToken);
                File fileObj = new File(uploadImagePath);
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
            System.out.println(">>>Upload profile pic :"+s);
            if (s != null) {
                p.dismiss();
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
            }
        }
    }
    class updateProfile extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
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
            System.out.println(">>> Update Profile params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"user/updateprofile")
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
            System.out.println(">>> Update Profile up result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    boolean is_success = jsonObject.getBoolean("is_success");
                    if(is_success){
                        Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        editor.putString("tabPosition","1");
                        editor.commit();

                        getFragmentManager().popBackStackImmediate();
                    }else{
                        String strMsg = jsonObject.getString("err_msg");
                        Toast.makeText(getActivity(),strMsg,Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Toast.makeText(ActivitySignUp.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    private void phoneEditText() {
        etUpdatePhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textPos=etUpdatePhone.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                System.out.println("*********above if*********"+etUpdatePhone.getText().length()+"**"+textPos);


                if(etUpdatePhone.getText().length()==3 && textPos!= 4)
                {
                    System.out.println("*********if*********");
                    etUpdatePhone.setText( "("+etUpdatePhone.getText().toString()+")"+ " ");
                    etUpdatePhone.setSelection(6);

                }else if (etUpdatePhone.getText().length()==9 && textPos!=10){
                    System.out.println("*********else1*********");
                    etUpdatePhone.setText(etUpdatePhone.getText().toString()+"-");
                    etUpdatePhone.setSelection(10);
                }else if(etUpdatePhone.getText().length()==9 && textPos == 10)
                {
                    System.out.println("*********123*********");
                    String text = etUpdatePhone.getText().delete(8, 9).toString();
                    etUpdatePhone.setText(text);
                    etUpdatePhone.setSelection(8);

                }else if(etUpdatePhone.getText().length() == 5)
                {
                    System.out.println("*********123444444444*********");
                    String text = etUpdatePhone.getText().delete(3, 5).toString();
                    etUpdatePhone.setText(text);
                    str = etUpdatePhone.getText().toString().replaceAll("\\(", "").replaceAll("\\)","");;
                   // String text1 = etUpdatePhone.getText().delete(1, 2).toString();
                    //etUpdatePhone.setText(text1);
                    String[] arr = str.split(" ");
                    String strA = arr[0];
//                    String strnew = arr[1];
                    System.out.println("*********123444444444*****text****"+etUpdatePhone.getText().toString()+"**"+str+"**"+strA);
                    etUpdatePhone.setText(strA.toString());
                    etUpdatePhone.setSelection(2);
                }
               // validatePhone(etUpdatePhone);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    public boolean validatePhone(View view){
        String phone = etUpdatePhone.getText().toString();
        if(isValidPhone(phone)){
            Toast.makeText(view.getContext(), "Phone number is valid", Toast.LENGTH_LONG).show();
            return true;
        }else{
            Toast.makeText(view.getContext(), "Phone number is invalid", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static boolean isValidPhone(String phone)
    {
        String expression = "^([0-9\\+]|\\(\\d{1,3}\\))[0-9\\-\\. ]{3,15}$";
        CharSequence inputString = phone;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.matches())
        {
            return true;
        }
        else{
            return false;
        }
    }
    private void checkCameraPermission() {
        if(ActivityCompat.checkSelfPermission(getActivity(), permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),permissionsRequired[0])){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.Permissionheader));
                builder.setMessage(getActivity().getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(getActivity(),permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }  else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.Permissionheader));
                builder.setMessage(getActivity().getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getActivity().getBaseContext(), getActivity().getResources().getString(R.string.cameraSettingPermission), Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(getActivity(),permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        }else{
            proceedAfterPermission();

        }
    }

    private void proceedAfterPermission() {
        selectProfilePic();
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
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),permissionsRequired[0]))
            {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.Permissionheader));
                builder.setMessage(getActivity().getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(getActivity(),permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getActivity().getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

}
