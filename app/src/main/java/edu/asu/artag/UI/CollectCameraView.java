package edu.asu.artag.UI;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import edu.asu.artag.Data.CameraPaintBoard.CameraView;
import edu.asu.artag.Data.CameraPaintBoard.PaintBoard;
import edu.asu.artag.Helper.PermissionCheckHelper;
import edu.asu.artag.R;

public class CollectCameraView extends AppCompatActivity implements FloatingProgressButton.handleFPBclick,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQ_CODE_CAMERA = 99;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQ_CODE_LOCATION_PERMISSION = 2;
    private Camera mCamera;
    private CameraView mPreview;
    private PaintBoard mPaintBoard;
    private Display mDisplay;

    private String mEmail;
    private String mTagID;

    private File file;
    private String mEncoded;
    private String encodedFinal;
    private int mResultCode;
    private Intent mResultData;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    private int mWidth;
    private int mHeight;
    public boolean mFlag_Place;
    private FragmentManager mFragmentManager;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private int mScreenDensity;
    private Handler mHandler;
    private ImageView mImageView;
    private GoogleApiClient mLocationClient;

    public static String[] permission_array = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private double mAltitude;
    private double mAzimuth;
    private String mImageURL;
    private LocationListener mListener;
    private Intent mIntent;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_camera_view);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.collect_camera_view);
        preview.addView(mPreview);

        mImageView = (ImageView) findViewById(R.id.tag_preview);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mTagID = intent.getStringExtra("tag_id");
        mImageURL = intent.getStringExtra("tagImage");

        Log.d("Collect",mImageURL);
        Log.d("Collect",mTagID);
        Log.d("Collect",mEmail);

        Picasso.with(this)
                .load(mImageURL)
                .resize(400, 600)
                .into(mImageView);

        FloatingProgressButton fpb_fragment = new FloatingProgressButton();
        mFragmentManager = getFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.activity_collect_camera_view, fpb_fragment);

        ft.commit();

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationClient.connect();


        mContext = this;


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /** A safe way to get an instance of the Camera object. */
    static Camera getCameraInstance() {

        Camera c = null;
        try {

            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("error!", "camera");
        }
        return c; // returns null if camera is unavailable
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i("ScreenShot", "User cancelled");
                Toast.makeText(this, "You need grant it.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (this == null) {
                return;
            }
            Log.i("ScreenShot", "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;

            setUpMediaProjection();
            setUpVirtualDisplay();
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        mPreview.getHolder().removeCallback(mPreview); // release the callback
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Double check is needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionCheckHelper.requestPermission(this, REQ_CODE_LOCATION_PERMISSION,
                    permission_array, true);
        } else {

            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(3000);
            request.setFastestInterval(1000);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mLocationClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (ActivityCompat.checkSelfPermission(CollectCameraView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(CollectCameraView.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                PermissionCheckHelper.requestPermission(CollectCameraView.this, REQ_CODE_LOCATION_PERMISSION,
                                        permission_array, true);

                            } else {
                                Location currentLocation = LocationServices.FusedLocationApi
                                        .getLastLocation(mLocationClient);


                                mAltitude = currentLocation.getAltitude();
                                mAzimuth = currentLocation.getBearing();
                                Log.d("Altitude", String.valueOf(mAltitude));
                                Log.d("Azimuth", String.valueOf(mAzimuth));
                            }
                        }
                    });

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mLocationClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mLocationClient.disconnect();
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                savePic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
        mDisplay = getWindowManager().getDefaultDisplay();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpVirtualDisplay() {

        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        mMediaProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new CollectCameraView.ImageAvailableListener(), mHandler);
    }


    @Override
    public void VolleyUpload() {
        String url = "http://roblkw.com/msa/collecttag.php";
        mContext = this;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);
                        Log.d("CollectTag",response);
                        if (response.equals("0")){
                            Toast.makeText(CollectCameraView.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            startCollectSuccess();
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

                Log.d("Collect",mEncoded);
                Log.d("Collect",mTagID);
                Log.d("Collect",mEmail);
                hashMap.put("email", mEmail);
                hashMap.put("tag_id",mTagID);
                hashMap.put("collect_img",mEncoded);
                return hashMap;
            }
        };
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy
                (0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(mStringRequest);
    }

    private void startCollectSuccess() {
        if(mIntent == null) {
            Intent mIntent = new Intent(this, CollectSuccessView.class);
            mIntent.putExtra("ImageURL", mImageURL);
            startActivity(mIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void startScreenShot() {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),REQUEST_MEDIA_PROJECTION);
    }


    /**
     * Encoding the pic through base64
     * @throws FileNotFoundException
     */
    @Override
    public void base64Encode() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bytes = output.toByteArray();
            mEncoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

            //encodedFinal = mEncoded.replaceAll(System.getProperty("line.separator"), "");

            VolleyUpload();
        }
    }



    /**
     * save the pic painted by user
     * @throws IOException
     */



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void savePic() throws IOException {

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + "pic.jpg");

        Image image = null;
        FileOutputStream stream = null;
        Bitmap bmp = null;

        try {
            image = mImageReader.acquireLatestImage();
            if(image != null) {
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * mWidth;
                // create bitmap
                bmp = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);

                stream = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, stream);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream!=null) {
                try {
                    stream.close();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaProjection != null) {
                                mMediaProjection.stop();
                            }
                        }
                    });
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (bmp!=null) {
                bmp.recycle();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaProjection != null) {
                            mMediaProjection.stop();
                        }
                    }
                });
            }
            base64Encode();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file); //out is your output file
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }

        Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();



    }



}
