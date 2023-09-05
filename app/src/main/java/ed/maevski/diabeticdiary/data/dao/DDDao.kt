package ed.maevski.diabeticdiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ed.maevski.diabeticdiary.data.entity.Audiofile
import kotlinx.coroutines.flow.Flow
@Dao
interface DDDao {
    @Query("SELECT * FROM audiofiles")
    fun getCachedFilms(): Flow<List<Audiofile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Audiofile>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRowAudioFile(rowAudioFile: Audiofile)
}
