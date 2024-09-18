package com.example.quranrecitation.ui.fragment.cnn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.quranrecitation.R
import com.example.quranrecitation.data.factory.ViewModelFactory
import com.example.quranrecitation.data.response.BaseResponse
import com.example.quranrecitation.data.response.ResponsePrediksi
import com.example.quranrecitation.databinding.FragmentCnnBinding
import com.example.quranrecitation.feature.Timer
import com.example.quranrecitation.room.AppDatabase
import com.example.quranrecitation.room.AudioRecord
import com.example.quranrecitation.ui.activity.ResultActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val REQUEST_CODE = 200

class CnnFragment : Fragment(), Timer.OnTimerTickListener {

    private var _binding: FragmentCnnBinding? = null
    private lateinit var player: ExoPlayer
    private var duration: Int = 0
    private var Duration = ""
    private lateinit var handler: Handler
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var filePath = ""
    private var isRecording = false
    private var isPause = false

    private lateinit var buttonRecord: ImageButton
    private lateinit var buttonDone: ImageButton
    private lateinit var buttonDelete: ImageButton

    private lateinit var vibrator: Vibrator
    private lateinit var timer: Timer
    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var db: AppDatabase

    private lateinit var reciteViewModel: CnnViewModel
    private lateinit var responsePrediksi: ResponsePrediksi
    private lateinit var audioRecord: AudioRecord

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCnnBinding.inflate(inflater, container, false)
        val root: View = binding.root

