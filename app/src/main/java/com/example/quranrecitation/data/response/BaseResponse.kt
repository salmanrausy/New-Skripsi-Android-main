package com.example.quranrecitation.data.response

sealed class BaseResponse {
    data class Success<out T>(val data: T) : BaseResponse()
    data class Error(val exception: Throwable) : BaseResponse()
    data class Loading(val isLoading: Boolean) : BaseResponse()
}
