package com.edwin.imsgdereg.ui;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.edwin.imsgdereg.R;
import com.edwin.imsgdereg.countryCode.CountryCode;
import com.edwin.imsgdereg.network.HttpUrlConnectionApi;
import com.edwin.imsgdereg.sms.OnParsingCompleted;
import com.edwin.imsgdereg.sms.SmsReceiver;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.provider.MediaStore.Files.FileColumns.PARENT;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "IMSGDEREG";

    EditText phoneET;
    Spinner ccSpinner;
    CountryCodeAdapter ccAdapter;
    Button getTokenButton;

    EditText tokenET;
    Button deRegButton;

    TextView logWindow;

    boolean sendSMSStatusOK;

    TelephonyManager telephonyManager;

    private static final int HTPP_RET_GETTOKEN = 1;
    private static final int HTPP_RET_TURNOFF = 2;

    private static final String SENDSMS_STATUS_OK = "OK";
    private static final String TURNOFF_STATUS_SUCCESS = "SUCCESS";

    private SmsReceiver mSmsReceiver = null;

    class SmsParsingCallback implements OnParsingCompleted {

        EditText mTokenET;
        public SmsParsingCallback(EditText edit){
            mTokenET = edit;
        }

        @Override
        public void onParsingComplete(String code) {
            // do something with result here!
            mTokenET.setText(code);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView desc = (TextView)findViewById(R.id.desc);
        desc.setLongClickable(true);
        desc.setOnLongClickListener(v -> {
            if (logWindow.getVisibility() != View.VISIBLE)
                logWindow.setVisibility(View.VISIBLE);
            else
                logWindow.setVisibility(View.INVISIBLE);
            return true;
        });

        // log window
        logWindow = (TextView)findViewById(R.id.logWindow);
        logWindow.setMovementMethod(new ScrollingMovementMethod());
        // scroll log window in scrollview issue solved
        logWindow.setOnTouchListener((v, event) -> {
            Log.v(TAG, "child scroll touch");
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        ScrollView parentScroll=(ScrollView)findViewById(R.id.container_main);
        parentScroll.setOnTouchListener((v, event) -> {
            Log.v(TAG, "parent scroll touch");
            logWindow.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

        // set phone number
        phoneET = (EditText) findViewById(R.id.phone);

        // list country code
        ccSpinner = (Spinner) findViewById(R.id.countryCode);
        ccAdapter = new CountryCodeAdapter(this,
                android.R.layout.simple_spinner_item,
                android.R.layout.simple_spinner_dropdown_item);
        ccSpinner.setAdapter(ccAdapter);
        ccSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ccAdapter.setSelected(position);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set default isoCode based on SimCountryIso
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String isoCode = telephonyManager.getSimCountryIso().toLowerCase(Locale.US);
        if (isoCode == null || isoCode.isEmpty()) {
            setLog("getSimCountryIso is null");
            String locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = getResources().getConfiguration().getLocales().get(0).getCountry();
            } else {
                locale = getResources().getConfiguration().locale.getCountry();
            }
            isoCode = locale.toLowerCase();
        }
        if (isoCode == null || isoCode.isEmpty()) {
            setLog("getLocales is null, set default as us");
            isoCode = "us";
        }
        int countrycode = CountryCode.COUNTRYCODE_MAP.get(isoCode);
        CountryCode cc = new CountryCode(countrycode, isoCode);
        ccSpinner.setSelection(ccAdapter.getPositionForId(cc));

        // wait sms to get token and paste here after getToken Button is pressed and succeed to get token
        tokenET =  (EditText) findViewById(R.id.token);

        // get token button
        sendSMSStatusOK = false;
        getTokenButton = (Button) findViewById(R.id.gettoken);
        getTokenButton.setOnClickListener(v -> {

            // no network to execute HTTP
            if(checkNetworkAvailable() == false) {
                showDialog(MainActivity.this, false, "Please turn on Wi-Fi or Mobile data and try again");
                return;
            }

            InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(phoneET.getWindowToken(), 0);

            sendSMSStatusOK = false;
            getTokenButton.setEnabled(false);
            CountryCode cc1 = (CountryCode)ccSpinner.getSelectedItem();
            String phoneno = cc1.countryCode + phoneET.getText().toString();
            HttpUrlConnectionApi.getInstance().
                    retrieveTokenFromNetwork(phoneno, cc1.isoCode, HTPP_RET_GETTOKEN);
        });

        // deRegister iMessage button
        deRegButton = (Button) findViewById(R.id.dereg);
        deRegButton.setEnabled(false);
        deRegButton.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(tokenET.getWindowToken(), 0);

            CountryCode cc12 = (CountryCode)ccSpinner.getSelectedItem();
            String phoneno = cc12.countryCode + phoneET.getText().toString();
            HttpUrlConnectionApi.getInstance().
                    turnOffiMessageFromNetwork(phoneno,
                    tokenET.getText().toString(), HTPP_RET_TURNOFF);
        });

        HttpUrlConnectionApi.getInstance().getResponseData().observe(this, 
            rd -> {
                if (rd == null) return;
                switch(rd.getActionId()) {
                case HTPP_RET_GETTOKEN:
                    getTokenButton.setEnabled(true);
                    setLog("response: " + rd.toString());

                    // if send sms success, then we need to set the expires time to
                    // set sendSMSStatusOK = false
                    // turn on deRegButton button
                    if (rd.getStatus().equalsIgnoreCase(SENDSMS_STATUS_OK)) {
                        sendSMSStatusOK = true;
                        deRegButton.setEnabled(true);
                        showDialog(MainActivity.this, true,
                                rd.getMessage()/*+"("+rd.getMessageCode()+")("+rd.getStatus()+")"*/);
                    } else {
                        showDialog(MainActivity.this, false,
                                rd.getMessage()/*+"("+rd.getMessageCode()+")("+rd.getStatus()+")"*/);
                    }
                    break;
                case HTPP_RET_TURNOFF:
                    setLog("response: " + rd.toString());

                    boolean ok = rd.getStatus().equalsIgnoreCase(TURNOFF_STATUS_SUCCESS);
                    showDialog(MainActivity.this, ok,
                            rd.getMessage()/*+"("+rd.getMessageCode()+")("+rd.getStatus()+")"*/);
                    break;
                }
            }
        );

        // grant sms/phone permission for line1 number and receive sms
        checkAndGrantPermission();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mSmsReceiver != null) this.unregisterReceiver(mSmsReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int PERMISSIONS_REQUEST_CODE = 11;

    private void registerSmsReceiver () {
        if(mSmsReceiver != null)
            return;

        mSmsReceiver = new SmsReceiver(new SmsParsingCallback(tokenET));
        registerReceiver(mSmsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"), "android.permission.BROADCAST_SMS", null);

        mSmsReceiver.getResponseData().observe(this,
                rd -> {
                    if (rd == null) return;
                    setLog("smsReceiver response: " + rd.toString());
                }
        );
    }

    private boolean checkNetworkAvailable(){

        ConnectivityManager cm = null;
        TelephonyManager tm = null;

        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

// Skip if no connection, or background data disabled
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !cm.getBackgroundDataSetting()) {
            return false;
        }

// Only update if WiFi or 3G is connected and not roaming
        int netType = info.getType();
        int netSubtype = info.getSubtype();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            return info.isConnected();
        } else if (netType == ConnectivityManager.TYPE_MOBILE ) {
            if (netSubtype == TelephonyManager.NETWORK_TYPE_UMTS ||
                    netSubtype == TelephonyManager.NETWORK_TYPE_CDMA ||
                    netSubtype == TelephonyManager.NETWORK_TYPE_LTE ||
                    netSubtype == TelephonyManager.NETWORK_TYPE_GSM) {
                return info.isConnected();
            }
            return false;
        } else {
            return false;
        }

    }

    private void checkAndGrantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS}, PERMISSIONS_REQUEST_CODE);

            } else {
                getLine1Number();
                registerSmsReceiver();
            }
        } else {
            getLine1Number();
            registerSmsReceiver();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLine1Number();
                registerSmsReceiver();
            }
        }
    }

    private void getLine1Number() {
        String line1Number=null;

        try {
            line1Number = telephonyManager.getLine1Number();
        } catch (SecurityException e) {
            e.printStackTrace();
            setLog("error-getLine1Number: " + e.getLocalizedMessage());
        }

        setLog("getLine1Number: " + line1Number);

        String parsedNumber = null;
        if (line1Number != null) {
            parsedNumber = parseLine1Number(line1Number);
        }
        if (parsedNumber != null)
            phoneET.setText(parsedNumber);
    }

    private String parseLine1Number(String line1) {
        String parsedNumber = line1;
        PhoneNumberUtil phonenumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phonenumber = null;
        try {
            if (line1.startsWith("+"))
                phonenumber = phonenumberUtil.parse(line1, null);
            else
                phonenumber = phonenumberUtil.parse(line1, telephonyManager.getSimCountryIso().toUpperCase(Locale.US));
        } catch (NumberParseException e) {
            e.printStackTrace();
            setLog("parseLine1Number error: " + e.getMessage());
            phonenumber = null;
        }
        if (phonenumber != null) {
            int countrycode_util = phonenumber.getCountryCode();
            String isoCode = phonenumberUtil.getRegionCodeForCountryCode(countrycode_util).toLowerCase();
            int countrycode = CountryCode.COUNTRYCODE_MAP.get(isoCode);
            if (countrycode != countrycode_util) // if not mached, take one from map
                setLog("country not matched: " + "phonenumberUtil:"+countrycode_util + " != " +
                        "map:"+countrycode);
            CountryCode cc = new CountryCode(countrycode, isoCode);
            ccSpinner.setSelection(ccAdapter.getPositionForId(cc));
            parsedNumber = String.valueOf(phonenumber.getNationalNumber());
            setLog("nation number: " + parsedNumber);
        }

        return parsedNumber;
    }

    private void showDialog(Context context, boolean statusOK, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(/*getTitle()*/"Deregistration Status")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, (dialog, which) -> {})
                .setIcon(statusOK ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setLog(final String message) {
        logWindow.append(message + "\r\n");
        Log.d(TAG, message);
    }
}
