package edu.asu.artag.Data.Gallery_Listing;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class Listing implements Parcelable {

    public int listing_id;
    public String title;
    public String description;
    public String price;
    public String url;
    public edu.asu.artag.Data.Gallery_Listing.Shop Shop;
    public Image[] Images;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(listing_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(url);
        dest.writeParcelable(Shop, 0);
        dest.writeParcelableArray(Images, 0);
    }

    public static final Creator<Listing> CREATOR = new Creator<Listing>() {
        @Override
        public Listing createFromParcel(Parcel source) {
            Listing listing = new Listing();
            listing.listing_id = source.readInt();
            listing.title = source.readString();
            listing.description = source.readString();
            listing.price = source.readString();
            listing.url = source.readString();
            listing.Shop = source.readParcelable(Shop.class.getClassLoader());
            listing.Images = (Image[]) source.readParcelableArray(Image.class.getClassLoader());
            Log.d("Listing", String.valueOf(listing.Images));
            return listing;
        }

        @Override
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };
}
