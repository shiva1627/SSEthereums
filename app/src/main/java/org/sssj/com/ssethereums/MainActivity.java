package org.sssj.com.ssethereums;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_PHONE_STATE;


public class MainActivity extends AppCompatActivity
{
    private static final int RC_SIGN_IN = 234;
    private static final String TAG = "MainActivity";
    private FirebaseAnalytics mFirebaseAnalytics;

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    TelephonyManager telephonyManager;

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    RequestQueue requestQueue;
    ProgressDialog progressBar;
    Dialog rateusDialog;

    String URLADD = "https://sscoinmedia.000webhostapp.com/EthereumWebService/uAdd1.php";
    String Change_Dev_ID = "https://sscoinmedia.000webhostapp.com/EthereumWebService/uUpdateuserdevid1.php";


    String usergmail, username, deviceId, lastBalance;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;

    private ProgressBar spinner;
    boolean bolDevPresent = false;
    private static final String DATE_FORMAT_MM_dd_yyyy_HH_mm_ss = "MM.dd.yyyy:HH.mm.ss";
    Long currentTime, newTime, savedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cardview_dark_background));


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        currentTime = System.currentTimeMillis();

        savedTime = sharedpreferences.getLong("LastClaim", 0);
        newTime = savedTime + 600000;
        lastBalance = sharedpreferences.getString("lastBalance", "0.00");
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        permissions.add(READ_PHONE_STATE);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        progressBar = new ProgressDialog(this);
        requestQueue = MySingleton.getInstance(this).getRequestQueue();  //using singleton object
        telephonyManager = (TelephonyManager) getSystemService(Context.
                TELEPHONY_SERVICE);

        /* getDeviceId() returns the unique device ID.*/
        deviceId = telephonyManager.getDeviceId();
        /* getSubscriberId() returns the unique subscriber ID,         */

        //  String subscriberId = telephonyManager.getSubscriberId();

        //firebase google sign in
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
                spinner.setIndeterminate(true);
                spinner.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

                if (is_Internet()) {
                    signIn();
                } else {
                    Toast.makeText(MainActivity.this, "Please Check Internet Connection...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        Bundle bundle = new Bundle();
        bundle.putString("AppOpen", "1");

        /*bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");*/
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


        if (is_Internet()) {
                if (currentTime > newTime) {
                    FirebaseAuth.getInstance().signOut();
                } else {
                    if (mAuth.getCurrentUser() != null) {
                        usergmail = mAuth.getCurrentUser().getEmail();
                        Intent in = new Intent(MainActivity.this, ProfileActivity.class);
                        in.putExtra("usergmail", usergmail);
                        in.putExtra("ubal", lastBalance);
                        startActivity(in);
                        finish();
                    } else {
                        // Log.i("XXXXX", "User Null");
                    }

                }
        } else {
            finish();;
            startActivity(new Intent(MainActivity.this,Try_again.class));
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                spinner.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Google responce error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("XXX1","In Firebase sign in");

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            usergmail = user.getEmail();
                            username = user.getDisplayName();
                            Sign_in_to_next_Activity();

                        } else {
                            spinner.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void signIn() {
        Log.i("XXX1","In sign in");
        //getting the google signin intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void Sign_in_to_next_Activity() {
        StringRequest addStringRequest = new StringRequest(Request.Method.POST, URLADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                spinner.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    switch (jsonObject.getInt("success")) {
                        case 1:
                            finish();
                            Intent in = new Intent(MainActivity.this, ProfileActivity.class);
                            in.putExtra("usergmail", usergmail);
                            in.putExtra("ubal", jsonObject.get("ubal").toString());
                            startActivity(in);
                            break;
                        case 2:
                            NewDeviceFound();
                            break;
                        case 3:
                            mGoogleSignInClient.signOut();
                            break;
                    }
                } catch (JSONException e) {
                    spinner.setVisibility(View.GONE);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                spinner.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", usergmail);
                param.put("name", username);
                param.put("devid", deviceId);
                return param;
            }
        };
        requestQueue.add(addStringRequest);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    //Checking internet present or not
    public boolean is_Internet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }


    public void NewDeviceFound() {
        rateusDialog = new Dialog(this);
        rateusDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rateusDialog.setCancelable(true);
        rateusDialog.setContentView(R.layout.dialog_same_device);
        final Button btnYes = (Button) rateusDialog.findViewById(R.id.btn_dailog_yes);
        final Button btnNo = (Button) rateusDialog.findViewById(R.id.btn_dailog_no);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateDeviceID();
                rateusDialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateusDialog.dismiss();
            }
        });
        rateusDialog.show();
    }


    private void UpdateDeviceID() {
        StringRequest addStringRequest = new StringRequest(Request.Method.POST, Change_Dev_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                spinner.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    switch (jsonObject.getInt("success")) {
                        case 1:
                            Sign_in_to_next_Activity();
                    }
                } catch (JSONException e) {
                    spinner.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                spinner.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", usergmail);
                param.put("name", username);
                param.put("devid", deviceId);
                return param;
            }
        };
        requestQueue.add(addStringRequest);
    }

   /* //checking user or device already present
    private boolean Check_user_And_DeviceID() {

        StringRequest addStringRequest = new StringRequest(Request.Method.POST, URLADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                spinner.setVisibility(View.GONE);

                Log.i("XXXXX",response);
             *//*   try {
                    JSONObject jsonObject = new JSONObject(response);
                    switch (jsonObject.getInt("success")) {
                        case 2:
                            bolDevPresent = true;
                            // NewDeviceFound();
                            //   Toast.makeText(MainActivity.this, "New Device Found!!!", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            bolDevPresent = true;
                            // mGoogleSignInClient.signOut();
                            //  Toast.makeText(MainActivity.this, "New Gmail found on same device,try to Login with register Gmail account !!! ", Toast.LENGTH_LONG).show();
                            break;
                    }

                    // Log.i("XXXXX", "Res " + response);

                } catch (JSONException e) {
                    spinner.setVisibility(View.GONE);
                    Log.i(TAG, " Err " + e);

                }*//*


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                spinner.setVisibility(View.GONE);
                Log.i(TAG, "Response Error " + error);
                Toast.makeText(MainActivity.this, "Response Error  " + error, Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", "shiva.281191@gmail.com");
                param.put("name", username);
                param.put("devid", deviceId);
                return param;
            }
        };
        requestQueue.add(addStringRequest);
        return bolDevPresent;
    }*/

}
