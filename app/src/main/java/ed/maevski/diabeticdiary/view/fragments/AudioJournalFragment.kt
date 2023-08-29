package ed.maevski.diabeticdiary.view.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ed.maevski.diabeticdiary.R
import ed.maevski.diabeticdiary.databinding.FragmentAudioJournalBinding
import java.io.IOException

class AudioJournalFragment : Fragment() {
    private var _binding: FragmentAudioJournalBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null

    //Флаг для состояния: ведется запись с микрофона или нет
    private var isRecording = false

    //Добавляю этот флаг для того, чтобы при загрузке запросить разрешение на запись аудио,
    //чтобы инициализировать MediaRecorder и можно было задать источник микрофон
    //Если пользователь откажется разрешать запись, то еще раз запросим разрешение при нажатии кнопки с микрофоном
    private var isPermission = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context?.let { MediaRecorder(it) }
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        println("mediaRecorder: $mediaRecorder")

        //Проверяем разрешение на запись аудио, на запись файла и запрашываем разршение у пользователя
        if (checkAndRequestPermissions()) {
            println("Есть все разрешения")
            isPermission = true
            initOutputAudio()
        }

                if (checkPermission()) {
                    isPermission = true
                    initOutputAudio()
                }

        binding.fabVoice.setOnClickListener {
            if (isRecording) {
                // Остановка записи
                println("Остановка записи")
                recordStop()
            } else {
                // Начало записи

                //Проверяем есть ли разрешение на запись аудио с микрофона и на запись файла
                if (!checkAndRequestPermissions()) {
                    //Если нет, то запрашиваем и выходим из метода
                    println("Если нет, то запрашиваем и выходим из метода")
                    //requestPermission()
                    return@setOnClickListener
                } else {
                    if (!isPermission) {
                        initOutputAudio()
                    }
                }

                println("Начало записи")
                recordStart()
            }
        }
    }

    private fun initOutputAudio() {
        //Устанавливаем источник звука, формат и кодек
        println("Устанавливаем источник звука, формат и кодек")
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.OGG)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )
        println("checkPermission:  RECORD_AUDIO result = $result")

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            1
        )
    }

    private fun recordStart(): Boolean {
        //Проверяем наличие микрофона в системе
        return if (context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) == true) {
//            val output = Environment.getExternalStorageDirectory().absolutePath + "/ddiary/${System.currentTimeMillis()}_rec.3gp"

            println("микрофон есть в системе.")

            val output =
                Environment.getExternalStorageDirectory().absolutePath + "/${System.currentTimeMillis()}_ddiary.3gp"

            mediaRecorder?.setOutputFile(output)

            println(output)

            println("mediaRecorder: $mediaRecorder")

            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()

                //в случае успешного запуска записи меняем флаг записи на true и меняем значок на остановку записи
                isRecording = true
                binding.fabVoice.setImageResource(R.drawable.baseline_stop_24)
                Toast.makeText(context, "Recording started!", Toast.LENGTH_SHORT).show()

            } catch (e: IllegalStateException) {
                println("IllegalStateException")
                e.printStackTrace()
            } catch (e: IOException) {
                println("IOException")
                e.printStackTrace()
            }

            true
        } else {
            println("микрофона в системе нет!!!")
            false
        }
    }

    private fun recordStop() {
        mediaRecorder?.stop()
        mediaRecorder?.release()

        //при нажатии на остановку записи меняем флаг записи на false, а также меняем значок на запись звука
        isRecording = false
        binding.fabVoice.setImageResource(R.drawable.baseline_keyboard_voice_24)

    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                RECORD_AUDIO_PERMISSION_CODE
            )
            return false
        } else {
            // Уже есть все необходимые разрешения
            //recordStart()
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE || requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Все разрешения предоставлены
                //recordStart()
            } else {
                // Разрешения не были предоставлены
                Toast.makeText(
                    requireContext(),
                    "Для записи аудио сообщений дайте разрешения на запись аудио и запись файла",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        mediaRecorder = null
        super.onDestroyView()
    }

    companion object {
        private val RECORD_AUDIO_PERMISSION_CODE = 123
        private val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 124
    }
}