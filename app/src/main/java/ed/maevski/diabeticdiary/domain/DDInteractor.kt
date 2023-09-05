package ed.maevski.diabeticdiary.domain

import ed.maevski.diabeticdiary.data.DDRepository
import ed.maevski.diabeticdiary.data.YandexApi
import ed.maevski.diabeticdiary.data.entity.Audiofile

class DDInteractor(
    private val repo: DDRepository,
    private val retrofitService: YandexApi,
) {
    fun putRowAudioFile(rowAudiofile: Audiofile) {
        repo.putRowAudioFileToDb(rowAudiofile)
    }
}