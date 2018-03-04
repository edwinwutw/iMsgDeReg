package com.edwin.imsgdereg.network;

import com.edwin.imsgdereg.model.ResponseData;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetDeReisterService {
    @POST("sendsms")
    Call<ResponseData> getTokenData(@Query("phone") String phoneNo, @Query("countryCode") String countryCode);

    @POST("turnOffiMessage")
    Call<ResponseData> getTurnOffiMessageResult(@Query("phone") String phoneNo, @Query("token") String token);
}
