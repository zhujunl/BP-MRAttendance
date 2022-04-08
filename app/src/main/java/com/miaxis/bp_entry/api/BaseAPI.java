package com.miaxis.bp_entry.api;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author ZJL
 * @date 2022/4/1 11:08
 * @des
 * @updateAuthor
 * @updateDes
 */
public class BaseAPI {

    private final static Retrofit.Builder RETROFIT_BUILDER = new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static void rebuildRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        //        builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory());
        //        builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier());
        OkHttpClient okHttpClient = builder.build();
        retrofit = RETROFIT_BUILDER
                .client(okHttpClient)
                .baseUrl("http://192.168.5.104:8090/")
                .build();
    }

    protected static StaffNet getStaffNet() {
        return getRetrofit().create(StaffNet.class);
    }

}
