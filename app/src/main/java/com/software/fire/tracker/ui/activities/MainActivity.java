package com.software.fire.tracker.ui.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.software.fire.tracker.R;
import com.software.fire.tracker.services.GeoFenceTransitionIntentService;
import com.software.fire.tracker.services.GeofenceErrorMessage;
import com.software.fire.tracker.ui.dialogs.SendOptionsDialog;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LocationSource.OnLocationChangedListener,
        OnMapReadyCallback,
        LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String GEOFENCE_ID = "geofence_id";
    private static final double RADIUS = 100;
    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private boolean mMapReady;
    private boolean mMapInitialPositionSet;

    private Marker mMarker;
    private double mLongitude;
    private double mLatitude;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendOptionsDialog sendOptionsDialog = new SendOptionsDialog();
                sendOptionsDialog.setSendOptionsManger(new SendOptionsDialog.SendOptionsManager() {
                    @Override
                    public LatLng getPosition() {
                        return new LatLng(mLatitude, mLongitude);
                    }
                });
            }
        });
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(
                    MainActivity.this,
                    "Geofence Added",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            String errorMessage = GeofenceErrorMessage.getErrorString(MainActivity.this, status.getStatusCode());
            Log.v(TAG, errorMessage);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(MainActivity.this, GeoFenceTransitionIntentService.class);
        return PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(
                        mLatitude,
                        mLongitude,
                        (float) RADIUS
                )
                .setExpirationDuration(1000000000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        );
        return builder.build();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMapReady) {
            if (mMarker != null) {
                mMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            CameraPosition target = new CameraPosition.Builder()
                    .target(new LatLng(mLatitude, mLongitude))
                    .zoom(15)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));

            if (!mMapInitialPositionSet) {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();

                if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }


                MarkerOptions markerOptions = new MarkerOptions()
                        .title("You're here")
                        .position(new LatLng(mLatitude, mLongitude));
                mMarker = mMap.addMarker(markerOptions);

                CircleOptions circleOptions = new CircleOptions()
                        .strokeWidth(2)
                        .strokeColor(Color.TRANSPARENT)
                        .radius(RADIUS)
                        .center(new LatLng(mLatitude, mLongitude))
                        .fillColor(Color.RED);
                mMap.addCircle(circleOptions);
                mMapInitialPositionSet = true;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapReady = true;
    }
}
