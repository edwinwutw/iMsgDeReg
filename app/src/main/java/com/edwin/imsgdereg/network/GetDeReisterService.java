package com.edwin.imsgdereg.network;

import com.edwin.imsgdereg.model.ResponseData;
import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetDeReisterService {
    @POST("sendsms")
    Call<ResponseData> getTokenData(@Header("Cookie") String userCookie,
                                    @Query("phone") String phoneNo,
                                    @Query("countryCode") String countryCode);

    @POST("turnOffiMessage")
    Call<ResponseData> getTurnOffiMessageResult(@Header("Cookie") String userCookie,
                                                @Query("phone") String phoneNo, @Query("token") String token);
}
