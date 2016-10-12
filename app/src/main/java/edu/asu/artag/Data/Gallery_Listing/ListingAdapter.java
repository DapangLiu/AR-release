package edu.asu.artag.Data.Gallery_Listing;


import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.PlusShare;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import edu.asu.artag.Helper.GooglePlusServiceHelper;
import edu.asu.artag.R;
import edu.asu.artag.UI.GalleryActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListingAdapter extends RecyclerView.Adapter<ListingHolder>
                            implements Callback<ActiveListings>,
                                        GooglePlusServiceHelper.GMSListener {

    public static final int RES_CODE_SHARE = 98;
    public static final int RES_CODE_PLUS_ONE = 99;

    private LayoutInflater mInflater;


    private ActiveListings mActiveListings;

    private GalleryActivity mActivity;

    private boolean isGMSAvailable;

    private List<String> mImageList;

    // Edit the Context to Gallery_activity, so that the adapter could have access to do UI things
    public ListingAdapter(GalleryActivity activity, List<String> imageList){

        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mImageList = imageList;

        this.isGMSAvailable = false;

    }

    public ActiveListings getActiveListings() {

        return mActiveListings;
    }

    @Override
    public ListingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListingHolder(mInflater.inflate(R.layout.listing_fragment, parent, false));
    }

    // This method defines which views would be shown
    @Override
    public void onBindViewHolder(ListingHolder holder, final int position) {
        final Listing listing = mActiveListings.results[position];
        holder.mTitleView.setText("Click Share Button to tell your friends!");
        holder.mShopView.setText("Click PlusOne Button to +1!");
        holder.mPriceView.setText(String.valueOf(position));

//        List<String> mTest = new ArrayList<String>();
//        mTest.add("http://roblkw.com/msa/tag/a.jpg");
//        mTest.add("http://roblkw.com/msa/tag/b.jpg");
//        mTest.add("http://roblkw.com/msa/tag/c.jpg");

        Log.d("ArrayList", String.valueOf(mImageList.size()));
        Log.d("ArrayList", String.valueOf(position));

        if (!((position+1 )> mImageList.size())) {
            // Use Picasso to load Image for the Holder
            Picasso.with(holder.mImageView.getContext())
                    .load(mImageList.get(position))
                    //.load(listing.Images[0].url_570xN)
                    .into(holder.mImageView);
            //.load(listing.Images[0].url_570xN)
            //.into(holder.mImageView);
        }else{
            Picasso.with(holder.mImageView.getContext())
                    .load("http://roblkw.com/msa/tag/b.jpg")
                    //.load(listing.Images[0].url_570xN)
                    .into(holder.mImageView);
        }

        // Decide whether we could show the share button and the +1 button
        if(isGMSAvailable) {
            holder.mPlusOneButton.setVisibility(View.VISIBLE);
            holder.mPlusOneButton.initialize(listing.url, RES_CODE_PLUS_ONE);
            holder.mPlusOneButton.setAnnotation(PlusOneButton.ANNOTATION_BUBBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openListingUrl = new Intent(Intent.ACTION_VIEW);
                    openListingUrl.setData(Uri.parse(mImageList.get(position)));
                    mActivity.startActivity(openListingUrl);
                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new PlusShare.Builder(mActivity)
                            .setType("text/plain")
                            .setText("Checkout this item on Etsy "+ listing.title)
                            .setContentUrl(Uri.parse(mImageList.get(position)))
                            .getIntent();

                    mActivity.startActivityForResult(intent, RES_CODE_SHARE);

                }
            });


        } else {
            holder.mPlusOneButton.setVisibility(View.GONE);

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Check out this item on Etsy " +
                    listing.title + "" + mImageList.get(position));
                    intent.setType("text/plain");

                    mActivity.startActivityForResult(Intent.createChooser(intent, "Share"), RES_CODE_SHARE);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if(mActiveListings == null) {
            return 0;
        }
        if(mActiveListings.results == null){
            return 0;
        }
        return mActiveListings.results.length;
    }

    @Override
    public void success(ActiveListings activeListings, Response response) {
        this.mActiveListings = activeListings;
        notifyDataSetChanged();
        this.mActivity.showListing();
    }


    // Need access to the gallery activity
    @Override
    public void failure(RetrofitError error) {

        this.mActivity.showError();
    }

    @Override
    public void onConnected() {

        // No matter which case, we should load the data
        if(getItemCount() == 0) {

            Etsy.getActiveListing(this);
        }

        isGMSAvailable = true;

        // Would make the view redraw
        notifyDataSetChanged();

    }

    @Override
    public void onDisconnected() {

        if(getItemCount() == 0) {

            Etsy.getActiveListing(this);
        }

        isGMSAvailable = false;

        // Would make the view redraw
        notifyDataSetChanged();


    }

}




