package com.example.quranrecitation.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResponsePrediksi(

    @field:SerializedName("confidence")
    val confidence: Float,

    @field:SerializedName("predictions")
    val predictions: String
) : Parcelable
