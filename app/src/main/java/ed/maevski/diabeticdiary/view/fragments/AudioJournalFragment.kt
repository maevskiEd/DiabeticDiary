package ed.maevski.diabeticdiary.view.fragments

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ed.maevski.diabeticdiary.R
import ed.maevski.diabeticdiary.data.entity.Audiofile
import ed.maevski.diabeticdiary.data.entity.Audiofile.AudioFileConst.CREATED_AUDIOFILE
import ed.maevski.diabeticdiary.data.entity.Audiofile.AudioFileConst.CREATE_AUDIOFILE
import ed.maevski.diabeticdiary.databinding.FragmentAudioJournalBinding
import ed.maevski.diabeticdiary.view.rv_adapters.AudioJournalRecyclerAdapter
import ed.maevski.diabeticdiary.viewmodel.AudioJournalFragmentViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class AudioJournalFragment : Fragment() {
    private var _binding: FragmentAudioJournalBinding? = null
    private val binding get() = _binding!!

    private val audioJournalFragment: AudioJournalFragmentViewModel by viewModels()
    val TAG = "myLogs"
    var isReading = false
    val scope = viewLifecycleOwner.lifecycleScope

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Получаем список при транзакции фрагмента
        val adapter = AudioJournalRecyclerAdapter()

        adapter.items = fetchAudioFiles()
        binding.audioJournalRecycler.adapter = adapter

        //Проверяем разрешение на запись аудио, на запись файла и запрашываем разршение у пользователя
        if (checkAndRequestPermissions()) audioJournalFragment.createAudioRecorder()

        binding.fabVoice.setOnClickListener {
            if (isReading) {
                // Остановка записи
                Log.d(TAG, "Остановка записи")

                recordStop()
            } else {
                // Начало записи

                //Еще раз проверяем разрешения. Вдруг пользователь при открытии фрагмента не предоставил все разрешения.
                if (!checkAndRequestPermissions()) {
                    //Если нет, то выходим из слушателя
                    Log.d(TAG, "Если нет, то запрашиваем и выходим из метода")
                    return@setOnClickListener
                }

                Log.d(TAG, "Начинаем запись звука")
                recordStart()
            }
        }
    }

    fun fetchAudioFiles(): List<Audiofile>? {
        val documentsDirectory =
            Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOCUMENTS}/ddiary")

        val audioFiles =
            documentsDirectory.listFiles { file -> file.isFile && file.name.endsWith(".raw") }

        Log.d(TAG, "audioFiles: ${audioFiles?.joinToString()}")
        Log.d(TAG, "audioFiles.size: ${audioFiles?.size}")

        val audioFileList = audioFiles?.map { file ->
            Audiofile(file.name, file.absolutePath, CREATED_AUDIOFILE)
        }

        Log.d(TAG, "audioFileList: ${audioFileList?.joinToString()}")

        return audioFileList
    }

    /*    fun fetchAudioFilesFromMediaStore(context: Context): List<Audiofile>{
            val audiofiles = mutableListOf<Audiofile>()

            val resolver = requireContext().contentResolver
            val projection = arrayOf(
                _ID,
                TITLE,
                DATA
            )

            val selection = "$DATA like ?"
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_DOCUMENTS}%")

            val sortOrder = "$TITLE ASC"

            val queryUri = EXTERNAL_CONTENT_URI

            resolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(_ID)
                val titleColumn = cursor.getColumnIndexOrThrow(TITLE)
                val dataColumn = cursor.getColumnIndexOrThrow(DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val data = cursor.getString(dataColumn)

                    val file = File(data)
                    if (file.exists()) {
                        audiofiles.add(Audiofile(id, title, data))
                    } else {
                        Log.d("AudioJournalAdapter", "File not found: $data")
                    }
                }
            }

            return audiofiles
        }*/

