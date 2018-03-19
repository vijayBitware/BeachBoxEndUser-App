package com.beachbox.beachbox.User.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.beachbox.beachbox.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by bitware on 12/4/17.
 */
/**
 * This class for implementing map.
 */

public class ActivityMap extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ViewSwitcher.ViewFactory{

    private double latitude, longitude;
    MapView mapView;
    private GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    LinearLayout ll_map;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init();
        showMap(savedInstanceState);

        ll_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","1");
                editor.commit();
                Intent intent = new Intent(ActivityMap.this ,UDashboardActivityNew.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void showMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.googleMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        googleMap = mapView.getMap();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationChangeListener(myLocationChangeListener());
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
    }

    private void init() {
        ll_map = (LinearLayout) findViewById(R.id.ll_map);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.e("Location >> ", "Lattitude-" + latitude + " " + "Logitude-" + longitude);
                googleMap.clear();
                MarkerOptions mp = new MarkerOptions();
                mp.position(new LatLng(latitude, longitude));
                mp.title("my position");
                googleMap.addMarker(mp);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        };
    }
    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
