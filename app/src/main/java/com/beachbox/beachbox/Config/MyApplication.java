package com.beachbox.beachbox.Config;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by bitwarepc on 11-Jul-17.
 */

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class
            .getSimpleName();
    private RequestQueue mRequestQueue;
    private static MyApplication mInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

  /*  @Override
    public boolean verify(String hostname, SSLSession session) {
        System.out.println(">>>> Approving certificate for "+hostname);
        return true;
    }*/
}