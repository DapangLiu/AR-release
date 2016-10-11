package edu.asu.artag.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.asu.artag.Helper.GooglePlusServiceHelper;
import edu.asu.artag.R;
import edu.asu.artag.Data.Gallery_Listing.ActiveListings;
import edu.asu.artag.Data.Gallery_Listing.ListingAdapter;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private TextView mTextView;
    private PullToRefreshView mPullToRefresh;

    public static boolean isRefreshing;

    private static final String STATE_ACTIVE_LISTING = "StateActiveListing";
    private ListingAdapter mListingAdapter;

    private List<String> mImageURL;

    private GooglePlusServiceHelper mGooglePlusServiceHelper;

    public static final int REFRESH_DELAY = 2000;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        //mProgressBar = findViewById(R.id.progressbar);
        //mTextView = (TextView) findViewById(R.id.error_view);

        // Set up RecyclerView
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        mPullToRefresh = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefresh.setRefreshing(false);
                    }
                }, REFRESH_DELAY);
            }
        });

        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");

        // showLoading by default
        showLoading();

//        if (savedInstanceState != null) {
//
//            // First check whether we have data thru the key
//            if (savedInstanceState.containsKey(STATE_ACTIVE_LISTING)) {
//
//                // If it is not the first time
//                mListingAdapter.success((ActiveListings) savedInstanceState.getParcelable(STATE_ACTIVE_LISTING), null);
//            }
//        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        mGooglePlusServiceHelper.connect();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mGooglePlusServiceHelper.disconnect();
//    }


    /**
     *
     * Handle the intent result from GMS checker
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGooglePlusServiceHelper.handleActivityResult(requestCode, resultCode, data);
        mListingAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        ActiveListings activeListings = mListingAdapter.getActiveListings();
        if(activeListings != null){
            outState.putParcelable(STATE_ACTIVE_LISTING,activeListings);

        }
        super.onSaveInstanceState(outState);
    }

    public void showLoading () {
        Toast.makeText(this, "loading", Toast.LENGTH_SHORT).show();
        //mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        VolleyGetGallery();
        //mTextView.setVisibility(View.GONE);
        mPullToRefresh.setRefreshing(true);
    }

    public void showListing () {
        Toast.makeText(this, "listing", Toast.LENGTH_SHORT).show();
        //mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        //mTextView.setVisibility(View.GONE);
        mPullToRefresh.setRefreshing(false);
    }

    public void showError() {
        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        //mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        //mTextView.setVisibility(View.VISIBLE);
        mPullToRefresh.setRefreshing(true);
    }


    public void VolleyGetGallery() {

        String url = "http://roblkw.com/msa/getgallery.php";
        mRequestQueue = Volley.newRequestQueue(this);
        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("请求结果:" + response);
                        Log.d("GetGallery",response);


                            mImageURL = Arrays.asList(response.split("\\s*,\\s*"));
                            // Because the adpter is the listener of our adapter, we need do the GMS check after the adapter is set up
                            mListingAdapter = new ListingAdapter(GalleryActivity.this, mImageURL);
                            // Set ListingAdapter as the adapter for RecyclerView.
                            mRecyclerView.setAdapter(mListingAdapter);

                            mGooglePlusServiceHelper = new GooglePlusServiceHelper(GalleryActivity.this, mListingAdapter);
                            mGooglePlusServiceHelper.connect();


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
                return hashMap;
            }
        };
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy
                (0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(mStringRequest);
    }

}
