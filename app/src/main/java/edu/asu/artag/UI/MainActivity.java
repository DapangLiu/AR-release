package edu.asu.artag.UI;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;


import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import edu.asu.artag.R;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "Login Activity";
    private static final int RES_CODE_SIGN_IN = 1000;

    private GoogleApiClient mGoogleApiClient;

    private TextView m_tvStatus;
    private TextView m_tvDispName;
    private TextView m_tvEmail;
    private GoogleSignInAccount acct;

    public String Account_Email;



    // ******************
    // Activity LifeCycle
    // ******************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sign_in);
        ButterKnife.bind(this);

        m_tvStatus = (TextView)findViewById(R.id.tvStatus);
        m_tvDispName = (TextView)findViewById(R.id.tvDispName);
        m_tvEmail = (TextView)findViewById(R.id.tvEmail);


        findViewById(R.id.btnSignIn).setOnClickListener(this);
        findViewById(R.id.btnSignOut).setOnClickListener(this);
        findViewById(R.id.btnDisconnect).setOnClickListener(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;


            // 1. Create the GoogleSignInOptions object with the requestEmail option.
            // https://developers.google.com/identity/sign-in/android/sign-in
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN)) // add new scope for further permission (G+)
                    .build();

            // 2. Build the GoogleApiClient object.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            // 3. Customize sign-in button.
            SignInButton signInButton = (SignInButton) findViewById(R.id.btnSignIn);
            signInButton.setStyle(SignInButton.COLOR_LIGHT, SignInButton.SIZE_WIDE, gso.getScopeArray());



    }

    // In onStart() Method we check the GMS availability
    @Override
    protected void onStart() {
        super.onStart();


        GoogleApiAvailability GMS_Availability = GoogleApiAvailability.getInstance();
        int GMS_CheckResult = GMS_Availability.isGooglePlayServicesAvailable(this);

        if (GMS_CheckResult != ConnectionResult.SUCCESS) {

            // Would show a dialog to suggest user download GMS through Google Play
            GMS_Availability.getErrorDialog(this, GMS_CheckResult, 1).show();
        }else{
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient.isConnected()) {

            Toast.makeText(this, R.string.String_GMS_disconnect_onStop, Toast.LENGTH_SHORT).show();
            mGoogleApiClient.disconnect();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d (TAG, getString(R.string.Status_Activity_onPause));

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d (TAG, getString(R.string.Status_Activity_onResume));
    }


    // *************
    // GMS LifeCycle
    // *************

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d( TAG, getString(R.string.Status_GMS_connected));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d( TAG, getString(R.string.Status_GMS_suspended));
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d( TAG, getString(R.string.Status_GMS_connectionFailed));
    }

    // ****************************************
    // Core Function: Login through Google Plus
    // ****************************************
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                startSignIn();
                break;
            case R.id.btnSignOut:
                signOut();
                break;
            case R.id.btnDisconnect:
                disConnect();
                break;
        }
    }


    /**
     * SignIn Google Account, Callback.
     */
    private void startSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RES_CODE_SIGN_IN);
    }


    /**
     * SignOut Google Account, Refresh Account Info Views.
     */
    private void signOut() {

        // Sign the user out and update the UI
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        m_tvStatus.setText(R.string.status_notsignedin);
                        m_tvEmail.setText("");
                        m_tvDispName.setText("");
                    }
                });
    }

    /**
     * Disconnect Google Account. Users need grant permissions again.
     */
    private void disConnect() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        m_tvStatus.setText(R.string.status_notconnected);
                        m_tvEmail.setText("");
                        m_tvDispName.setText("");
                    }
                });
    }


    // Retrieve the sign-in result with getSignInResultFromIntent.
    // https://developers.google.com/identity/sign-in/android/sign-in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == RES_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInResultHandler(result);
        }
    }

    /**
     * Handle the SignIn result data. Setup Info Views.
     *
     * @param result: from onActivityResult()
     */
    private void signInResultHandler(GoogleSignInResult result) {
        if (result.isSuccess()) {
             acct = result.getSignInAccount();
            m_tvStatus.setText(R.string.status_signedin);
            try {
                m_tvDispName.setText(acct.getDisplayName());
                m_tvEmail.setText(acct.getEmail());
            }
            catch (NullPointerException e) {
                Log.d(TAG, "Error retrieving some account information");
                Toast.makeText(MainActivity.this, "Oops! Please double check your account info.", Toast.LENGTH_SHORT).show();
            }

            checkAGWALoginStatus(acct);
            Account_Email = acct.getEmail();

        }
        else {
            Status status = result.getStatus();
            int statusCode = status.getStatusCode();

            // Constant Value: 12501 (API)
            if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                m_tvStatus.setText(R.string.status_signincancelled);
            }

            // Constant Value: 12500 (API)
            else if (statusCode == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                m_tvStatus.setText(R.string.status_signinfail);
            }
            else {
                m_tvStatus.setText(R.string.status_nullresult);
            }
        }
    }

    private void checkAGWALoginStatus(final GoogleSignInAccount acct) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String AGWAurl = "http://roblkw.com/msa/login.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AGWAurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "response -> " + response);
                        Toast.makeText(MainActivity.this, R.string.status_signedin, Toast.LENGTH_SHORT).show();

                        // AGWA Sign up if not. response 0 means correct.
                        if (response.equals("0")) {
                            startMap();
                    }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("email", acct.getEmail());
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Start Map Activity
     */
    private void startMap() {
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("email",acct.getEmail());
                startActivity(intent);
    }

}
