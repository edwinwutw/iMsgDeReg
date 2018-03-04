package com.edwin.imsgdereg;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

        phoneET = (EditText) findViewById(R.id.phone);
        countryCodeET =(EditText) findViewById(R.id.countrycode);
        tokenET =  (EditText) findViewById(R.id.token);

        logWindow = (TextView)findViewById(R.id.logWindow);
        logWindow.setMovementMethod(new ScrollingMovementMethod());

        getTokenButton = (Button) findViewById(R.id.gettoken);
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieTokoenFromNetwork(phoneET.getText().toString(), countryCodeET.getText().toString());
            }
        });

        deRegButton = (Button) findViewById(R.id.dereg);
        deRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffiMessageFromNetwork(phoneET.getText().toString(), tokenET.getText().toString());
            }
        });

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
