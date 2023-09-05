package ed.maevski.diabeticdiary.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ed.maevski.diabeticdiary.data.dao.DDDao
import ed.maevski.diabeticdiary.data.entity.Audiofile

@Database(entities = [Audiofile::class], version = 1, exportSchema = false)
abstract class DDDatabase : RoomDatabase() {
    abstract fun ddDao(): DDDao
}