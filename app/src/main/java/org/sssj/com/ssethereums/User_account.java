package org.sssj.com.ssethereums;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class User_account extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView imgUser;
    TextView nameUser, mailUser, txtMainBal, txtCurrBal, txtEthAddr, txtCoinbaseAddr;
    String personPhotoUrl;
    String MainBal = "http://sscoinmedia.tech/EthereumWebService/ethereumClaimTimer.php";
    private AdView mAdView;
    RequestQueue requestQueue;

    private AlertDialog progressDialog;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefseditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.show();

        requestQueue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();
        prefs = getSharedPreferences("startappCount", Context.MODE_PRIVATE);
        prefseditor = prefs.edit();
        prefseditor.putInt("startappCount", 1);
        prefseditor.commit();

        imgUser = (ImageView) findViewById(R.id.user_pro_image);
        nameUser = (TextView) findViewById(R.id.txtUser_pro_name);
        mailUser = (TextView) findViewById(R.id.txtUser_pro_name_mail);
        txtMainBal = (TextView) findViewById(R.id.txttotalbal2);
        txtCurrBal = (TextView) findViewById(R.id.txtcurrbal2);
        txtEthAddr = (TextView) findViewById(R.id.txteth2);
        txtCoinbaseAddr = (TextView) findViewById(R.id.txtcoinbaseemail);

        if (user != null) {
            nameUser.setText(user.getDisplayName());
            mailUser.setText(user.getEmail());
            personPhotoUrl = user.getPhotoUrl().toString();

        }


        Glide.with(getApplicationContext()).load(personPhotoUrl)
                .thumbnail(0.5f)
                .into(imgUser);
        Load_Bal();

    }

    private void Load_Bal() {
        StringRequest addStringRequest = new StringRequest(Request.Method.POST, MainBal, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.i("Claim_Timer", "Response " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    txtCurrBal.setText(jsonObject.getString("ubal"));
                    txtMainBal.setText(jsonObject.getString("total"));
                    txtEthAddr.setText(jsonObject.getString("daddr"));
                    txtCoinbaseAddr.setText(jsonObject.getString("coinbaseaddr"));


                } catch (JSONException e) {
                    progressDialog.dismiss();

                    Log.i("Claim_Timer", " Err " + e);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.i("XXXXX", "Error " + error);

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", mAuth.getCurrentUser().getEmail());
                return param;
            }
        };
        requestQueue.add(addStringRequest);
    }
}
