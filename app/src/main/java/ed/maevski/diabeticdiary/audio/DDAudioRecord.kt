package ed.maevski.diabeticdiary.audio

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import ed.maevski.diabeticdiary.data.entity.Audiofile
import kotlinx.coroutines.channels.Channel

//class DDAudioRecorder private constructor(private val onAudioRecordedListener: (String) -> Unit) {
class DDAudioRecorder private constructor () {

    private lateinit var channel: Channel<Pair<String, String>>
    private var audioRecorder: AudioRecord? = null
    private var item: Uri? = null
    val TAG = "myLogs"


    companion object {
        // Статическая переменная для хранения единственного экземпляра
        private var instance: DDAudioRecorder? = null

        // Метод для получения экземпляра MyAudioRecorder
        fun getInstance(channel: Channel<Pair<String, String>>): DDAudioRecorder {
            if (instance == null) {
                instance = DDAudioRecorder()
                instance?.initialize(channel)
            }
            return instance!!
        }
    }

    // Инициализация объекта с передачей канала
    private fun initialize(channel: Channel<Pair<String, String>>) {
        if (!::channel.isInitialized) {
            this.channel = channel
        } else {
            // Вы можете выбрасывать исключение или обрабатывать ситуацию по-другому
            // в зависимости от вашей логики
            Log.d(TAG, "канал был инициализировани и пытается повторно инициализироваться")

        }
    }

    init {
        createAudioRecorder()
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecorder() {
        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minInternalBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig, audioFormat
        )
        val internalBufferSize = minInternalBufferSize * 4

        audioRecorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(internalBufferSize)
            .build()
    }

    // Методы для работы с записью аудио
    fun startRecording(context: Context) {
        createAudioFile(context)

        // Здесь можете добавить код для начала записи
    }

    private fun createAudioFile() {
        if (audioRecorder == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val context = context.applicationContext
            val resolver = context.contentResolver
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

            item = resolver.insert(collection, values)

            val audioFile = Audiofile(
                values.getAsString(MediaStore.MediaColumns.DISPLAY_NAME),
                values.getAsString(MediaStore.MediaColumns.RELATIVE_PATH),
                Audiofile.AudioFileConst.CREATE_AUDIOFILE
            )

            Log.d(TAG, "file: ${audioFile}")

        }
    }

    fun stopRecording() {
        // Здесь можете добавить код для остановки записи
    }

    fun release() {
        audioRecorder?.release()
    }
}