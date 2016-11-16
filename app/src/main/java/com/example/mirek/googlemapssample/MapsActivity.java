package com.example.mirek.googlemapssample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.example.mirek.googlemapssample.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final Handler movementHandler = new Handler();
    private Runnable firstMove;
    private Runnable secondMove;
    private Runnable thirdMove;
    private LatLng[] places;
    private LatLng fiji;
    private LatLng hawaii;
    private LatLng mountainView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-33.866, 151.195);

        fiji = new LatLng(-18.142, 178.431);
        hawaii = new LatLng(21.291, -157.821);
        mountainView = new LatLng(37.423, -122.091);
        places = new LatLng[]{fiji, hawaii, mountainView, hawaii, fiji, sydney};

        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addPolyline(new PolylineOptions().geodesic(true)
                .add(sydney)
                .add(fiji)
                .add(hawaii)
                .add(mountainView));

        final Marker marker = mMap.addMarker(new MarkerOptions().
                position(sydney).
                title("Hello world"));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MarkerAnimation.animateMarkerToICS(mMap, marker, mountainView, new LatLngInterpolator.Spherical());
            }
        }, 2000);

    }

    private void go() {
        final int movementPace = 1000;

        ArrayList<Movement> movements = new ArrayList<>(places.length);
        for (int i = 0; i < places.length; i++) {
            Movement movement = new Movement(places[i]);
            movement.setLength(movementPace);
            movements.add(movement);
        }

        for (int i = 0; i < movements.size(); i++) {
            int nextRunnableIndex = i + 1;
            if (nextRunnableIndex < movements.size()) {
                movements.get(i).setNextRunnable(movements.get(i + 1));
            }
        }
        movementHandler.postDelayed(movements.get(0), movementPace);
    }

    class Movement implements Runnable {
        private Runnable nextRunnable;
        private LatLng place;
        private int length;

        public Movement(LatLng placeToMove) {
            this.place = placeToMove;
        }

        public void setNextRunnable(Runnable nextRunnable) {
            this.nextRunnable = nextRunnable;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Override
        public void run() {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
            if (nextRunnable != null) {
                movementHandler.postDelayed(nextRunnable, length);
            }
        }
    }

}
