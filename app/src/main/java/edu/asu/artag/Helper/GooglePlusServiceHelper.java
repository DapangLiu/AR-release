package edu.asu.artag.Helper;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class GooglePlusServiceHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    // The interface the sub class must implement
    public interface GMSListener {

        public void onConnected();

        public void onDisconnected();

    }

    private static final int RES_CODE_AVAILABILITY = -101;
    private static final int RES_CODE_RESOLUTION = -100;

    private GMSListener mListener;
    private Activity mActivity;
    private GoogleApiClient apiClient;

    /**
     *
     * Help checking GMS availability and Google Plus scope
     *
     * @param activity
     * @param listener
     */
    public GooglePlusServiceHelper(Activity activity, GMSListener listener) {
        this.mListener = listener;
        this.mActivity = activity;

        this.apiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API,
                        Plus.PlusOptions.builder()
                            .setServerClientId("301750894943-f7lf7s2snpn13au1sfem664drqka0co4.apps.googleusercontent.com")
                            .build())
                .build();


    }

    public void connect() {
        if (isGMSAvailable()) {
            apiClient.connect();
        } else {
            mListener.onDisconnected();
        }
    }

    public void disconnect() {
        if (isGMSAvailable()) {
            apiClient.disconnect();
        } else {
            mListener.onDisconnected();
        }
    }


    /**
     *
     * The key method of checking the availability of GMS status
     *
     * @return
     */
    private boolean isGMSAvailable() {

        GoogleApiAvailability GMS_Availability = GoogleApiAvailability.getInstance();
        int GMS_CheckResult = GMS_Availability.isGooglePlayServicesAvailable(mActivity);

        switch (GMS_CheckResult) {
            case ConnectionResult.SUCCESS:
                return true;

            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_UPDATING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                GMS_Availability.getErrorDialog(mActivity, GMS_CheckResult, RES_CODE_AVAILABILITY).show();

                // If we do not know, I make it false
                return false;

            default:
                return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mListener.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mListener.onDisconnected();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivity, RES_CODE_RESOLUTION);

            } catch (IntentSender.SendIntentException e) {
                connect();
            }
        } else {
            mListener.onDisconnected();
        }
    }


    /**
     *
     * Handle the checking result and give connection response
     *
     * @param requestCode: identical code for every request
     * @param resultCode: identical code for every result
     * @param data
     */
    public void handleActivityResult (int requestCode, int resultCode, Intent data) {

        if(requestCode == RES_CODE_RESOLUTION || requestCode == RES_CODE_AVAILABILITY) {

            if (resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                mListener.onDisconnected();
            }
        }

    }
}


