package ed.maevski.diabeticdiary.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ed.maevski.diabeticdiary.App
import ed.maevski.diabeticdiary.data.entity.Audiofile
import ed.maevski.diabeticdiary.domain.DDInteractor
import ed.maevski.diabeticdiary.utils.S3ClientYandex
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioJournalFragmentViewModel : ViewModel() {
    @Inject
    lateinit var interactor: DDInteractor
    lateinit var s3ClientYandex: S3ClientYandex

    val isRecordingChannel = Channel<Boolean>(Channel.CONFLATED)

    val TAG = "myLogs"
    var myBufferSize = 8192
    init {
        App.instance.dagger.inject(this)

        viewModelScope.launch {
            isRecordingChannel.send(false)
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            isRecordingChannel.send(true)
//            interactor.startRecordingAudioFile()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            isRecordingChannel.send(false)
//            interactor.stopRecordingAudioFile()
        }
    }

    fun putRowAudioFileToDB(rowAudiofile: Audiofile) {
        viewModelScope.launch {
            try {
                interactor.addRowToDBAudioFile(rowAudiofile)
            } catch (e: Exception) {
                Log.d(TAG, "Словили исключение при записи данных")
            }
        }
    }
}