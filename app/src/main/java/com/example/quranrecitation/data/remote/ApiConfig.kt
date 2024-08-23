package com.example.quranrecitation.data.remote

import com.example.quranrecitation.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiConfig {
    companion object {
        private const val BASE_URL = "https://salman.zaws.net"

        fun getApiService(): ApiService {
            val okhttp = OkHttpClient.Builder()
                .apply {
                    val loggingInterceptor = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    } else {
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
                    }
                    addInterceptor(loggingInterceptor)
                }
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                //.baseUrl(BuildConfig.BASE_URL)
                .baseUrl(BASE_URL)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}