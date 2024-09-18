package com.example.quranrecitation.ui.fragment.cnn

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranrecitation.data.remote.ApiConfig
import com.example.quranrecitation.data.response.BaseResponse
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class CnnViewModel(private val application: Application) : ViewModel() {

    private val _resultPrediksiSuara = MutableLiveData<BaseResponse>()
    val resultUploadSuara: LiveData<BaseResponse> = _resultPrediksiSuara

    fun uploadSuara(soundMultipart: MultipartBody.Part) {
        viewModelScope.launch {
            if (!isInternetAvailable()) {
                _resultPrediksiSuara.value =
                    BaseResponse.Error(Throwable("Tidak ada jaringan internet, silahkan rekam suara lagi."))
                return@launch
            }

            flow {
                val response =
                    ApiConfig.getApiService().prediksiSuara(soundMultipart)
                emit(response)
            }.onStart {
                _resultPrediksiSuara.value = BaseResponse.Loading(true)
            }.onCompletion {
                _resultPrediksiSuara.value = BaseResponse.Loading(false)
            }.catch {
                Log.e("Error", it.message.toString())
                it.printStackTrace()
                _resultPrediksiSuara.value = BaseResponse.Error(it)
            }.collect {
                _resultPrediksiSuara.value = BaseResponse.Success(it)
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

//    private val _textExampleAudio = MutableLiveData<String>().apply {
//        value = "Tekan tombol Putar untuk memutar contoh suara orang mengaji"
//    }

//    private val _textRecordRecite = MutableLiveData<String>().apply {
//        value = "Tekan tombol mikrofon dan lantunkan ayat di atas"
//    }
//
//    val textRecordRecite: LiveData<String> = _textRecordRecite

//    val textExampleAudio: LiveData<String> = _textExampleAudio
}