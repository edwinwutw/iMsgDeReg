package com.edwin.imsgdereg.sms;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import com.edwin.imsgdereg.network.ResponseData;
import com.edwin.imsgdereg.ui.SingleLiveEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jerry on 3/9/18.
 */

public class SmsReceiver extends BroadcastReceiver{

    private Context mContext;
    private String mBody;
    private String mToken = null;

    private OnParsingCompleted mCallback;

    private final SingleLiveEvent<ResponseData> mResponseLiveData = new SingleLiveEvent<>();


    public SmsReceiver(OnParsingCompleted callback){
        mCallback = callback;
    }

    public LiveData<ResponseData> getResponseData() {
        return mResponseLiveData;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        ResponseData rd = new ResponseData();
        rd.setStatus("SmsReceiver:onReceive");

        if (intent.getExtras() != null) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            SmsMessage sms = messages[0];
            if (messages.length == 1 || sms.isReplace()) {
                mBody = sms.getDisplayMessageBody();
            } else {
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                mBody = bodyText.toString();
            }
            rd.setMessageCode("intent.getExtras()!=null");
            rd.setMessage("message body: " + mBody);

            mResponseLiveData.postValue(rd);


//            if(mBody != null && mBody.length() >0){
//
//                Log.d("Jerry","mBody >"+mBody);

                if(mBody != null && mBody.length() >0){

                    Log.d("Jerry","mBody >"+mBody);

                    if(mBody.contains("iMessage") ) {
                        Pattern pattern = Pattern.compile("(\\d{6})");
                        Matcher matcher = pattern.matcher(mBody);
                        String mToken = null;
                        if (matcher.find())
                            mToken = matcher.group(0);
//                        //mToken = mBody.
//                        String[] StrArray = mBody.split(" ");
//                        for (int i=0; i<StrArray.length; i++){
//                            try {
//                                Log.d("Jerry","StrArray[i] >"+StrArray[i]);
//                                int code = Integer.valueOf(StrArray[i]);
//
//                                if (code >0)
//                                    mToken = StrArray[i];
//                            } catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
                        if(mToken != null) {
                            Log.d("Jerry", "token >" + mToken);
                            mCallback.onParsingComplete(mToken);
                        }
                    }
                }

//            }

        }

    }
}
