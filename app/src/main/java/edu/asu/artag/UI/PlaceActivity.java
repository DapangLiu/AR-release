package edu.asu.artag.UI;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import edu.asu.artag.Data.CameraPaintBoard.CameraView;
import edu.asu.artag.Data.CameraPaintBoard.PaintBoard;
import edu.asu.artag.R;

public class PlaceActivity extends AppCompatActivity implements FloatingProgressButton.handleFPBclick {

    private static final int REQ_CODE_CAMERA = 99;
    private Camera mCamera;
    private CameraView mPreview;
    private PaintBoard mPaintBoard;

    private File file;
    private Double mLoc_long;
    private Double mLoc_lat;
    private Double mOrient_azimuth;
    private Double mOrient_altitude;
    private String mEncoded;
    private String mEmail;
    private String encodedFinal;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;




    public boolean mFlag_Place;
    private FragmentManager mFragmentManager;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);


        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        preview.addView(mPreview);

        mPaintBoard = (PaintBoard) findViewById(R.id.paint_board);

        FloatingProgressButton fpb_fragment = new FloatingProgressButton();
        mFragmentManager = getFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.activity_place,fpb_fragment);

        ft.commit();


        mContext = this;

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mLoc_long = intent.getDoubleExtra("loc_long",0.00);
        mLoc_lat = intent.getDoubleExtra("loc_lat",0.00);
        mOrient_azimuth = intent.getDoubleExtra("orient_azimuth",0.00);
        mOrient_altitude = intent.getDoubleExtra("orient_altitude",0.00);

//
    }

    /** A safe way to get an instance of the Camera object. */
    static Camera getCameraInstance(){

        Camera c = null;
        try {

            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("error!","camera");
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    protected void onPause() {
        super.onPause();
                                      // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }



    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        mPreview.getHolder().removeCallback(mPreview); // release the callback
    }



    @Override
    public void VolleyUpload() {
        mFlag_Place = false;
        String url = "http://roblkw.com/msa/placetag.php";
        mContext = this;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);
                        Log.d("Placetag",response);
                        if (response.equals("0")){
                            mFlag_Place = true;
                            Toast.makeText(PlaceActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("请求错误:" + error.toString());
                setFlag_Place(false);
            }
        }) {
            // 携带参数
            @Override
            protected HashMap<String, String> getParams()
                    throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();

                Log.d("c",encodedFinal);

                hashMap.put("email", mEmail);
                hashMap.put("loc_long", mLoc_long.toString());
                hashMap.put("loc_lat", mLoc_lat.toString());
                hashMap.put("tag_img",encodedFinal);
                hashMap.put("orient_azimuth", mOrient_azimuth.toString());
                hashMap.put("orient_altitude", mOrient_altitude.toString());
                return hashMap;
            }
        };
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy
                (0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(mStringRequest);

    }

    @Override
    public void startScreenShot() {

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
        }
        bytes = output.toByteArray();
        mEncoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

        encodedFinal = mEncoded.replaceAll(System.getProperty("line.separator"), " ");

        VolleyUpload();
    }



    /**
     * save the pic painted by user
     * @throws IOException
     */
    @Override
    public void savePic() throws IOException {
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + "pic.jpg");
        OutputStream stream = new FileOutputStream(file);
        mPaintBoard.saveBitmap(stream);
        stream.close();

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

        base64Encode();

    }

    public boolean isFlag_Place() {
        return mFlag_Place;
    }

    public void setFlag_Place(boolean flag_Place) {
        this.mFlag_Place = flag_Place;
    }

}
