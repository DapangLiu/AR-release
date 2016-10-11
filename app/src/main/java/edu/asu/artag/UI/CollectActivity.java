package edu.asu.artag.UI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;

import edu.asu.artag.Helper.PermissionCheckHelper;
import edu.asu.artag.R;

import static edu.asu.artag.UI.MapsActivity.permission_array;

public class CollectActivity extends AppCompatActivity
        implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String mTagID;
    private Location mTagLocation;
    private Marker mTagMarker;
    private Marker mLastSelectedMarker;

    private Double mTagLongitude;
    private Double mTagLatitude;
    private GoogleApiClient mLocationClient;
    private Double mCurrentLongitude;
    private Double mCurrentLatitude;
    private String mImageURL;
    private Double mTagAzimuth;
    private Double mTagAltitude;
    private ImageView mTagImageView;
    private String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        if (mLastSelectedMarker != null && mLastSelectedMarker.isInfoWindowShown()) {
            // Refresh the info window when the info window's content has changed.
            mLastSelectedMarker.showInfoWindow();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        mLocationClient.connect();

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //addMarkersToMap(mTagLocation);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());


        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        enableMyLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collect, menu);
        return true;
    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionCheckHelper.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    permission_array, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    public void showNearbyTags(MenuItem item) {

        if(mCurrentLatitude == null && mCurrentLongitude == null){
            Toast.makeText(mContext, "You need press the 'show my location' button first.", Toast.LENGTH_SHORT).show();
            return;
        }else {
            VolleyPostNearTags(this);
        }
    }

    public void VolleyPostNearTags(Activity activity) {
        String url = "http://roblkw.com/msa/neartags.php";
        mContext = activity;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);

                        mTagID = Arrays.asList(response.split("\\s*,\\s*")).get(0);
                        mTagLongitude = Double.parseDouble(Arrays.asList(response.split("\\s*,\\s*")).get(1));
                        mTagLatitude = Double.parseDouble(Arrays.asList(response.split("\\s*,\\s*")).get(2));

                        Log.d("volley", String.valueOf(mTagLatitude));
                        Log.d("volley", String.valueOf(mTagLongitude));

                        if ((mTagLatitude == null)&&(mTagLongitude == null)){
                            Toast.makeText(mContext, "There is no nearby tags...", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            VolleyPostFindTag();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求错误:" + error.toString());
            }
        }) {
            // 携带参数
            @Override
            protected HashMap<String, String> getParams()
                    throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("email", mEmail);
                hashMap.put("loc_long", "-111.9373");
                hashMap.put("loc_lat", "33.4193");
                return hashMap;
            }


        };

        mRequestQueue.add(mStringRequest);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionCheckHelper.requestPermission(this, 1001,
                    permission_array, true);
        }else{

        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);

            if (currentLocation == null) {
                Toast.makeText(this, R.string.status_try_current_location_again, Toast.LENGTH_SHORT).show();
            } else {
                mCurrentLatitude = currentLocation.getLatitude();
                mCurrentLongitude = currentLocation.getLongitude();

                Log.d("latitude", "33.4193");
                Log.d("longitude", "-111.9373");
            }

        }

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionCheckHelper.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionCheckHelper.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.tag_window, null);
            mContents = getLayoutInflater().inflate(R.layout.tag_content, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            mTagImageView = ((ImageView) view.findViewById(R.id.badge));
            mTagImageView.setImageResource(R.drawable.pegman);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null) {
                SpannableString snippetText = new SpannableString(snippet);
                //snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }

    private void addMarkersToMap(double latitude, double longitude) {


        if (mTagMarker != null) {

            mTagMarker.remove();

        }

     LatLng tagLatlng = new LatLng(latitude, longitude);

            mTagMarker = mMap.addMarker(new MarkerOptions()
                .position(tagLatlng)
                .title("The Tag is #"+mTagID)
                .snippet("Azimuth:"+mTagAzimuth+"\n Altitude:"+mTagAltitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if(mTagMarker != null) {
            adjustCamera(tagLatlng);
        }
    }

    private void adjustCamera(final LatLng tagLatlng) {
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(tagLatlng)
                            .include(new LatLng(mCurrentLatitude,mCurrentLongitude))
                            .build();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                }
            });
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this,CollectCameraActivity.class);
        intent.putExtra("email", mEmail);
        intent.putExtra("tag_id",mTagID);
        intent.putExtra("tagImage",mImageURL);
        startActivity(intent);


    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        Toast.makeText(this, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

        mLastSelectedMarker = marker;

        return false;
    }

    private void VolleyPostFindTag() {

        String url = "http://roblkw.com/msa/findtag.php";
        mRequestQueue = Volley.newRequestQueue(mContext);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);

                        mImageURL = Arrays.asList(response.split("\\s*,\\s*")).get(0);
                        mTagAzimuth = Double.parseDouble(Arrays.asList(response.split("\\s*,\\s*")).get(1));
                        mTagAltitude = Double.parseDouble(Arrays.asList(response.split("\\s*,\\s*")).get(2));

                        Log.d("findtag",mImageURL);
                        Log.d("findtag", String.valueOf(mTagAltitude+mTagAzimuth));

                        addMarkersToMap(mTagLatitude, mTagLongitude);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求错误:" + error.toString());
            }
        }) {
            // 携带参数
            @Override
            protected HashMap<String, String> getParams()
                    throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("tag_id", String.valueOf(mTagID));
                return hashMap;
            }

        };

        mRequestQueue.add(mStringRequest);
    }
}
