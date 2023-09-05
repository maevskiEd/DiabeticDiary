package ed.maevski.diabeticdiary.data

import ed.maevski.diabeticdiary.data.dao.DDDao
import ed.maevski.diabeticdiary.data.entity.Audiofile

class DDRepository(private val ddDao: DDDao) {
    fun putRowAudioFileToDb(rowAudiofile: Audiofile) {
        ddDao.insertRowAudioFile(rowAudiofile)
    }
}