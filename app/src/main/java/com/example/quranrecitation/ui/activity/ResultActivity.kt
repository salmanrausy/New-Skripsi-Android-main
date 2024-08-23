package com.example.quranrecitation.ui.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.widget.Toast
import com.example.quranrecitation.R
import com.example.quranrecitation.data.response.ResponsePrediksi
import com.example.quranrecitation.databinding.ActivityResultBinding
import com.example.quranrecitation.room.AudioRecord

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private var responsePrediksi: ResponsePrediksi? = null
    private var audioRecord: AudioRecord? = null

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private val mediaPlayer by lazy {
        MediaPlayer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getParcelableExtra<Parcelable>("response_prediksi") != null)
            responsePrediksi = intent.getParcelableExtra("response_prediksi")

        if (intent.getParcelableExtra<Parcelable>("audio_record") != null)
            audioRecord = intent.getParcelableExtra("audio_record")

        binding.TextHasilDeskripsi.text =
            "Berdasarkan suara yang diupload, hasil prediksi Anda adalah ${responsePrediksi?.predictions}, dengan akurasi ${responsePrediksi?.confidence}"

        binding.ButtonPlayExample.setOnClickListener {
            if (audioRecord?.filePath?.isNotEmpty() == true) {
                if (!mediaPlayer.isPlaying) {
                    startPlaying()
                } else {
                    stopPlaying()
                }
            } else {
                Toast.makeText(this, "Ga ada audio yang diputar.", Toast.LENGTH_SHORT).show()
            }
        }

        preparePlayingSound()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun preparePlayingSound() {
        mediaPlayer.apply {
            setDataSource(audioRecord!!.filePath)
            prepare()

            binding.seekbar.max = duration
        }
        binding.seekbar.setOnTouchListener { _, _ -> true }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekbar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 0)
        }

        mediaPlayer.setOnCompletionListener {
            binding.ButtonPlayExample.setImageResource(R.drawable.ic_play)
            handler.removeCallbacks(runnable)
        }
    }

    private fun startPlaying() {
        mediaPlayer.start()
        binding.ButtonPlayExample.setImageResource(R.drawable.ic_pause)
        handler.postDelayed(runnable, 0)
    }

    private fun stopPlaying() {
        mediaPlayer.stop()
        binding.ButtonPlayExample.setImageResource(R.drawable.ic_play)
        handler.removeCallbacks(runnable)
    }
}