package com.edwin.imsgdereg;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.edwin.imsgdereg.model.ResponseData;
import com.edwin.imsgdereg.network.GetDeReisterService;
import com.edwin.imsgdereg.network.RetrofitInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Button getTokenButton;
    Button deRegButton;
    TextView logWindow;
    GetDeReisterService networkService;
    EditText phoneET;
    EditText countryCodeET;
    EditText tokenET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        networkService = RetrofitInstance.getRetrofitInstance().create(GetDeReisterService.class);

        // set phone number
        phoneET = (EditText) findViewById(R.id.phone);

        // set country code
        countryCodeET =(EditText) findViewById(R.id.countrycode);

        // wait sms to get token and paste here after getToken Button is pressed and succeed to get token
        tokenET =  (EditText) findViewById(R.id.token);

        // loggin window
        logWindow = (TextView)findViewById(R.id.logWindow);
        logWindow.setMovementMethod(new ScrollingMovementMethod());

        // get token button
        getTokenButton = (Button) findViewById(R.id.gettoken);
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieTokoenFromNetwork(phoneET.getText().toString(), countryCodeET.getText().toString());
            }
        });

        // deRegister iMessage button
        deRegButton = (Button) findViewById(R.id.dereg);
        deRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffiMessageFromNetwork(phoneET.getText().toString(), tokenET.getText().toString());
            }
        });

        attmptGetLine1Number();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void attmptGetLine1Number() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Read Phone State Permission")
                        .setMessage("No read phone state permission, could you please grant it?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "You can't access READ PHONE STATE in this application!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                } else
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_CODE);
            } else
                getLine1Number();
        } else
            getLine1Number();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLine1Number();
        }
    }

    private void getLine1Number() {
        String phone = "";

        try {
            TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            phone = tMgr.getLine1Number();
        } catch (SecurityException e) {
            e.printStackTrace();
            setLog("error-getLine1Number: ", e.getLocalizedMessage());
        }

        setLog("getLine1Number: ", phone);

        phoneET.setText(phone);
    }

    private void retrieTokoenFromNetwork(final String phone, final String countryCode) {
        Call<ResponseData> call = networkService.getTokenData(phone, countryCode);
        //Log.wtf("URL Called", call.request().url() + "");
        setLog("URL Called", call.request().url().toString());

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response != null) {
                    setResponseLog(response);
//                    Log.i("token", "message=" + response.body().getMessage() +
//                            " messageCode=" + response.body().getMessageCode() +
//                            " status=" + response.body().getStatus());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                setLog("error", t.getLocalizedMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void turnOffiMessageFromNetwork(final String phone, final String token) {
        Call<ResponseData> call = networkService.getTurnOffiMessageResult(phone, token);
        //Log.wtf("URL Called", call.request().url() + "");
        setLog("URL Called", call.request().url().toString());

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response != null) {
                    setResponseLog(response);
//                    Log.i("token", "message=" + response.body().getMessage() +
//                            " messageCode=" + response.body().getMessageCode() +
//                            " status=" + response.body().getStatus());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                setLog("error", t.getLocalizedMessage());
                //Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResponseLog(final Response<ResponseData> response) {
        setLog("message", response.body().getMessage());
        setLog("messageCode", response.body().getMessageCode());
        setLog("status", response.body().getStatus());
    }

    private void setLog(final String title, final String message) {
        logWindow.append("-- " + title + " = " + message + "\r\n\r\n");
    }
}
