package org.sssj.com.ssethereums;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.ads.AdSize;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class WithdrawActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static int minBalance = 1500000;
    private static int maxBalance = 2500000;
    int WDFeesEth;
    int WDFeesCoin;


    // int WDFees;


    String Curr_Bal_URL = "http://sscoinmedia.tech/EthereumWebService/ethereumClaimTimer.php";
    String Withdraw_URL = "http://sscoinmedia.tech/EthereumWebService/ethereumAddrequest.php";

    private com.facebook.ads.AdView adView;

    RequestQueue requestQueue;
    FirebaseAuth mAuth;

    //  ProgressDialog progressDialog;
    private AlertDialog progressDialog;

    TextView txtCurrentBalance, txteth1, txteth3, txteth4, txteth5, txteth7, txteth8, txtethaddamt;
    Button btnwithRequest;
    Double currentBal;
    EditText edAmount, edEstiAmt, edEthAddr, edCoinEstiAmt, edCoinAddr;
    int WDAmt, WDEstiAmt, WDEstiAmtCoin;
    String etheAddr = "", coinbaseId = "", payMethod;
    String emailPattern;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefseditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        prefs = getSharedPreferences("startappCount", Context.MODE_PRIVATE);
        prefseditor = prefs.edit();
        prefseditor.putInt("startappCount", 1);
        prefseditor.apply();

        txtCurrentBalance = (TextView) findViewById(R.id.txtwbalance2);

        edAmount = (EditText) findViewById(R.id.edwithAmt);
        txtethaddamt = (TextView) findViewById(R.id.txtethaddamt);

        edEstiAmt = (EditText) findViewById(R.id.edwithEstimatedAmt);
        edEthAddr = (EditText) findViewById(R.id.edwithethAddress);
        txteth1 = (TextView) findViewById(R.id.txtethAdd);
        txteth3 = (TextView) findViewById(R.id.txtethAdd3);
        txteth4 = (TextView) findViewById(R.id.txtethAdd4);


        edCoinEstiAmt = (EditText) findViewById(R.id.edwithEstimatedAmtcoin);
        edCoinAddr = (EditText) findViewById(R.id.edwithethAddresscoin);
        txteth5 = (TextView) findViewById(R.id.txtethAdd5);
        txteth7 = (TextView) findViewById(R.id.txtethAdd7);
        txteth8 = (TextView) findViewById(R.id.txtethAdd8);


        // Instantiate an AdView view
        adView = new com.facebook.ads.AdView(getApplicationContext(), "2011001305879361_2011003952545763", AdSize.BANNER_HEIGHT_50);

        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();

      /*  progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");*/
        progressDialog = new SpotsDialog(this, R.style.Custom);
        progressDialog.show();
        edAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        int edamt = Integer.parseInt(edAmount.getText().toString());
                        if (edamt < currentBal && edamt >= minBalance && edamt <= maxBalance) {

                            WDAmt = Integer.parseInt(edAmount.getText().toString());
                            WDEstiAmt = WDAmt - WDFeesEth;
                            WDEstiAmtCoin = WDAmt - WDFeesCoin;
                            edCoinEstiAmt.setText(String.valueOf(WDEstiAmtCoin));
                            edEstiAmt.setText(String.valueOf(WDEstiAmt));

                        } else {
                            edAmount.setText("");
                            edEstiAmt.setText("");
                            edCoinEstiAmt.setText("");
                            if (edamt > maxBalance) {
                                Toast toast = Toast.makeText(WithdrawActivity.this, "You can request max 2500000 Gwei per request...", Toast.LENGTH_LONG);
                                // Here we can set the Gravity to Top and Right
                                toast.setGravity(Gravity.CENTER, 0, -200);
                                toast.show();
                            } else if (edamt < minBalance) {
                                Toast toast = Toast.makeText(WithdrawActivity.this, "You can request min 1500000 Gwei per request...", Toast.LENGTH_LONG);
                                // Here we can set the Gravity to Top and Right
                                toast.setGravity(Gravity.CENTER, 0, -200);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(WithdrawActivity.this, "You haven't enough balance to request...", Toast.LENGTH_LONG);
                                // Here we can set the Gravity to Top and Right
                                toast.setGravity(Gravity.CENTER, 0, -200);
                                toast.show();
                            }
                        }
                    } catch (NumberFormatException e) {
                        edAmount.setText("");
                        edCoinEstiAmt.setText("");
                        edEstiAmt.setText("");
                        Toast.makeText(WithdrawActivity.this, "Please Enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        btnwithRequest = (Button) findViewById(R.id.btnwithRequest);

        btnwithRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!payMethod.equalsIgnoreCase("Select Payment Method")) {
                    progressDialog.show();
                }

                if (payMethod.equalsIgnoreCase("ETH Address")) {

                    if (isEthereumAddress()) {
                        if (WDAmt != 0 && WDEstiAmt != 0 && !etheAddr.isEmpty()) {
                            Withdraw_Doge();
                            //Toast.makeText(WithdrawActivity.this, "You can get " + WDEstiAmt + " Doge", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(WithdrawActivity.this, "Please Filled All Fields", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(WithdrawActivity.this, "Please enter valid Ethereun Address !!! ", Toast.LENGTH_SHORT).show();
                        edEthAddr.setText("");
                    }
                }

                if (payMethod.equalsIgnoreCase("Coinbase Email")) {

                    if (isCoinbaseEmail()) {

                        if (WDAmt != 0 && WDEstiAmtCoin != 0 && !coinbaseId.isEmpty()) {
                            Withdraw_Doge();
                            //Toast.makeText(WithdrawActivity.this, "You can get " + WDEstiAmt + " Doge", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(WithdrawActivity.this, "Please Filled All Fields", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(WithdrawActivity.this, "Please enter valid Coinbase email id !!! ", Toast.LENGTH_SHORT).show();
                        edCoinAddr.setText("");
                    }
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();

        requestQueue = MySingleton.getInstance(this).getRequestQueue();

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select Payment Method");
        categories.add("ETH Address");
        categories.add("Coinbase Email");


        // Creating adapter for spinner
      //  ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

          ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
      //  dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.setDropDownViewResource(R.layout.spinner_center_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);


    }

    private boolean isEthereumAddress() {

        etheAddr = edEthAddr.getText().toString();
        // coinbaseId = edCoinAddr.getText().toString();


        etheAddr = edEthAddr.getText().toString().trim();

        String firstTwo = etheAddr.length() < 2 ? etheAddr : etheAddr.substring(0, 2);

        String validEthereum = "0x";

// onClick of button perform this simplest code.
        if (firstTwo.equals(validEthereum)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCoinbaseEmail() {

        coinbaseId = edCoinAddr.getText().toString();


        coinbaseId = edCoinAddr.getText().toString().trim();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

// onClick of button perform this simplest code.
        if (coinbaseId.matches(emailPattern)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Check_Curr_Balance();
    }

    private void Check_Curr_Balance() {
        StringRequest addStringRequest = new StringRequest(Request.Method.POST, Curr_Bal_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.i("Claim_Timer", "Response " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    currentBal = jsonObject.getDouble("ubal");
                    WDFeesEth = jsonObject.getInt("ethaddfees");
                    WDFeesCoin = jsonObject.getInt("cnbsfees");

                    txtCurrentBalance.setText(currentBal + "");
                    if (currentBal > 0) {
                        edAmount.setEnabled(true);
                        edEthAddr.setEnabled(true);
                        edCoinAddr.setEnabled(true);
                        btnwithRequest.setEnabled(true);
                    }


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Please try again... ", Toast.LENGTH_SHORT).show();
                    Log.i("Claim_Timer", " Err " + e);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("WithdrawActivity", "Error " + error);
                Toast.makeText(getApplicationContext(), "Please try again... ", Toast.LENGTH_SHORT).show();

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

    private void Withdraw_Doge() {
        StringRequest addStringRequest = new StringRequest(Request.Method.POST, Withdraw_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt("success") == 1) {
                        txtCurrentBalance.setText(jsonObject.get("newBal") + "");
                        Log.i("XXXXX", "NewBal " + jsonObject.get("newBal"));
                        currentBal = jsonObject.getDouble("newBal");
                        Toast.makeText(WithdrawActivity.this, "Request Successfully placed !!!", Toast.LENGTH_LONG).show();
                        edEthAddr.setText("");
                        edCoinAddr.setText("");
                        edAmount.setText("");
                        edEstiAmt.setText("");
                        edCoinEstiAmt.setText("");
                    }


                } catch (JSONException e) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Please try again...  " , Toast.LENGTH_SHORT).show();
                    Log.i("WithdrawActivity", " JSONException " + e);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.i("WithdrawActivity", "VolleyError" + error);
                Toast.makeText(getApplicationContext(), "Please try again... ", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("email", mAuth.getCurrentUser().getEmail());
                param.put("reqamt", WDAmt + "");
                if (payMethod.equalsIgnoreCase("ETH Address")) {
                    param.put("fees", WDFeesEth + "");
                    Log.i("WithdrawActivity", "Param " + WDFeesEth);

                }
                if (payMethod.equalsIgnoreCase("Coinbase Email")) {
                    param.put("fees", WDFeesCoin + "");
                    Log.i("WithdrawActivity", "Param2 " + WDFeesCoin);
                }
                param.put("etheaddress", etheAddr);
                param.put("coinbaseemail", coinbaseId);
                param.put("paymethod", payMethod);

                return param;
            }
        };
        requestQueue.add(addStringRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner payMethod

        payMethod = parent.getItemAtPosition(position).toString();

        if (payMethod.equalsIgnoreCase("ETH Address")) {

            edEstiAmt.setVisibility(View.VISIBLE);
            edEthAddr.setVisibility(View.VISIBLE);
            txteth1.setVisibility(View.VISIBLE);
            txteth3.setVisibility(View.VISIBLE);
            txteth4.setVisibility(View.VISIBLE);
            edAmount.setVisibility(View.VISIBLE);
            txtethaddamt.setVisibility(View.VISIBLE);

            edCoinEstiAmt.setVisibility(View.GONE);
            edCoinAddr.setVisibility(View.GONE);
            txteth5.setVisibility(View.GONE);
            txteth7.setVisibility(View.GONE);
            txteth8.setVisibility(View.GONE);
        }

        if (payMethod.equalsIgnoreCase("Coinbase Email")) {


            edEstiAmt.setVisibility(View.GONE);
            edEthAddr.setVisibility(View.GONE);
            txteth1.setVisibility(View.GONE);
            txteth3.setVisibility(View.GONE);
            txteth4.setVisibility(View.GONE);
            edAmount.setVisibility(View.VISIBLE);
            txtethaddamt.setVisibility(View.VISIBLE);

            edCoinEstiAmt.setVisibility(View.VISIBLE);
            edCoinAddr.setVisibility(View.VISIBLE);
            txteth5.setVisibility(View.VISIBLE);
            txteth7.setVisibility(View.VISIBLE);
            txteth8.setVisibility(View.VISIBLE);
        }

        // Showing selected spinner payMethod

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}
