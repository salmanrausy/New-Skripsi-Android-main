package com.example.quranrecitation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.quranrecitation.R

class SplashActivity : AppCompatActivity() {

    //INI CLASS UNTUK SPLASHSCREEN
    //Deklarasi variabel timer splash muncul
    private val SPLASH_TIME_OUT:Long = 3500
    private lateinit var gambar_splashScreen: ImageView
    private lateinit var gambar1: ImageView
    private lateinit var gambar2: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        gambar_splashScreen = findViewById(R.id.logoSplash)
        gambar1 = findViewById(R.id.gambar1)
        gambar2 = findViewById(R.id.gambar2)

        supportActionBar?.hide()
        setAnimation()

        //Instruksi menjalankan main screen setelah timer splash screen selesai
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }

    private fun setAnimation(){
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        gambar_splashScreen.animation = fadeIn
        gambar2.animation = fadeIn
        gambar1.animation = fadeIn

    }
}