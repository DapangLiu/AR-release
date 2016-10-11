package edu.asu.artag.Data.Gallery_Listing;


import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class Etsy {

    private static final String API_KEY = "5bw6ztfrj4v8a77trygfq4jf";

    // Add API_KEY as part of the request URL
    private static RequestInterceptor getInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addEncodedQueryParam("api_key",API_KEY);
            }
        };
    }



    // Return an Instance of Api with EndPoint set to it
    private static Api getApi() {
        return new RestAdapter.Builder()
                .setEndpoint("https://openapi.etsy.com/v2")
                .setRequestInterceptor(getInterceptor())
                .build()
                .create(Api.class);
    }

    // Need a callback to know the status of loading
    public static void getActiveListing(Callback<ActiveListings> callback) {
        getApi().activeListings("Shop,Images", callback);
    }

}
