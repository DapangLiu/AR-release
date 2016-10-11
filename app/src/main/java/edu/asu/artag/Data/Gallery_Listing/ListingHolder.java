package edu.asu.artag.Data.Gallery_Listing;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.PlusOneButton;

import edu.asu.artag.R;

public class ListingHolder extends RecyclerView.ViewHolder {

    public ImageView mImageView;
    public TextView mTitleView;
    public TextView mShopView;
    public TextView mPriceView;
    public ImageButton mImageButton;
    public PlusOneButton mPlusOneButton;


    public ListingHolder(View itemView) {
        super(itemView);

        mImageView = (ImageView) itemView.findViewById(R.id.listing_image);
        mTitleView = (TextView) itemView.findViewById(R.id.listing_title);
        mShopView = (TextView) itemView.findViewById(R.id.listing_shop_name);
        mPriceView = (TextView) itemView.findViewById(R.id.listing_price);

        mImageButton = (ImageButton) itemView.findViewById(R.id.listing_share_button);
        mPlusOneButton = (PlusOneButton) itemView.findViewById(R.id.listing_plus_one_button);

    }
}