/*    @SuppressLint("MissingPermission")
    fun createAudioRecorder() {
        Log.d(TAG, "Функция: createAudioRecorder()")

        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minInternalBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig, audioFormat
        )
        val internalBufferSize = minInternalBufferSize * 4
        Log.d(
            TAG, "minInternalBufferSize = " + minInternalBufferSize
                    + ", internalBufferSize = " + internalBufferSize
                    + ", myBufferSize = " + myBufferSize
        )

        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(internalBufferSize)
            .build()
    }*/

    private fun recordStart(): Boolean {
        Log.d(TAG, "record start")

        //Проверяем наличие микрофона в системе
        return if (context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) == true) {
            Log.d(TAG, "микрофон есть в системе.")

            Thread(Runnable {
                if (audioJournalFragment.audioRecord == null) return@Runnable
                val myBuffer = ByteArray(audioJournalFragment.myBufferSize)
                var readCount = 0
                var totalCount = 0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            "${System.currentTimeMillis()}_ddiary.raw"
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOCUMENTS}/ddiary"
                        )
                    }

                    val resolver = requireContext().contentResolver
                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val item = resolver.insert(collection, values)

                    val audioFile = Audiofile(
                        values.getAsString(MediaStore.MediaColumns.DISPLAY_NAME),
                        values.getAsString(MediaStore.MediaColumns.RELATIVE_PATH),
                        CREATE_AUDIOFILE
                    )

                    Log.d(TAG, "file: ${audioFile}")

                    try {
                        audioJournalFragment.putRowAudioFileToDB(audioFile)
                    } catch (e: Exception) {
                        Log.d(TAG, "Словили исключение при записи данных")
                    }

                    item?.let { uri ->
                        val outputStream: OutputStream? = resolver.openOutputStream(uri)
                        outputStream?.use { output ->

                            while (isReading) {
                                readCount = audioJournalFragment.audioRecord!!.read(myBuffer, 0, audioJournalFragment.myBufferSize)
                                if (readCount > 0) {
                                    try {
                                        output.write(myBuffer, 0, readCount)
                                    } catch (e: IOException) {
                                        println("IOException")
                                        e.printStackTrace()
                                    }

                                    totalCount += readCount
                                    Log.d(
                                        TAG, "readCount = $readCount, totalCount = $totalCount"
                                    )
                                }
                            }
                        }


                    }

                } else {
                    val outputFilePath =
                        Environment.getExternalStorageDirectory().absolutePath + "/ddiary/${System.currentTimeMillis()}_ddiary.raw"

                    Log.d(TAG, "file: $outputFilePath")
                    val outputFile = File(outputFilePath)
                    Log.d(TAG, "outputFile: $outputFile")
                    val outputStream: FileOutputStream? = null
                    try {
                        val outputStream = FileOutputStream(outputFile)
                        Log.d(TAG, "outputStream: $outputStream")
                    } catch (e: IOException) {
                        println("IOException")
                        e.printStackTrace()
                    }

                    while (isReading) {
                        readCount = audioJournalFragment.audioRecord!!.read(myBuffer, 0, audioJournalFragment.myBufferSize)
                        if (readCount > 0) {
                            try {
                                outputStream?.write(myBuffer, 0, readCount)
                            } catch (e: IOException) {
                                println("IOException")
                                e.printStackTrace()
                            }

                            totalCount += readCount
                            Log.d(
                                TAG, "readCount = " + readCount + ", totalCount = "
                                        + totalCount
                            )
                        }
                    }
                }


            }).start()

            audioJournalFragment.audioRecord?.startRecording()
            Log.d(TAG, "recordingState = ${audioJournalFragment.audioRecord?.recordingState}")
            isReading = true
            binding.fabVoice.setImageResource(R.drawable.baseline_stop_24)

            true
        } else {
            Log.d(TAG, "микрофона в системе нет!!!")
            false
        }
    }

    private fun recordStop() {
        Log.d(TAG, "record stop")
        //при нажатии на остановку записи меняем флаг записи на false, а также меняем значок на запись звука
        isReading = false
        audioJournalFragment.audioRecord?.stop()
        binding.fabVoice.setImageResource(R.drawable.baseline_keyboard_voice_24)
    }

    private fun checkAndRequestPermissions(): Boolean {
        Log.d(TAG, "Функция: checkAndRequestPermissions()")
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
            Log.d(TAG, "разрешений нет, ждем ответа от пользователя")

            requestPermissions(
                permissions.toTypedArray(),
                RECORD_AUDIO_PERMISSION_CODE
            )
            return false
        } else {
            Log.d(TAG, "разрешения все есть")

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
            Log.d(TAG, "grantResults: ${grantResults.joinToString()}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Разрешение на запись аудио предоставлено
                    Log.d(TAG, "onRequestPermissionsResult: Все разрешения предоставлены")

                    audioJournalFragment.createAudioRecorder()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: разрешения не предоставлены")

                    // Разрешения не были предоставлены
                    Toast.makeText(
                        requireContext(),
                        "Для записи аудио сообщений дайте разрешения на запись аудио",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Все разрешения предоставлены
                    Log.d(TAG, "onRequestPermissionsResult: Все разрешения предоставлены")

                    audioJournalFragment.createAudioRecorder()

                } else {
                    Log.d(TAG, "onRequestPermissionsResult: разрешения не предоставлены")

                    // Разрешения не были предоставлены
                    Toast.makeText(
                        requireContext(),
                        "Для записи аудио сообщений дайте разрешения на запись аудио и запись файла",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    override fun onDestroyView() {
        isReading = false
        if (audioJournalFragment.audioRecord != null) {
            audioJournalFragment.audioRecord!!.release()
        }
        super.onDestroyView()
    }

    companion object {
        private val RECORD_AUDIO_PERMISSION_CODE = 123
        private val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 124
    }
}