package com.example.quranrecitation.ui.fragment.cnn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.example.quranrecitation.util.uriToFile
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
    private var duration_ = ""
    private lateinit var handler: Handler
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private var getFile: File? = null

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var filePath = ""
    private var isRecording = false
    private var isPause = false

    private lateinit var buttonRecord: ImageButton
    private lateinit var buttonDone: ImageButton
    private lateinit var buttonDelete: ImageButton
    private lateinit var tvUploadFile: TextView
    private lateinit var buttonBatal: Button
    private lateinit var buttonProses: Button
    private lateinit var tvTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var buttonPlayExample: ImageButton
    private lateinit var progressBar: ProgressBar

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

        //TODO Binding all important id componenet
        tvTime = binding.time
        buttonPlayExample = binding.ButtonPlayExample
        seekBar = binding.seekbar
        buttonRecord = binding.ButtonRecord
        buttonDone = binding.ButtonDone
        buttonDelete = binding.ButtonDelete
        buttonBatal = binding.buttonBatalFile
        buttonProses = binding.buttonProsesFile
        tvUploadFile = binding.tvFileUpload
        progressBar = binding.progressBar

        //TODO Action untuk aktivitas contoh audio orang mengaji
        player = SimpleExoPlayer.Builder(requireContext()).build()
        player.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (playWhenReady) {
                            //TODO Memutar contoh audio
                            buttonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
                            buttonRecord.isClickable = false
                            tvUploadFile.isClickable = false
                        } else {
                            //TODO Stop memutar contoh audio
                            buttonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                            buttonRecord.isClickable = true
                            tvUploadFile.isClickable = true
                        }
                    }

                    Player.STATE_ENDED -> {
                        player.playWhenReady = false
                        player.seekTo(0)
                        buttonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        seekBar.progress = 0
                        tvTime.text = "0:00 / " + getTimeString(duration)
                        buttonRecord.isClickable = true
                        tvUploadFile.isClickable = true
                    }

                    else -> {
                        buttonPlayExample.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        buttonRecord.isClickable = false
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    duration = player.duration.toInt() / 1000
                    seekBar.max = duration
                    tvTime.text = "0:00 / " + getTimeString(duration)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        })

        val mediaItem =
            MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/quran-recitation-4832f.appspot.com/o/5_109.mp3?alt=media&token=0ec2736c-ed27-4b6d-bd7d-2f46312cde4b")
        player.setMediaItem(mediaItem)
        player.prepare()

        buttonPlayExample.setOnClickListener {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
                player.playWhenReady = true
//                binding.ButtonRecord.isClickable = true
            } else {
//                binding.ButtonRecord.isClickable = false
                player.playWhenReady = !player.playWhenReady
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        handler.post(
            object : Runnable {
                override fun run() {
                    val currentPosition = player.currentPosition.toInt() / 1000
                    binding.seekbar.progress = currentPosition
                    binding.time.text =
                        getTimeString(currentPosition) + "/" + getTimeString(duration)
                    handler.postDelayed(this, 1000)
                }
            },
        )

        player.repeatMode = Player.REPEAT_MODE_OFF

        //TODO action button untuk proses dari perekaman
        buttonRecord.setOnClickListener {
            binding.waveFormView.visibility = View.VISIBLE
            binding.tvFileUpload.visibility = View.GONE
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

        buttonDone.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvFileUpload.visibility = View.VISIBLE
            binding.waveFormView.visibility = View.GONE
            binding.ButtonDone.visibility = View.GONE
            binding.ButtonDelete.visibility = View.GONE

            stopRecording()
            prepareUploadSuara()
            uploadSuaraProcess()
        }

        buttonDelete.setOnClickListener {
            stopRecording()
            File(filePath).delete()

            Toast.makeText(requireContext(), "Recorder deleted", Toast.LENGTH_SHORT).show()

            binding.waveFormView.visibility = View.GONE
            binding.tvFileUpload.visibility = View.VISIBLE
            binding.ButtonDone.visibility = View.GONE
            binding.ButtonDelete.visibility = View.GONE
        }

        //TODO Action Button untuk proses suara dari file local
        buttonBatal.setOnClickListener {
            binding.linearButton1.visibility = View.GONE
            binding.tvFileName.visibility = View.GONE
            binding.tvTimer.visibility = View.VISIBLE
            binding.LinearButton.visibility = View.VISIBLE
            binding.tvFileUpload.text = getString(R.string.upload_dengan_file)
        }

        buttonProses.setOnClickListener {
            if (getFile != null) {
                // Jika file sudah dipilih dari galeri, siapkan untuk diunggah
                prepareUploadSuaraFromGallery(getFile!!)
            } else {
                // Jika tidak ada file yang dipilih, tampilkan pesan error atau lanjutkan dengan proses lain
                Toast.makeText(requireContext(), "Pilih file terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }


        tvUploadFile.setOnClickListener{
            startGallery()
        }

        binding.tvFileName.visibility = View.GONE
        buttonDelete.isClickable = false
        buttonDone.isClickable = false
        binding.waveFormView.visibility = View.GONE
        binding.linearButton1.visibility = View.GONE
        binding.ButtonDone.visibility = View.GONE
        binding.ButtonDelete.visibility = View.GONE

//        uploadSuaraProcess()

        return root
    }

    private fun prepareUploadSuaraFromGallery(selectedFile: File) {
        val requestSoundFile = selectedFile.asRequestBody("audio/*".toMediaTypeOrNull())

        val soundMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            selectedFile.name,
            requestSoundFile
        )

        reciteViewModel.uploadSuara(soundMultipart)
    }

    private fun prepareUploadFileFromGallery(file: File) {
        // Konversi file ke RequestBody dan MultipartBody
        val requestSoundFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
        val soundMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestSoundFile
        )

        // Panggil ViewModel untuk upload suara
        reciteViewModel.uploadSuara(soundMultipart)
    }


    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "audio/*"
        val chooser = Intent.createChooser(intent, "Choose audio")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedAudio = result.data?.data as Uri

            selectedAudio.let { uri ->
                activity?.let { activityContext ->
                    // Konversi Uri menjadi file
                    val myFile = uriToFile(uri, activityContext)
                    getFile = myFile
                    // Tampilkan nama file atau informasi lain yang diperlukan
                    val fileName = myFile.name
                    binding.tvFileName.visibility = View.VISIBLE
                    binding.linearButton1.visibility = View.VISIBLE
                    binding.tvFileName.text = fileName
                    binding.tvFileUpload.text = "Pilih file yang lain"
                    binding.tvTimer.visibility = View.GONE
                    binding.LinearButton.visibility = View.GONE

                    // Setelah memilih file, kita mempersiapkan upload suara
                    prepareUploadFileFromGallery(myFile)
                }
            }
        }
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
        val timeStamp = Date().time
        val ampsPath = "$dirPath$fileName"

        try {
            val fos = FileOutputStream(ampsPath)
            val out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        } catch (e: IOException) {
        }

        audioRecord = AudioRecord(fileName, filePath, timeStamp, duration_, ampsPath)

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
        binding.ButtonPlayExample.isClickable = true
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
        binding.ButtonPlayExample.isClickable = true

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
        binding.ButtonPlayExample.isClickable = false
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
        binding.ButtonPlayExample.isClickable = false
        binding.ButtonDone.visibility = View.VISIBLE
        binding.ButtonDelete.visibility = View.VISIBLE

        //start recording
        recorder = MediaRecorder()
        dirPath = "${requireContext().externalCacheDir?.absolutePath}/"

        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh.mm.ss", Locale.getDefault())
        val date = simpleDateFormat.format(Date())
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

    private fun getTimeString(duration: Int): String {
        val min = duration / 60
        val sec = duration % 60
        return String.format("%02d:%02d", min, sec)
    }

    override fun onTimerTick(duration: String) {
//        println(duration)
        binding.tvTimer.text = duration
        this.duration_ = duration.dropLast(3)
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
