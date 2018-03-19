package com.beachbox.beachbox.User.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.Config.ConnectionDetector;
import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Adapter.AdapterRestaurant;

import org.w3c.dom.Text;

public class HelpActivity extends AppCompatActivity {
    Boolean isInternetPresent,isSuccess;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    WebView webView;
    String helpUrl = "";
    TextView tv_home,AdvName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        cd = new ConnectionDetector(HelpActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = HelpActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
       // tv_home = (TextView)findViewById(R.id.tv_home);
        webView = (WebView) findViewById(R.id.webView1);
       // AdvName =  (TextView)findViewById(R.id.AdvName);
        helpUrl = sharedPreferences.getString("helpenduser_url","").trim();

      /*  if(AdapterRestaurant.strAdvFlag.equalsIgnoreCase("yes")){
            AdvName.setText(sharedPreferences.getString("title",""));
        }*/


        webView.getSettings().setJavaScriptEnabled(true); // enable javascript
        final Activity activity = this;
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        webView .loadUrl(helpUrl);
       // setContentView(mWebview );

       /* tv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AdapterRestaurant.strAdvFlag.equalsIgnoreCase("yes")){
                    editor.putString("tabPosition","1");
                    editor.commit();
                    Intent i = new Intent(HelpActivity.this,UDashboardActivityNew.class);
                    startActivity(i);
                    finish();
                }else{
                    editor.putString("tabPosition","4");
                    editor.commit();
                    Intent i = new Intent(HelpActivity.this,UDashboardActivityNew.class);
                    startActivity(i);
                    finish();
                }
                editor.remove("helpenduser_url");
                editor.apply();
            }
        });*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(AdapterRestaurant.strAdvFlag.equalsIgnoreCase("yes")){
            editor.putString("tabPosition","1");
            editor.commit();
            Intent i = new Intent(HelpActivity.this,UDashboardActivityNew.class);
            startActivity(i);
            finish();
        }else{
            editor.putString("tabPosition","4");
            editor.commit();
            Intent i = new Intent(HelpActivity.this,UDashboardActivityNew.class);
            startActivity(i);
            finish();
        }
        editor.remove("helpenduser_url");
        editor.apply();

    }
}
