package com.example.quranrecitation.data.remote

import com.example.quranrecitation.data.response.ResponsePrediksi
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/")
    suspend fun prediksiSuara(
        @Part file: MultipartBody.Part
    ): ResponsePrediksi
}