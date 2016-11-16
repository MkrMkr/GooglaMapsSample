package com.example.mirek.googlemapssample;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-33.866, 151.195);

        fiji = new LatLng(-18.142, 178.431);
        hawaii = new LatLng(21.291, -157.821);
        mountainView = new LatLng(37.423, -122.091);
        places = new LatLng[]{fiji, hawaii, mountainView, hawaii, fiji, sydney};

        try {
            final List<LatLng> routePoints = readRouteFromJsonResources();
            LatLng firstPos = routePoints.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(firstPos));

            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

            LatLng[] points = routePoints.toArray(new LatLng[routePoints.size()]);

            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(points));

            final Marker markerInFirstPosition = mMap.addMarker(new MarkerOptions().position(firstPos).title("First point position"));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MarkerToIcsAnimator markerToIcsAnimator = new MarkerToIcsAnimator(markerInFirstPosition, routePoints);
                    markerToIcsAnimator.runAnimation();
                }
            }, 2000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class MarkerToIcsAnimator implements Animator.AnimatorListener {

        private List<LatLng> routePoints;
        private int lastPositionIndex = 0;
        private Marker marker;
        private double speed = 1;  // m/s

        public MarkerToIcsAnimator(Marker movingMarker, List<LatLng> route) {
            this.marker = movingMarker;
            this.routePoints = route;
        }

        public void runAnimation() {
            if (routePoints != null && routePoints.size() > 1) {
                lastPositionIndex++;

                double distance = SphericalUtil.computeDistanceBetween(routePoints.get(lastPositionIndex - 1),
                        routePoints.get
                                (lastPositionIndex));

                double time = distance / speed;

                MarkerAnimation.animateMarkerToICS(mMap, marker, routePoints.get(lastPositionIndex), time, new LatLngInterpolator
                        .Spherical(), this);
            }
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (routePoints != null && routePoints.size() > 2) {
                lastPositionIndex++;
                if (lastPositionIndex < routePoints.size()) {
                    double distance = SphericalUtil.computeDistanceBetween(routePoints.get(lastPositionIndex - 1),
                            routePoints.get
                                    (lastPositionIndex));

                    double time = distance / speed;

                    MarkerAnimation.animateMarkerToICS(mMap, marker, routePoints.get(lastPositionIndex), time, new
                            LatLngInterpolator.Spherical(), this);


                    Log.i("DISTANCE", "" + distance);
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    }

    private List<LatLng> readRouteFromJsonResources() throws IOException, JSONException {
        StringBuilder builder = new StringBuilder();
        InputStream in = getResources().openRawResource(R.raw.route);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        JSONObject root = new JSONObject(builder.toString());
        JSONArray routes = root.getJSONArray("steps");

        List<LatLng> routePoints = new ArrayList<>();
        for (int i = 0; i < routes.length(); i++) {
            JSONArray locations = routes.getJSONObject(i).getJSONArray("location");
            routePoints.add(new LatLng(locations.getDouble(1), locations.getDouble(0)));
        }

        return routePoints;
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
