package edu.asu.artag.Data.Gallery_Listing;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface Api {
    @GET("/listings/active")
    void activeListings(@Query("includes") String includes,
                        Callback<ActiveListings> callback);
}
