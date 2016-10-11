package edu.asu.artag.UI;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
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
import java.nio.ByteBuffer;
import java.util.HashMap;

import edu.asu.artag.Data.CameraPaintBoard.CameraView;
import edu.asu.artag.Data.CameraPaintBoard.PaintBoard;
import edu.asu.artag.R;

import static android.R.attr.width;



public class CollectCameraActivity extends AppCompatActivity implements FloatingProgressButton.handleFPBclick{

    private static final int REQUEST_MEDIA_PROJECTION = 66;
    private String mEmail;
    private String mTagID;

    private static final int REQ_CODE_CAMERA = 99;
    private Camera mCamera;
    private CameraView mPreview;
    private PaintBoard mPaintBoard;
    private Display mDisplay;

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
    private String mTagImageURL;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private int mScreenDensity;
    private Handler mHandler;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_camera);

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
        ft.add(R.id.activity_collect_camera,fpb_fragment);

        ft.commit();

        mMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);


        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();


        mContext = this;

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mTagID = intent.getStringExtra("tag_id");
        //mTagImageURL = intent.getStringExtra("mImageURL");

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
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }


    @Override
    public void VolleyUpload() {
        mFlag_Place = false;
        String url = "http://roblkw.com/msa/collecttag.php";
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
                            Toast.makeText(CollectCameraActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
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
                hashMap.put("tag_id",mTagID);
                hashMap.put("tag_img",encodedFinal);
                return hashMap;
            }
        };
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy
                (0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(mStringRequest);
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
                bmp.compress(Bitmap.CompressFormat.JPEG, 0, stream);
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

            if (image!=null) {
                image.close();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaProjection != null) {
                            mMediaProjection.stop();
                        }
                    }
                });
            }
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

        base64Encode();

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

    public boolean isFlag_Place() {
        return mFlag_Place;
    }

    public void setFlag_Place(boolean flag_Place) {
        this.mFlag_Place = flag_Place;
    }


}
