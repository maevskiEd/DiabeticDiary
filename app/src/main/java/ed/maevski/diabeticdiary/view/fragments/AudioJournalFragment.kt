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
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import ed.maevski.diabeticdiary.R
import ed.maevski.diabeticdiary.audio.DDAudioRecorder
import ed.maevski.diabeticdiary.data.entity.Audiofile
import ed.maevski.diabeticdiary.data.entity.Audiofile.AudioFileConst.CREATED_AUDIOFILE
import ed.maevski.diabeticdiary.data.entity.Audiofile.AudioFileConst.CREATE_AUDIOFILE
import ed.maevski.diabeticdiary.databinding.FragmentAudioJournalBinding
import ed.maevski.diabeticdiary.view.rv_adapters.AudioJournalRecyclerAdapter
import ed.maevski.diabeticdiary.viewmodel.AudioJournalFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class AudioJournalFragment : Fragment() {
    private var _binding: FragmentAudioJournalBinding? = null
    private val binding get() = _binding!!

    private val audioJournalFragmentViewModel: AudioJournalFragmentViewModel by viewModels()
    val TAG = "myLogs"
    private lateinit var scope: LifecycleCoroutineScope

    private var audioRecorder: DDAudioRecorder? = null

    val channel = Channel<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView!!!")

        _binding = FragmentAudioJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated!!!")

        scope = viewLifecycleOwner.lifecycleScope

        //Получаем список при транзакции фрагмента
        val adapter = AudioJournalRecyclerAdapter()

        adapter.items = fetchAudioFiles()
        binding.audioJournalRecycler.adapter = adapter

        //Проверяем разрешение на запись аудио, на запись файла и запрашываем разршение у пользователя
        if (checkAndRequestPermissions()) {
            audioRecorder = DDAudioRecorder.getInstance()
        }

        scope.launch {
            for (element in audioJournalFragmentViewModel.isRecordingChannel) {
                if (element) {
                    binding.fabVoice.setImageResource(R.drawable.baseline_stop_24)
                    binding.fabVoice.tag = FAB_TAG_STOP
                } else {
                    binding.fabVoice.setImageResource(R.drawable.baseline_keyboard_voice_24)
                    binding.fabVoice.tag = FAB_TAG_START
                }
            }
        }

        binding.fabVoice.setOnClickListener {
            val tagValue = binding.fabVoice.tag as? String

            when (tagValue) {
                FAB_TAG_START -> {
                    //Проверяем на наличие микрофона, если он сломан то разрешения можно не спрашивать
                    //и записывать нам нечего
                    if (context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) == false) {
                        Log.d(TAG, "Отсутствует микрофон в устройстве")
                        return@setOnClickListener
                    }

                    Log.d(TAG, "Начинаем запись звука")

                    //Еще раз проверяем разрешения. Вдруг пользователь при открытии фрагмента не предоставил все разрешения.
                    if (!checkAndRequestPermissions()) {
                        //Если нет, то выходим из слушателя
                        Log.d(TAG, "Если нет, то запрашиваем и выходим из метода")
                        return@setOnClickListener
                    }

                    // Выполните логику для "start"
                    // Например, начать запись
                    binding.fabVoice.setImageResource(R.drawable.baseline_stop_24)
                    binding.fabVoice.tag = FAB_TAG_STOP

                    audioJournalFragmentViewModel.startRecording()
                    audioRecorder?.startRecording(requireContext())
                }

                FAB_TAG_STOP -> {
                    Log.d(TAG, "Остановка записи")
                    // Выполните логику для "stop"
                    // Например, остановить запись
                    binding.fabVoice.setImageResource(R.drawable.baseline_keyboard_voice_24)
                    binding.fabVoice.tag = FAB_TAG_START

                    audioJournalFragmentViewModel.stopRecording()

                }

                else -> {
                    // Если значение tag не является "start" или "stop", выполните действия по умолчанию

                    Log.d(TAG, "В tag записалось непонятно что: $tagValue")

                }
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

    private fun recordStart(): Boolean {
        Log.d(TAG, "record start")

        //Проверяем наличие микрофона в системе
        return if (context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) == true) {
            Log.d(TAG, "микрофон есть в системе.")

            scope.launch(Dispatchers.Default) {
                if (audioJournalFragmentViewModel.audioRecord == null) return@launch
                val myBuffer = ByteArray(audioJournalFragmentViewModel.myBufferSize)
                var readCount = 0
                var totalCount = 0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = requireContext().contentResolver
                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val _displayName = "${System.currentTimeMillis()}_ddiary.raw"
                    val values = ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME, _displayName
                        )
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOCUMENTS}/ddiary"
                        )
                    }

                    val item = resolver.insert(collection, values)

                    val audioFile = Audiofile(
                        values.getAsString(MediaStore.MediaColumns.DISPLAY_NAME),
                        values.getAsString(MediaStore.MediaColumns.RELATIVE_PATH),
                        CREATE_AUDIOFILE
                    )

                    Log.d(TAG, "file: ${audioFile}")

                    //Делаем запись в базу данных с именем созданного файла
                    audioJournalFragmentViewModel.putRowAudioFileToDB(audioFile)
                    /*                    launch(Dispatchers.IO) {
                                            try {
                                                audioJournalFragment.putRowAudioFileToDB(audioFile)
                                            } catch (e: Exception) {
                                                Log.d(TAG, "Словили исключение при записи данных")
                                            }
                                        }*/

                    //Пишем звук в файл
                    launch(Dispatchers.IO) {
                        item?.let { uri ->
                            val outputStream: OutputStream? = resolver.openOutputStream(uri)
                            outputStream?.use { output ->

                                while (isReading) {
                                    readCount = audioJournalFragmentViewModel.audioRecord!!.read(
                                        myBuffer,
                                        0,
                                        audioJournalFragmentViewModel.myBufferSize
                                    )
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
                    }.join()
                    delay(2000)


                    //Ждем завершения записи аудио, 3 сек и пробуем отправить файл на сервер
                    async {
                        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                        val selectionArgs = arrayOf(_displayName)
                        val projection = arrayOf(
                            MediaStore.MediaColumns.IS_PENDING,
                            MediaStore.MediaColumns.DATA
                        )
                        val cursor =
                            resolver.query(collection, projection, selection, selectionArgs, null)

                        if ((cursor != null) && cursor.moveToFirst()) {
                            val isPending =
                                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_PENDING))
                            val _path =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                            cursor.close()

                            if (isPending == 0) {
                                Log.d(
                                    TAG,
                                    "Файл существует и не открыт на запись, пробуем кинуть его на сервер"
                                )
                                Log.d(
                                    TAG, "путь: $_path"
                                )
                                // Файл существует и не открыт на запись
                                // Вы можете выполнить нужные действия здесь
                                true
                            } else {
                                Log.d(
                                    TAG, "Файл существует, но открыт на запись"
                                )
                                // Файл существует, но открыт на запись
                                // Можно выполнить необходимые действия, например, вывести сообщение об ошибке
                                false
                            }
                        } else {
                            Log.d(
                                TAG, "Файл не существует в MediaStore"
                            )
                            // Файл не существует в MediaStore
                            false
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
                        readCount = audioJournalFragmentViewModel.audioRecord!!.read(
                            myBuffer,
                            0,
                            audioJournalFragmentViewModel.myBufferSize
                        )
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
            }

            audioJournalFragmentViewModel.audioRecord?.startRecording()
            Log.d(
                TAG,
                "recordingState = ${audioJournalFragmentViewModel.audioRecord?.recordingState}"
            )
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
        audioJournalFragmentViewModel.audioRecord?.stop()
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

                    audioRecorder = DDAudioRecorder.getInstance()
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

                    audioRecorder = DDAudioRecorder.getInstance()

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
        audioRecorder?.release()

        super.onDestroyView()
    }

    companion object {
        private val RECORD_AUDIO_PERMISSION_CODE = 123
        private val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 124
        private val FAB_TAG_START = "START"
        private val FAB_TAG_STOP = "STOP"
    }
}