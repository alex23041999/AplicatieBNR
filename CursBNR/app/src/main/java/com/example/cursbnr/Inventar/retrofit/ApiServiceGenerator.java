package com.example.cursbnr.Inventar.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//clasa cu ajutorul careia facem legatura cu FakeApi-ul creat pe site-ul FakeJson
public class ApiServiceGenerator {
    private static OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder().addHeader("App-Secret", "some-secret-key").build();
                    return chain.proceed(newRequest);
                }
            })
            .addInterceptor(new HttpInterceptor()) // Just For logging
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static final Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl("https://app.fakejson.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson));

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }

    public static Retrofit retrofit() { // For Error Handing when non-OK response is received from Server
        OkHttpClient client = new OkHttpClient.Builder().build();
        return builder.client(client).build();
    }
}
