package com.edwin.imsgdereg.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.edwin.imsgdereg.ui.SingleLiveEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by edwinwu on 2018/3/12.
 */

public class HttpUrlConnectionApi {
    private static final String TAG = "IMSGDEREG";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static HttpUrlConnectionApi sInstance;

    private static final String POST_URL = "https://selfsolve.apple.com/deregister-imessage";

    public static final String PATH_SENDSMS = "/us/en/sendsms";
    public static final String PATH_TURNOFFIMSG = "/us/en/turnOffiMessage";
    public static final String PARA_KEY_PHONE = "phone";
    public static final String PARA_KEY_CC = "countryCode";
    public static final String PARA_KEY_TOKEN = "token";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";

    public static final String HTTP_RESP_PARAM_MESSAGECODE = "messageCode";
    public static final String HTTP_RESP_PARAM_MESSAGE     = "message";
    public static final String HTTP_RESP_PARAM_STATUS      = "status";

    private CookieManager cookieManager = new CookieManager();

    //private final MutableLiveData<ResponseData> mResponseLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<ResponseData> mResponseLiveData = new SingleLiveEvent<>();

    private HttpUrlConnectionApi() {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
    }

    public LiveData<ResponseData> getResponseData() {
        return mResponseLiveData;
    }

    public synchronized static HttpUrlConnectionApi getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new HttpUrlConnectionApi();
                Log.d(TAG, "Made new HttpUrlConnectionApi");
            }
        }
        return sInstance;
    }

    public void retrieveTokenFromNetwork(final String phone, final String countrycode,
                                         final int actionId) {
        String path = POST_URL + PATH_SENDSMS;
        String params =
                PARA_KEY_PHONE + "=" + phone +
                        HTTP_REQ_ENTITY_JOIN +
                        PARA_KEY_CC + "=" + countrycode;

        threadtoSendPost(path, params, actionId);
    }

    public void turnOffiMessageFromNetwork(final String phone, final String token,
                                                  final int actionId) {
        String path = POST_URL + PATH_TURNOFFIMSG;
        String params =
                PARA_KEY_PHONE + "=" + phone +
                        HTTP_REQ_ENTITY_JOIN +
                        PARA_KEY_TOKEN + "=" + token;

        threadtoSendPost(path, params, actionId);
    }

    private void threadtoSendPost(final String path, final String params, int actionId) {
        new Thread(() -> sendPOST(path, params, actionId)).start();
    }

    private void sendPOST(final String PATH, final String params, final int actionId){
        ResponseData rd;

        try {
            URL url = new URL(PATH);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setConnectTimeout(5000); //set timeout to 5 seconds
            con.setReadTimeout(5000);

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(params.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                rd = parseSuccessResponse(response.toString());

                rd.setActionId(actionId);
                rd.setUrl(PATH + "?" + params);
            } else {
                rd = new ResponseData();
                rd.setStatus("http error");
                rd.setMessageCode(String.valueOf(responseCode));

                rd.setActionId(actionId);
                rd.setUrl(PATH + "?" + params);
            }
        } catch (Exception e) {
            rd = new ResponseData();
            rd.setStatus("http exception");
            rd.setMessage(e.getMessage());

            rd.setActionId(actionId);
            rd.setUrl(PATH + "?" + params);
        }
        mResponseLiveData.postValue(rd);
    }


    public ResponseData parseSuccessResponse(String responseString) {
        ResponseData rd = new ResponseData();
        try {
            JSONObject json = new JSONObject(responseString);
            if (json != null) {
                if (json.has(HTTP_RESP_PARAM_STATUS))
                    rd.setStatus(json.getString(HTTP_RESP_PARAM_STATUS));
                if (json.has(HTTP_RESP_PARAM_MESSAGECODE))
                    rd.setMessageCode(json.getString(HTTP_RESP_PARAM_MESSAGECODE));
                if (json.has(HTTP_RESP_PARAM_MESSAGE))
                    rd.setMessage(json.getString(HTTP_RESP_PARAM_MESSAGE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            rd.setStatus("json error");
            rd.setMessage(e.getMessage());
        }
        return rd;
    }

}
