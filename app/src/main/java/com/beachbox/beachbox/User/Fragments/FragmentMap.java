package com.beachbox.beachbox.User.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.ActivityMap;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class used for showing map.
 * Created by bitwarepc on 25-Jul-17.
 */

public class FragmentMap extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ViewSwitcher.ViewFactory{
    View view;
    private double latitude, longitude;
    MapView mapView;
    private GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    LinearLayout ll_map;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean zoomIn = true;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_map,null);
            init();
            showMap(savedInstanceState);
        }
        return view;
    }

    private void init() {
        ll_map = (LinearLayout)view. findViewById(R.id.ll_map);
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        ll_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((UDashboardActivityNew)getActivity()).replaceFragment(new FragmentRestaurants());

            }
        });


    }
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                googleMap.clear();
                MarkerOptions mp = new MarkerOptions();
                mp.position(new LatLng(latitude, longitude));
                mp.title("My position");
                googleMap.addMarker(mp);

                MarkerOptions mp1;
                // for setting the restaurant on marker
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.mapiconnew);

                for (int i = 0; i < FragmentRestaurants.arrNewMap.size() ; i++) {

                    if(FragmentRestaurants.arrNewMap.get(i).getAdvertisement() == null){
                        if(!FragmentRestaurants.arrNewMap.get(i).getLat().isEmpty()){

                            /*mp1 = new MarkerOptions();
                            mp1.position(new LatLng(Double.parseDouble(FragmentRestaurants.arrNewMap.get(i).getLat()), Double.parseDouble(FragmentRestaurants.arrNewMap.get(i).getLng())));
                            mp1.title(FragmentRestaurants.arrNewMap.get(i).getRestaurantName());
                            mp1.icon(icon);
                            //mp1.snippet(FragmentRestaurants.arrNewMap.get(i).getTitle());
                            googleMap.addMarker(mp1);*/

                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(FragmentRestaurants.arrNewMap.get(i).getLat()), Double.parseDouble(FragmentRestaurants.arrNewMap.get(i).getLng())))
                                    .icon(icon)
                                    .title(FragmentRestaurants.arrNewMap.get(i).getRestaurantName()));
                        }

                    }
                }

                if(zoomIn){
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 10.0f)
                    );
                    zoomIn = false;
                }
            }
        };
    };

    private void showMap(Bundle savedInstanceState) {
        mapView = (MapView)view. findViewById(R.id.googleMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        googleMap = mapView.getMap();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


        System.out.println(">>> FragmentRestaurants.arrNewMap.size() :"+FragmentRestaurants.arrNewMap.size());
        for (int i = 0; i < FragmentRestaurants.arrNewMap.size() ; i++) {
            if(FragmentRestaurants.arrNewMap.get(i).getAdvertisement() != null){
                System.out.println(">> ADV NO :"+FragmentRestaurants.arrNewMap.get(i).getAdvertisement());

            }else{
                System.out.println(">> LAT :"+FragmentRestaurants.arrNewMap.get(i).getLat()+"--"+FragmentRestaurants.arrNewMap.get(i).getLng());

            }

        }

    }


    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {
       // Toast.makeText(getActivity(), "MAPP CLLIILLKKKK", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(getActivity(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
