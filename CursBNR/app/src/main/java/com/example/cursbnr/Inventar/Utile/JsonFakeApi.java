package com.example.cursbnr.Inventar.Utile;

import androidx.annotation.Keep;

import retrofit2.Call;
import retrofit2.http.GET;
public interface JsonFakeApi {

    @GET("q/lxLELIAa?token=Hs8D_qCghmZCie47XKkg9w")
    Call<FakeApiResponse> getProduse();
}
