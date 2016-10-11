package edu.asu.artag.UI;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.asu.artag.Helper.PermissionCheckHelper;
import edu.asu.artag.Helper.ToolbarActionItemTarget;
import edu.asu.artag.Helper.ViewTargets;
import edu.asu.artag.R;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "MapActivity" ;
    private static final int REQ_CODE_LOCATION_PERMISSION = 666;

    private GoogleMap mMap;

    private GoogleApiClient mLocationClient;

    private LocationListener mListener;

    private Marker mMarker;

    private final static int RES_CODE_PLAY_SERVICES = 1001;

    private boolean mLocationPermissionStatus = true;
    private boolean mMapPermissionResult = true;
    private boolean MapPermissionFlag = false;



    private double mLatitude;
    private double mLongitude;

    private double mAzimuth;
    private double mAltitude;
    private View currentLocationButton;

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    private String mEmail;

    public static String[] permission_array = {Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Before getting map fragment, check the availability of GMS

            setContentView(R.layout.activity_maps);

        // Check GMS service before loading
        if (checkMapService()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationClient.connect();

            Intent intent = getIntent();
            mEmail = intent.getStringExtra("email");

            // Loading the Floating Button Fragment
            FloatingButtonMenu savedFragment = (FloatingButtonMenu) getFragmentManager().findFragmentById(R.id.fragmentHolder);

            // If there is not savedFragment, start loading.
            if (savedFragment == null) {

                FloatingButtonMenu fbmFragment = new FloatingButtonMenu();
                FragmentManager fragmentManager = getFragmentManager();
                android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(R.id.fragmentHolder, fbmFragment);

                ft.commit();
            }

        // If GMS is not available, getting back to the Login Activity.
        } else {
            setContentView(R.layout.activity_main);
        }

    }

    // Check the Availability of Google Map Services
    private boolean checkMapService() {

        GoogleApiAvailability mApiAvailability = GoogleApiAvailability.getInstance();

        int checkResult = mApiAvailability.isGooglePlayServicesAvailable(MapsActivity.this);

        if (checkResult == ConnectionResult.SUCCESS) {
            return true;
        } else if (mApiAvailability.isUserResolvableError(checkResult)) {
            mApiAvailability.getErrorDialog(this, checkResult,
                    RES_CODE_PLAY_SERVICES).show();
        } else {
            Toast.makeText(this, R.string.status_GMS_fail, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {


            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);


        if(MapPermissionChecker() == true){

            // Add a marker in Sydney and move the camera
            LatLng tiananmen = new LatLng(39.9054895, 116.3976317);
            mMap.addMarker(new MarkerOptions().position(tiananmen).title(getString(R.string.marker_tiananmen)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(tiananmen));
        }
    }

    /**
     * Permission Check
     *
     * @return : boolean flag variable to mark current status
     */
    private boolean MapPermissionChecker() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionCheckHelper.requestPermission(this, REQ_CODE_LOCATION_PERMISSION,
                    permission_array,true);



        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            //mMap.setMyLocationEnabled(true);
            MapPermissionFlag = true;

        }

        return MapPermissionFlag;
    }


    /**
     *
     * Handle the result of permission request
     *
     * @param requestCode: Unique code to distinguish different permission request
     * @param permissions: String Array of permissions required
     * @param grantResults: int result. 1 = equal; 0 = not equal
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQ_CODE_LOCATION_PERMISSION) {
            return;
        }

        // If all permissions are granted, go ahead.
        if (PermissionCheckHelper.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) &&
                PermissionCheckHelper.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.CAMERA) &&
                PermissionCheckHelper.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                PermissionCheckHelper.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

            mMapPermissionResult = true;

        } else {
            // Display the missing permission error dialog when the fragments resume.
            mMapPermissionResult = false;
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (!mMapPermissionResult) {

            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mMapPermissionResult = true;
        }
    }

    /**
     * show the error when permission missing
     */
    private void showMissingPermissionError() {
        PermissionCheckHelper.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(this, R.string.status_map_ready, Toast.LENGTH_SHORT).show();

        mListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                CameraUpdate update = updateMapCamera(location);
                mMap.animateCamera(update);
                addMarker(location);

            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(3000);
        request.setFastestInterval(1000);


        // Double check is needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionCheckHelper.requestPermission(this, REQ_CODE_LOCATION_PERMISSION,
                    permission_array, true);
        } else {

              LocationServices.FusedLocationApi.requestLocationUpdates(
                    mLocationClient, request, mListener);

            Location currentLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mLocationClient);

            setLocationData(currentLocation);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d( TAG, getString(R.string.Status_GMS_suspended));
        mLocationClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mMapPermissionResult) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(
//                    mLocationClient, mListener
//            );
//        }
//    }




    /**
     *
     * Show the "CURRENT LOCATION" button at menu bar
     * instead of original "My Location" button from Google API
     *
     * @param item: menu button view
     */
    public void showCurrentLocation(MenuItem item) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionCheckHelper.requestPermission(this, REQ_CODE_LOCATION_PERMISSION,
                    permission_array, true);

        } else {

            Location currentLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mLocationClient);
            if (currentLocation == null) {
                Toast.makeText(this, R.string.status_try_current_location_again, Toast.LENGTH_SHORT).show();
            } else {
                CameraUpdate update = updateMapCamera(currentLocation);
                mMap.animateCamera(update);

                addMarker(currentLocation);

                setLocationData(currentLocation);


            }
        }
    }

    private void setLocationData(Location currentLocation) {
        mLatitude = currentLocation.getLatitude();
        mLongitude = currentLocation.getLongitude();
        mAltitude = currentLocation.getAltitude();
        mAzimuth = currentLocation.getBearing();
    }


    /**
     *
     * Handle the currentLocation object and return a CameraUpdate Object
     * to make the Camera focuses on the current location
     *
     * @param currentLocation
     * @return
     */
    private CameraUpdate updateMapCamera(Location currentLocation) {
        LatLng latLng = new LatLng(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );
        return CameraUpdateFactory.newLatLngZoom(
                latLng, 18
        );
    }

    /**
     *
     * Add marker onto the current location
     *
     * @param currentLocation
     */
    private void addMarker(Location currentLocation) {

        // If there is already a marker, remove it before add new
        if (mMarker != null) {

            mMarker.remove();

        }

            MarkerOptions markerOptions = new MarkerOptions()
                    .title(getString(R.string.marker_you_are_here))
                    .position(new LatLng(currentLocation.getLatitude(),
                            currentLocation.getLongitude()));
            mMarker = mMap.addMarker(markerOptions);

    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(double azimuth) {
        mAzimuth = azimuth;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }


}