        reciteViewModel = obtainViewModel()

        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        timer = Timer(this)
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        player = SimpleExoPlayer.Builder(requireContext()).build()
        player.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (playWhenReady) {
                            binding.ButtonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
                        } else {
                            binding.ButtonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        }
                    }

                    Player.STATE_ENDED -> {
                        player.playWhenReady = false
                        player.seekTo(0)
                        binding.ButtonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        binding.seekbar.progress = 0
                        binding.time.text = "0:00 / " + getTimeString(duration)
                    }

                    else -> {
                        binding.ButtonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    duration = player.duration.toInt() / 1000
                    binding.seekbar.max = duration
                    binding.time.text = "0:00 / " + getTimeString(duration)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        })

        val mediaItem =
            MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/quran-recitation-4832f.appspot.com/o/5_109.mp3?alt=media&token=0ec2736c-ed27-4b6d-bd7d-2f46312cde4b")
        player.setMediaItem(mediaItem)
        player.prepare()

        binding.ButtonPlayExample.setOnClickListener {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
                player.playWhenReady = true
            } else {
                player.playWhenReady = !player.playWhenReady
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo((progress * 1000).toLong())
                    binding.time.text = getTimeString(progress) + "/" + getTimeString(duration)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val currentPosition = player.currentPosition.toInt() / 1000
                binding.seekbar.progress = currentPosition
                binding.time.text = getTimeString(currentPosition) + "/" + getTimeString(duration)
                handler.postDelayed(this, 1000)
            }
        })

        player.repeatMode = Player.REPEAT_MODE_OFF

        buttonRecord = binding.ButtonRecord
        buttonDone = binding.ButtonDone
        buttonDelete = binding.ButtonDelete

        binding.ButtonRecord.setOnClickListener {
            setAnimation()

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionGranted = true

                when {
                    isPause -> resumeRecording()
                    isRecording -> pauseRecording()
                    else -> startRecording()
                }
            } else {
                requestPermissions(permissions, REQUEST_CODE)
            }

            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        binding.ButtonDone.setOnClickListener {
            stopRecording()

            binding.progressBar.visibility = View.VISIBLE
            prepareUploadSuara()

//            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            showBottomSheet()
        }

        binding.ButtonDelete.setOnClickListener {
            stopRecording()
            File(filePath).delete()

            Toast.makeText(requireContext(), "Recorder deleted", Toast.LENGTH_SHORT).show()
        }

        buttonDelete.isClickable = false
        buttonDone.isClickable = false

        uploadSuaraProcess()

        return root
    }

    private fun prepareUploadSuara() {
        val requestSoundFile = File(filePath).asRequestBody("audio/wav".toMediaTypeOrNull())

        val soundMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            filePath,
            requestSoundFile
        )

        reciteViewModel.uploadSuara(soundMultipart)
    }

    private fun uploadSuaraProcess() {
        reciteViewModel.resultUploadSuara.observe(viewLifecycleOwner) {
            when (it) {
                is BaseResponse.Success<*> -> {
                    responsePrediksi = it.data as ResponsePrediksi
                    binding.progressBar.visibility = View.GONE
                    save()
                    toHasilPrediksi(responsePrediksi)
                    Toast.makeText(requireContext(), "Berhasil upload suara.", Toast.LENGTH_SHORT)
                        .show()
                }

                is BaseResponse.Error -> {
                    Toast.makeText(
                        requireContext(),
                        it.exception.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE
                }

                is BaseResponse.Loading -> {
                    if (it.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun toHasilPrediksi(responsePrediksi: ResponsePrediksi) {
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("response_prediksi", responsePrediksi)
            putExtra("audio_record", audioRecord)
        }
        startActivity(intent)
    }

    private fun save() {
        var timeStamp = Date().time
        var ampsPath = "$dirPath$fileName"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (e: IOException) {
        }

        audioRecord = AudioRecord(fileName, filePath, timeStamp, Duration, ampsPath)

        GlobalScope.launch {
            db.audioRecordDao().insert(audioRecord)
        }

        Toast.makeText(requireContext(), "Recorder saved!", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard(view: View) {
        //imm = input method meter
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun stopRecording() {
        timer.stop()

        recorder.apply {
            stop()
            release()
        }

        isPause = false
        isRecording = false

        buttonDelete.isClickable = false
        buttonDone.isClickable = false

        buttonRecord.setImageResource(R.drawable.ic_mic)
        buttonRecord.clearAnimation()

        binding.tvTimer.text = "00:00:00"
        amplitudes = binding.waveFormView.clear()
    }

    private fun pauseRecording() {
        recorder.pause()
        isPause = true
//        isRecording = false
        buttonRecord.setImageResource(R.drawable.ic_pause)
        buttonRecord.clearAnimation() //Hentikan animasi blink saat berhenti

        timer.pause()

        buttonDelete.isClickable = true
        buttonDone.isClickable = true
        buttonDelete.setImageResource(R.drawable.ic_cross)
    }

    private fun resumeRecording() {
        recorder.resume()
        isPause = false
//        isRecording = false
        buttonRecord.setImageResource(R.drawable.ic_mic)
        timer.start()
        setAnimation()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted =
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                startRecording()
            }
        }
    }

    private fun startRecording() {
        //start recording
        recorder = MediaRecorder()
        dirPath = "${requireContext().externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh.mm.ss", Locale.getDefault())
        var date = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"

        filePath = "$dirPath$fileName.mp3"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)

            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }

        isRecording = true
        isPause = false

        timer.start()

        buttonDelete.isClickable = true
        buttonDone.isClickable = true
        buttonDelete.setImageResource(R.drawable.ic_cross)
        setAnimation()
    }

    private fun setAnimation() {
        val blink = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
        buttonRecord.animation = blink
    }

//    private fun showBottomSheet() {
//        val bottomSheetFragment = BottomSheetFragment()
//        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
//    }

    private fun getTimeString(duration: Int): String {
        val min = duration / 60
        val sec = duration % 60
        return String.format("%02d:%02d", min, sec)
    }

//    override fun onBottomSheetExit() {
//        // Reset the state when BottomSheet exits
//        stopRecording()
//    }

    override fun onTimerTick(duration: String) {
//        println(duration)
        binding.tvTimer.text = duration
        this.Duration = duration.dropLast(3)
        binding.waveFormView.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        player.release()
        handler.removeCallbacksAndMessages(null)
    }

    private fun obtainViewModel(): CnnViewModel {
        val factory = ViewModelFactory.getInstance(requireActivity().application)
        return ViewModelProvider(requireActivity(), factory)[CnnViewModel::class.java]
    }
}
