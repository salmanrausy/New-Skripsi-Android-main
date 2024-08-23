package com.example.quranrecitation.ui.fragment.information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InformationViewModel : ViewModel() {

    private val _textTentangAplikasi = MutableLiveData<String>().apply {
        value = "Aplikasi Quran Recitation merupakan sebuah aplikasi yang dibangun untuk memenuhi project Skripsi pada tahun ajaran 2023/2024 di Institut Teknologi Perusahaan Listrik Negara (IT-PLN). Tujuan dari pembangunan aplikasi ini agar dapat membantu pengguna untuk belajar membaca Al-Quran secara mandiri yang dibantu dengan program Machine Learning yang ditanamkan ke dalam aplikasi menggunakan metode Mel Frequency Cepstral Coefficients dan Convolutional Neural Network. Harapan terbesar dari pembangunan aplikasi ini adalah agar dapat membantu pengguna sehingga mendapatkan pahala jariah hingga akhir hayat bagi seluruh pihak yang terlibat dalam pembangunan dan pengembangan aplikasi Quran Recitation berbasis Android."
    }

    val textTentangAplikasi: LiveData<String> = _textTentangAplikasi

    private val _textList1 = MutableLiveData<String>().apply {
        value = "1. Masuk ke halaman Recite Quran melalui Navigasi Bar,"
    }

    private val _textList2 = MutableLiveData<String>().apply {
        value = "2. Klik tombol record dengan ikon microphone untuk merekam suara anda dan lantunkan contoh ayat Al-Quran yang tertera. Proses perekaman ditandai dengan berubahnya warna background pada ikon microphone dari warna kuning menjadi warna merah. Ikon berwarna merah menandakan proses perekaman sedang berlangsung,"
    }

    private val _textList3 = MutableLiveData<String>().apply {
        value = "3. Klik kembali tombol record untuk mengakhiri sesi perekaman, tunggu hasil penilaian yang ditandai dengan loading process,"
    }

    private val _textList4 = MutableLiveData<String>().apply {
        value = "4. Hasil penilaian mengaji anda muncul melalui halaman fragment yang muncul beserta informasi akurasi atau ketepatan anda dalam membaca ayat Al-Quran yang tertera,"
    }

    private val _textList5 = MutableLiveData<String>().apply {
        value = "5. Jika anda ingin menyimpan hasil penilaian, klik tombol “Simpan hasil di History”. Jika penyimpanan hasil berhasil, akan muncul notifikasi,"
    }

    private val _textList6 = MutableLiveData<String>().apply {
        value = "6. Anda dapat mengakses hasil penilaian serta dapat mengulang kembali rekaman mengaji pada halaman History."
    }

    val text1: LiveData<String> = _textList1
    val text2: LiveData<String> = _textList2
    val text3: LiveData<String> = _textList3
    val text4: LiveData<String> = _textList4
    val text5: LiveData<String> = _textList5
    val text6: LiveData<String> = _textList6
}