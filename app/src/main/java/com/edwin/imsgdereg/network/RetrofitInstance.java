package com.edwin.imsgdereg.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitInstance {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://selfsolve.apple.com/deregister-imessage/us/en/";

    private RetrofitInstance() {}

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
