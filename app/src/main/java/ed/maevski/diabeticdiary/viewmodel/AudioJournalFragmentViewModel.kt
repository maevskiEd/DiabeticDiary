package ed.maevski.diabeticdiary.viewmodel

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import ed.maevski.diabeticdiary.App
import ed.maevski.diabeticdiary.data.entity.Audiofile
import ed.maevski.diabeticdiary.domain.DDInteractor
import javax.inject.Inject

class AudioJournalFragmentViewModel : ViewModel() {
    @Inject
    lateinit var interactor: DDInteractor
    val TAG = "myLogs"
    var myBufferSize = 8192
    var audioRecord: AudioRecord? = null
    init {
        App.instance.dagger.inject(this)
    }

    fun putRowAudioFileToDB(rowAudiofile: Audiofile) {
        interactor.putRowAudioFile(rowAudiofile)
    }

    @SuppressLint("MissingPermission")
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
    }
}