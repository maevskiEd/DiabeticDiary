package ed.maevski.diabeticdiary.domain

import ed.maevski.diabeticdiary.data.DDRepository
import ed.maevski.diabeticdiary.data.YandexApi
import ed.maevski.diabeticdiary.data.entity.Audiofile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class DDInteractor(
    private val repo: DDRepository,
    private val retrofitService: YandexApi,
) {
    private val job = Job() // Создание новой Job для скоупа интерактора
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun addRowToDBAudioFile(rowAudiofile: Audiofile) {
        repo.putRowAudioFileToDb(rowAudiofile)
    }

    fun startRecordingAudioFile(){

    }

    fun stopRecordingAudioFile() {

    }

    fun cancelAllJob() {
        job.cancel() // Отмена всех корутин, связанных с этим скоупом
    }
}